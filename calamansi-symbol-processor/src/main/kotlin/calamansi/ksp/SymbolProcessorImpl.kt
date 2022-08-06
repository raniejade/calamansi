package calamansi.ksp

import calamansi.component.Component
import calamansi.component.Property
import calamansi.ksp.model.ComponentDefinition
import calamansi.ksp.model.Definition
import calamansi.ksp.model.ScriptDefinition
import calamansi.script.Script
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
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

        // entry file
        codeGenerator.createNewFile(
            Dependencies(true, *entryDependencies.toTypedArray()), GEN_PACKAGE_NAME, "entry"
        ).writer().use { writer ->
            writer.appendLine("""
                package $GEN_PACKAGE_NAME
                import calamansi.runtime.Entry
                import calamansi.runtime.registry.Registry
                
                class EntryImpl : Entry {
                    override fun bootstrap(registry: Registry) {
                
            """.trimIndent())

            components.forEach { component ->
                writer.appendLine("        registry.registerComponent(${component.name})")
            }

            writer.appendLine("""
                    }
                }
            """.trimIndent())
        }

        // service loader for entry
        codeGenerator.createNewFile(
            Dependencies(true, *entryDependencies.toTypedArray()), "META-INF.service", "calamansi.runtime.Entry", ""
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
            writer.appendLine(
                """
                // GENERATED from ${definition.name}
                package $GEN_PACKAGE_NAME
                import kotlin.collections.listOf
                import kotlin.reflect.KClass
                import calamansi.runtime.registry.ComponentDefinition
                
                object ${definition.name} : ComponentDefinition<$componentQualifiedName> {
                    override val type: KClass<$componentQualifiedName> = $componentQualifiedName::class
                    override val placeholderInstance: $componentQualifiedName = $componentQualifiedName()
                    override val dependencies: List<ComponentDefinition<*>> = listOf()
                }
            """.trimIndent()
            )
        }
    }

    private fun maybeCreateDefinition(resolver: Resolver, classDecl: KSClassDeclaration): Definition? {
        return when {
            isDirectSubTypeOf(classDecl, Component::class) -> createComponentDefinition(
                resolver, classDecl
            )

            isDirectSubTypeOf(classDecl, Script::class) -> createScriptDefinition(resolver, classDecl)
            else -> null
        }
    }

    private fun createComponentDefinition(resolver: Resolver, classDecl: KSClassDeclaration): ComponentDefinition {
        require(isDirectSubTypeOf(classDecl, Component::class))
        val dependencies = mutableListOf<KSClassDeclaration>()
        // check for dependencies
        val depAnnotation = classDecl.getAnnotationsByType(calamansi.component.Dependencies::class).firstOrNull()
        if (depAnnotation != null) {
            dependencies.addAll(depAnnotation.components.map {
                checkNotNull(
                    resolver.getClassDeclarationByName(
                        checkNotNull(it.qualifiedName)
                    )
                )
            })
        }

        // create component definition
        val properties = classDecl.getDeclaredProperties().filter { isSupportedProperty(resolver, it) }.toList()

        return ComponentDefinition(
            generateDefinitionName(classDecl), classDecl, dependencies.toList()
        )
    }

    private fun createScriptDefinition(resolver: Resolver, classDecl: KSClassDeclaration): ScriptDefinition {
        require(isDirectSubTypeOf(classDecl, Script::class))
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
        val typeSupported = when (property.type.resolve()) {
            builtIns.intType, builtIns.floatType, builtIns.doubleType, builtIns.stringType, builtIns.shortType, builtIns.longType -> true
            else -> false
        }
        return property.isMutable && property.hasBackingField && property.isPublic() && property.isAnnotationPresent(
            Property::class
        ) && typeSupported
    }

    private fun isDirectSubTypeOf(classDecl: KSClassDeclaration, type: KClass<*>): Boolean {
        return classDecl.superTypes.any { checkNotNull(it.resolve().declaration.qualifiedName).asString() == type.qualifiedName }
    }

    companion object {
        private const val GEN_PACKAGE_NAME = "calamansi._gen"
    }
}