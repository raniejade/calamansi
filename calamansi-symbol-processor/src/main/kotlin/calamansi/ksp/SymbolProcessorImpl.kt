package calamansi.ksp

import calamansi.ksp.model.ComponentDefinition
import calamansi.ksp.model.Definition
import calamansi.ksp.model.PropertyDefinition
import calamansi.ksp.model.ScriptDefinition
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import kotlin.reflect.KClass

@OptIn(KspExperimental::class)
class SymbolProcessorImpl(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator
    private var processed = false
    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            return emptyList()
        }
        val entryDependencies = mutableListOf<KSFile>()
        val definitions = mutableListOf<Definition>()
        for (file in resolver.getAllFiles()) {
            var definitionGenerated = false
            for (decl in file.declarations.filter { it.qualifiedName != null }) {
                val classDecl = resolver.getClassDeclarationByName(checkNotNull(decl.qualifiedName))
                if (classDecl != null) {
                    val generatedDefinition = maybeCreateDefinition(resolver, classDecl)
                    if (generatedDefinition != null) {
                        definitionGenerated = true
                        definitions.add(generatedDefinition)
                    }
                }
            }

            // a definition was generated for one or more declarations in the file
            // we track the file as a dependency of the "entry" file.
            if (definitionGenerated) {
                entryDependencies.add(file)
            }
        }

        val components = definitions.filterIsInstance<ComponentDefinition>()
        val scripts = definitions.filterIsInstance<ScriptDefinition>()

        components.forEach { component -> generateComponentDefinition(resolver, component) }
        scripts.forEach { script -> generateScriptDefinition(resolver, script) }

        // entry file
        codeGenerator.createNewFile(
            Dependencies(true, *entryDependencies.toTypedArray()), GEN_PACKAGE_NAME, "entry"
        ).writer().use { writer ->
            writer.appendLine(
                """
                package $GEN_PACKAGE_NAME
                import calamansi.runtime.Entry
                import calamansi.runtime.registry.Registry
                
                class EntryImpl : Entry {
                    override fun bootstrap(registry: Registry) {
            """.trimIndent()
            )

            components.forEach { component ->
                writer.appendLine("        registry.registerComponent(${component.name})")
            }

            scripts.forEach { script ->
                writer.appendLine("        registry.registerScript(${script.name})")
            }

            writer.appendLine(
                """
                    }
                }
            """.trimIndent()
            )
        }

        // service loader for entry
        codeGenerator.createNewFile(
            Dependencies(true, *entryDependencies.toTypedArray()), "META-INF.services", "calamansi.runtime.Entry", ""
        ).writer().use { writer ->
            writer.appendLine("calamansi._gen.EntryImpl")
        }

        processed = true
        return emptyList()
    }

    private fun generateComponentDefinition(resolver: Resolver, definition: ComponentDefinition) {
        val deps = mutableSetOf<KSFile>()
        deps.add(checkNotNull(definition.original.containingFile))
        deps.addAll(definition.dependencies.map { checkNotNull(it.containingFile) })
        codeGenerator.createNewFile(
            Dependencies(false, *deps.toTypedArray()), GEN_PACKAGE_NAME, definition.name
        ).writer().use { writer ->
            val componentQualifiedName = checkNotNull(definition.original.qualifiedName).asString()

            val properties = definition.properties.joinToString(separator = ",\n${indent(4, 2)}") { prop ->
                val typeRef = "${checkNotNull(prop.type.qualifiedName).asString()}"
                val propertyName = "\"${prop.name}\""
                val propertyRef = "$componentQualifiedName::${prop.name}"
                if (!prop.isEnum) {
                    "SimpleProperty($typeRef::class, $propertyName, $propertyRef)"
                } else {
                    "EnumProperty($typeRef::class, $propertyName, $propertyRef, enumValues<$typeRef>())"
                }
            }

            val dataProperties = definition.properties.joinToString(separator = ",\n${indent(4, 1)}") { prop ->
                val typeRef = "${checkNotNull(prop.type.qualifiedName).asString()}"
                "val ${prop.name}: $typeRef"
            }.ifEmpty { "var __unused: Int? = null" }

            val toDataAssignments = definition.properties.joinToString(separator = ",\n${indent(4, 2)}") { prop ->
                "${prop.name} = component.${prop.name}"
            }

            val fromDataAssignments = definition.properties.joinToString(separator = "\n${indent(4, 2)}") { prop ->
                "component.${prop.name} = data.${prop.name}"
            }

            val dataClassName = "${definition.name}Data"

            val dependencies = definition.dependencies.joinToString(
                separator = ",\n${
                    indent(
                        4,
                        2
                    )
                }"
            ) { "${checkNotNull(it.qualifiedName).asString()}::class" }


            writer.appendLine(
                """
                // GENERATED from ${definition.name}
                package $GEN_PACKAGE_NAME
                import kotlin.collections.listOf
                import kotlin.reflect.KClass
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.modules.SerializersModule
                import kotlinx.serialization.modules.polymorphic
                import kotlinx.serialization.modules.subclass
                import calamansi.component.Component
                import calamansi.runtime.registry.ComponentData
                import calamansi.runtime.registry.ComponentDefinition
                import calamansi.runtime.registry.Property
                import calamansi.runtime.registry.SimpleProperty
                import calamansi.runtime.registry.EnumProperty
                
                @Serializable
                data class $dataClassName(
                    $dataProperties
                ) : ComponentData<$componentQualifiedName> {
                    override val type: KClass<$componentQualifiedName>
                        get() = $componentQualifiedName::class
                }
                
                object ${definition.name} : ComponentDefinition<$componentQualifiedName> {
                    override val type: KClass<$componentQualifiedName> = $componentQualifiedName::class
                    override fun create(): $componentQualifiedName = $componentQualifiedName()
                    override val dependencies: List<KClass<out Component>> = listOf(
                        $dependencies
                    )
                    override val properties: List<Property<$componentQualifiedName, *>> = listOf(
                        $properties
                    )
                    
                    override fun toData(component: Component): ComponentData<*> {
                        require(component is $componentQualifiedName)
                        return $dataClassName(
                            $toDataAssignments
                        )
                    }
                    override fun fromData(data: ComponentData<*>, component: Component) {
                        require(data is $dataClassName)
                        require(component is $componentQualifiedName)
                        $fromDataAssignments  
                    }
                    
                    override fun serializersModule(): SerializersModule = SerializersModule {
                        polymorphic(ComponentData::class) {
                            subclass($dataClassName::class)
                        }
                    }
                }
            """.trimIndent()
            )
        }
    }

    private fun generateScriptDefinition(resolver: Resolver, definition: ScriptDefinition) {
        val deps = mutableSetOf<KSFile>()
        deps.add(checkNotNull(definition.original.containingFile))
        codeGenerator.createNewFile(
            Dependencies(false, *deps.toTypedArray()), GEN_PACKAGE_NAME, definition.name
        ).writer().use { writer ->
            val componentQualifiedName = checkNotNull(definition.original.qualifiedName).asString()
            writer.appendLine(
                """
                // GENERATED from ${definition.name}
                package $GEN_PACKAGE_NAME
                import kotlin.collections.listOf
                import kotlin.reflect.KClass
                import calamansi.runtime.registry.ScriptDefinition
                
                object ${definition.name} : ScriptDefinition<$componentQualifiedName> {
                    override val type: KClass<$componentQualifiedName> = $componentQualifiedName::class
                    override fun create(): $componentQualifiedName = $componentQualifiedName()
                }
            """.trimIndent()
            )
        }
    }

    private fun maybeCreateDefinition(resolver: Resolver, classDecl: KSClassDeclaration): Definition? {
        return when {
            isDirectSubTypeOf(classDecl, QualifiedNames.Component) -> createComponentDefinition(
                resolver, classDecl
            )

            isDirectSubTypeOf(classDecl, QualifiedNames.Script) -> createScriptDefinition(resolver, classDecl)
            else -> null
        }
    }

    private fun createComponentDefinition(resolver: Resolver, classDecl: KSClassDeclaration): ComponentDefinition {
        require(isDirectSubTypeOf(classDecl, QualifiedNames.Component))
        val dependencies = mutableListOf<KSClassDeclaration>()
        // check for dependencies
        val depAnnotation = classDecl.getAnnotation(QualifiedNames.Dependencies)
        if (depAnnotation != null) {
            val components = depAnnotation.arguments[0].value as List<KSType>
            dependencies.addAll(components.map {
                it.declaration as KSClassDeclaration
            })
        }

        val properties = classDecl.getDeclaredProperties().filter { isSupportedProperty(resolver, it) }.map {
            val propTypeDecl = it.type.resolve().declaration as KSClassDeclaration
            PropertyDefinition(
                checkNotNull(it.qualifiedName).getShortName(),
                propTypeDecl,
            )
        }

        return ComponentDefinition(
            generateDefinitionName(classDecl), classDecl, properties.toList(), dependencies.toList()
        )
    }

    private fun createScriptDefinition(resolver: Resolver, classDecl: KSClassDeclaration): ScriptDefinition {
        require(isDirectSubTypeOf(classDecl, QualifiedNames.Script))
        return ScriptDefinition(
            generateDefinitionName(classDecl), classDecl
        )
    }

    // com.example.Foo -> com_example_FooDefinition
    private fun generateDefinitionName(classDecl: KSClassDeclaration): String {
        return checkNotNull(classDecl.qualifiedName).asString().replace(".", "_")
    }


    private fun isSupportedProperty(resolver: Resolver, property: KSPropertyDeclaration): Boolean {
        val builtIns = resolver.builtIns
        val type = property.type.resolve()
        val isBuiltInType = when (type) {
            builtIns.intType, builtIns.floatType, builtIns.doubleType, builtIns.stringType, builtIns.shortType, builtIns.longType -> true
            else -> false
        }
        val isEnum = (type.declaration as KSClassDeclaration).classKind == ClassKind.ENUM_CLASS
        val isSupportedType = isBuiltInType || isEnum
        val hasPropertyAnnotation = property.hasAnnotation(QualifiedNames.Property)
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

        private object QualifiedNames {
            const val Component = "calamansi.component.Component"
            const val Property = "calamansi.component.Property"
            const val Dependencies = "calamansi.component.Dependencies"
            const val Script = "calamansi.Script"
        }

        // extra baseIndent indents is for trimIndent when using string blocks
        private fun indent(baseIndent: Int, times: Int) = "    ".repeat(baseIndent + times)
    }
}