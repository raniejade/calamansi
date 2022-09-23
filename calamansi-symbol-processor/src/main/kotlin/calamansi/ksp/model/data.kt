package calamansi.ksp.model

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration

class PropertyDefinition(val name: String, val type: KSClassDeclaration, val nullable: Boolean) {
    val isEnum: Boolean = type.classKind == ClassKind.ENUM_CLASS

    fun qualifiedName(): String {
        val name = checkNotNull(type.qualifiedName).asString()
        return "$name${if (nullable) "?" else ""}"
    }
}

class NodeDefinition(val name: String, val original: KSClassDeclaration, val properties: List<PropertyDefinition>)