package calamansi.ksp.model

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

class PropertyDefinition(val name: String, val type: KSClassDeclaration) {
    val isEnum: Boolean = type.classKind == ClassKind.ENUM_CLASS
}

class ScriptDefinition(val name: String, val original: KSClassDeclaration, val properties: List<PropertyDefinition>)