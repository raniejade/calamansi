package calamansi.ksp.model

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

class PropertyDefinition(val name: String, val type: KSClassDeclaration) {
    val isEnum: Boolean = type.classKind == ClassKind.ENUM_CLASS
}

sealed class Definition(val name: String, val original: KSClassDeclaration)
class ComponentDefinition(
    name: String,
    original: KSClassDeclaration,
    val properties: List<PropertyDefinition>,
    val dependencies: List<KSClassDeclaration>
) : Definition(name, original)

class ScriptDefinition(name: String, original: KSClassDeclaration) : Definition(name, original)