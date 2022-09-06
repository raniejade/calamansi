package calamansi.ksp

import calamansi.ksp.model.PropertyDefinition
import calamansi.ksp.model.ScriptDefinition
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import kotlin.reflect.typeOf

class SymbolProcessorImpl(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator
    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            return emptyList()
        }
        val entryDependencies = mutableListOf<KSFile>()
        val scripts = mutableListOf<ScriptDefinition>()
        for (file in resolver.getAllFiles()) {
            var definitionGenerated = false
            for (decl in file.declarations.filter { it.qualifiedName != null }) {
                val classDecl = resolver.getClassDeclarationByName(checkNotNull(decl.qualifiedName))
                if (classDecl != null) {
                    val generatedDefinition = createScriptDefinition(resolver, classDecl)
                    if (generatedDefinition != null) {
                        definitionGenerated = true
                        scripts.add(generatedDefinition)
                    }
                }
            }

            // a definition was generated for one or more declarations in the file
            // we track the file as a dependency of the "entry" file.
            if (definitionGenerated) {
                entryDependencies.add(file)
            }
        }

        scripts.forEach { script -> generateCodeForScriptDefinition(resolver, script) }

        // service file
        codeGenerator.createNewFile(
            Dependencies(true, *entryDependencies.toTypedArray()),
            "META-INF.services",
            "calamansi.internal.ScriptDefinition",
            extensionName = ""
        ).writer().use { writer ->
            scripts.forEach { script -> writer.appendLine("calamansi._gen.${script.name}") }
        }

        processed = true
        return emptyList()
    }

    private fun generateCodeForScriptDefinition(resolver: Resolver, definition: ScriptDefinition) {
        val deps = mutableSetOf<KSFile>()
        deps.add(checkNotNull(definition.original.containingFile))
        codeGenerator.createNewFile(
            Dependencies(false, *deps.toTypedArray()), GEN_PACKAGE_NAME, definition.name
        ).writer().use { writer ->
            val scriptQualifiedName = checkNotNull(definition.original.qualifiedName).asString()
            val dataClassName = "${definition.name}Data"

            val properties = definition.properties.joinToString(separator = ",\n${indent(4, 2)}") { prop ->
                val typeRef = checkNotNull(prop.type.qualifiedName).asString()
                val propertyName = "\"${prop.name}\""
                val propertyRef = "$dataClassName::${prop.name}"
                val scriptPropertyType = if (!prop.isEnum) {
                    "ScriptPropertyType.Simple(typeOf<$typeRef>())"
                } else {
                    "ScriptPropertyType.Enum(typeOf<$typeRef>(), enumValues<$typeRef>(), $propertyRef)"
                }

                "ScriptProperty($propertyName, $scriptPropertyType , { thisRef, value -> $propertyRef.set(thisRef as $dataClassName, value as $typeRef) }, { thisRef -> $propertyRef.get(thisRef as $dataClassName) })"
            }

            val dataProperties = definition.properties.joinToString(separator = ",\n${indent(4, 1)}") { prop ->
                val typeRef = "${checkNotNull(prop.type.qualifiedName).asString()}"
                "@Contextual var ${prop.name}: $typeRef"
            }.ifEmpty { "var __unused: Int? = null" }

            val toDataAssignments = definition.properties.joinToString(separator = ",\n${indent(4, 2)}") { prop ->
                "${prop.name} = target.${prop.name}"
            }

            val fromDataAssignments = definition.properties.joinToString(separator = "\n${indent(4, 2)}") { prop ->
                "target.${prop.name} = data.${prop.name}"
            }

            writer.appendLine(
                """
                // GENERATED from $scriptQualifiedName
                package $GEN_PACKAGE_NAME
                import kotlin.collections.setOf
                import kotlin.reflect.KClass
                import kotlin.reflect.typeOf
                import kotlinx.serialization.Contextual
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.modules.SerializersModule
                import kotlinx.serialization.modules.polymorphic
                import kotlinx.serialization.modules.subclass
                
                import calamansi.Script
                import calamansi.internal.ScriptDefinition
                import calamansi.internal.ScriptProperty
                import calamansi.internal.ScriptPropertyType
                import calamansi.internal.ScriptData
                import calamansi.meta.CalamansiInternal
                
                @Serializable
                @CalamansiInternal
                data class $dataClassName(
                    $dataProperties
                ) : ScriptData {
                    override val type: KClass<out Script>
                        get() = $scriptQualifiedName::class
                }
                
                @CalamansiInternal
                class ${definition.name} : ScriptDefinition {
                    override val type: KClass<out Script> = $scriptQualifiedName::class
                    override fun create(): $scriptQualifiedName = $scriptQualifiedName()
                    
                    override val properties: Set<ScriptProperty> = setOf(
                        $properties
                    )
                    
                    override fun applyData(target: Script, data: ScriptData) {
                        require(target is $scriptQualifiedName && data is $dataClassName)
                        $fromDataAssignments
                    }

                    override fun extractData(target: Script): ScriptData {
                        require(target is $scriptQualifiedName)
                        return $dataClassName(
                            $toDataAssignments
                        )
                    }
                    
                    override fun serializersModule(): SerializersModule = SerializersModule {
                        polymorphic(ScriptData::class) {
                            subclass($dataClassName::class)
                        }
                    }
                }
            """.trimIndent()
            )
        }
    }

    private fun createScriptDefinition(resolver: Resolver, classDecl: KSClassDeclaration): ScriptDefinition? {
        if (!isDirectSubTypeOf(classDecl, QualifiedNames.Script)) {
            return null
        }

        val properties = classDecl.getDeclaredProperties().filter { isSupportedProperty(resolver, it) }.map {
            val propTypeDecl = it.type.resolve().declaration as KSClassDeclaration
            PropertyDefinition(
                checkNotNull(it.qualifiedName).getShortName(),
                propTypeDecl
            )
        }.toList()

        return ScriptDefinition(
            generateDefinitionName(classDecl), classDecl, properties
        )
    }


    private fun isSupportedProperty(resolver: Resolver, property: KSPropertyDeclaration): Boolean {
        val builtIns = resolver.builtIns
        val type = property.type.resolve()
        val isBuiltInType = when (type) {
            builtIns.intType, builtIns.floatType, builtIns.doubleType, builtIns.stringType, builtIns.shortType, builtIns.longType -> true
            else -> SUPPORTED_TYPES.contains(type.declaration.qualifiedName?.asString())
        }
        val isEnum = (type.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS
        val isSupportedType = isBuiltInType || isEnum
        val hasPropertyAnnotation = property.hasAnnotation(QualifiedNames.Property)
        if (hasPropertyAnnotation && !isSupportedType) {
            environment.logger.error(
                "Annotated property '${property.qualifiedName?.getShortName()}' is not supported.",
                property
            )
        }
        return property.isMutable && property.hasBackingField && property.isPublic() && hasPropertyAnnotation && isSupportedType
    }

    fun KSAnnotated.hasAnnotation(qualifiedName: String): Boolean {
        return getAnnotation(qualifiedName) != null
    }

    fun KSAnnotated.getAnnotation(qualifiedName: String): KSAnnotation? {
        for (annotation in annotations) {
            val type = annotation.annotationType.resolve().declaration
            type.qualifiedName
            if (type !is KSClassDeclaration) {
                return null
            }

            val annotationQualifiedName = type.qualifiedName ?: return null
            if (annotationQualifiedName.asString() == qualifiedName) {
                return annotation
            }
        }

        return null
    }

    private fun isDirectSubTypeOf(classDecl: KSClassDeclaration, type: String): Boolean {
        return classDecl.superTypes.any { checkNotNull(it.resolve().declaration.qualifiedName).asString() == type }
    }

    companion object {
        private const val GEN_PACKAGE_NAME = "calamansi._gen"
        private val SUPPORTED_TYPES = setOf(
            "org.joml.Vector2f",
            "org.joml.Vector3f",
        )

        private object QualifiedNames {
            const val Script = "calamansi.Script"
            const val Property = "calamansi.meta.Property"
            const val ResourceRef = "calamansi.resource.ResourceRef"
        }

        // com.example.Foo -> com_example_FooDefinition
        private fun generateDefinitionName(classDecl: KSClassDeclaration): String {
            return checkNotNull(classDecl.qualifiedName).asString().replace(".", "_")
        }

        // extra baseIndent indents is for trimIndent when using string blocks
        private fun indent(baseIndent: Int, times: Int) = "    ".repeat(baseIndent + times)
    }
}