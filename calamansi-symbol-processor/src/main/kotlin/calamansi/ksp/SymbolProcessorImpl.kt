package calamansi.ksp

import calamansi.ksp.model.NodeDefinition
import calamansi.ksp.model.PropertyDefinition
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*

class SymbolProcessorImpl(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator
    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) {
            return emptyList()
        }
        val entryDependencies = mutableListOf<KSFile>()
        val nodes = mutableListOf<NodeDefinition>()
        for (file in resolver.getAllFiles()) {
            var definitionGenerated = false
            for (decl in file.declarations.filter { it.qualifiedName != null }.filterIsInstance<KSClassDeclaration>()) {
                val generatedDefinition = createNodeDefinition(resolver, decl)
                if (generatedDefinition != null) {
                    definitionGenerated = true
                    nodes.add(generatedDefinition)
                }
            }

            // a definition was generated for one or more declarations in the file
            // we track the file as a dependency of the "entry" file.
            if (definitionGenerated) {
                entryDependencies.add(file)
            }
        }

        nodes.forEach { node -> generateCodeForNodeDefinition(resolver, node) }

        // service file
        codeGenerator.createNewFile(
            Dependencies(true, *entryDependencies.toTypedArray()),
            "META-INF.services",
            "calamansi.internal.NodeDefinition",
            extensionName = ""
        ).writer().use { writer ->
            nodes.forEach { node -> writer.appendLine("calamansi._gen.${node.name}") }
        }

        processed = true
        return emptyList()
    }

    private fun generateCodeForNodeDefinition(resolver: Resolver, definition: NodeDefinition) {
        val deps = mutableSetOf<KSFile>()
        deps.add(checkNotNull(definition.original.containingFile))
        codeGenerator.createNewFile(
            Dependencies(false, *deps.toTypedArray()), GEN_PACKAGE_NAME, definition.name
        ).writer().use { writer ->
            val nodeQualifiedName = checkNotNull(definition.original.qualifiedName).asString()
            val dataClassName = "${definition.name}Data"

            val properties = definition.properties.joinToString(separator = ",\n${indent(4, 2)}") { prop ->
                val typeRef = checkNotNull(prop.type.qualifiedName).asString()
                val propertyName = "\"${prop.name}\""
                val propertyRef = "$dataClassName::${prop.name}"
                val nodePropertyType = if (!prop.isEnum) {
                    "NodePropertyType.Simple(typeOf<$typeRef>())"
                } else {
                    "NodePropertyType.Enum(typeOf<$typeRef>(), enumValues<$typeRef>())"
                }

                "NodeProperty($propertyName, $nodePropertyType , { thisRef, value -> $propertyRef.set(thisRef as $dataClassName, value as $typeRef) }, { thisRef -> $propertyRef.get(thisRef as $dataClassName) })"
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
                // GENERATED from $nodeQualifiedName
                package $GEN_PACKAGE_NAME
                import kotlin.collections.setOf
                import kotlin.reflect.KClass
                import kotlin.reflect.typeOf
                import kotlinx.serialization.Contextual
                import kotlinx.serialization.Serializable
                import kotlinx.serialization.modules.SerializersModule
                import kotlinx.serialization.modules.polymorphic
                import kotlinx.serialization.modules.subclass
                
                import calamansi.node.Node
                import calamansi.internal.NodeDefinition
                import calamansi.internal.NodeProperty
                import calamansi.internal.NodePropertyType
                import calamansi.internal.NodeData
                import calamansi.meta.CalamansiInternal
                
                @Serializable
                @CalamansiInternal
                data class $dataClassName(
                    $dataProperties
                ) : NodeData {
                    override val type: KClass<out Node>
                        get() = $nodeQualifiedName::class
                }
                
                @CalamansiInternal
                class ${definition.name} : NodeDefinition {
                    override val type: KClass<out Node> = $nodeQualifiedName::class
                    override fun create(): $nodeQualifiedName = $nodeQualifiedName()
                    
                    override val properties: Set<NodeProperty> = setOf(
                        $properties
                    )
                    
                    override fun applyData(target: Node, data: NodeData) {
                        require(target is $nodeQualifiedName && data is $dataClassName)
                        $fromDataAssignments
                    }

                    override fun extractData(target: Node): NodeData {
                        require(target is $nodeQualifiedName)
                        return $dataClassName(
                            $toDataAssignments
                        )
                    }
                    
                    override fun serializersModule(): SerializersModule = SerializersModule {
                        polymorphic(NodeData::class) {
                            subclass($dataClassName::class)
                        }
                    }
                }
            """.trimIndent()
            )
        }
    }

    private fun createNodeDefinition(resolver: Resolver, classDecl: KSClassDeclaration): NodeDefinition? {
        // subtype of Node or Node itself
        if (!isDirectSubTypeOf(
                classDecl,
                QualifiedNames.Node
            ) && checkNotNull(classDecl.qualifiedName).asString() != QualifiedNames.Node
        ) {
            return null
        }

        val properties = classDecl.getAllProperties().filter { isSupportedProperty(resolver, it) }.map {
            val propTypeDecl = it.type.resolve().declaration as KSClassDeclaration
            PropertyDefinition(
                checkNotNull(it.qualifiedName).getShortName(),
                propTypeDecl
            )
        }.toList()

        environment.logger.info(
            "properties for $classDecl: ${
                classDecl.getAllProperties()
                    .map { it.qualifiedName?.asString() to it.annotations.map { it.shortName }.toList() }.toList()
            }"
        )

        return NodeDefinition(
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
        // no need to check for hasBackingProperty as Property annotation can only be applied for fields.
        return property.isMutable && property.isPublic() && hasPropertyAnnotation && isSupportedType
    }

    fun KSAnnotated.hasAnnotation(qualifiedName: String): Boolean {
        return getAnnotation(qualifiedName) != null
    }

    fun KSAnnotated.getAnnotation(qualifiedName: String): KSAnnotation? {
        for (annotation in annotations) {
            val type = annotation.annotationType.resolve().declaration
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
        return classDecl.getAllSuperTypes()
            .any { checkNotNull(it.declaration.qualifiedName).asString() == type }
    }

    companion object {
        private const val GEN_PACKAGE_NAME = "calamansi._gen"
        private val SUPPORTED_TYPES = setOf(
            // JOML
            "org.joml.Vector2f",
            "org.joml.Vector3f",

            // Calamansi UI
            "calamansi.ui.FlexLayout",
            "calamansi.ui.FlexAlign",
            "calamansi.ui.FlexDirection",
            "calamansi.ui.FlexJustify",
            "calamansi.ui.FlexWrap",
            "calamansi.ui.FlexBounds",
            "calamansi.ui.FlexAxisValue",
            "calamansi.ui.FlexAxisValue.Auto",
            "calamansi.ui.FlexAxisValue.Fixed",
            "calamansi.ui.FlexAxisValue.Relative",
            "calamansi.ui.FontValue",
            "calamansi.ui.FontValue.Inherit",
            "calamansi.ui.FontValue.Ref",
        )

        private object QualifiedNames {
            const val Node = "calamansi.node.Node"
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