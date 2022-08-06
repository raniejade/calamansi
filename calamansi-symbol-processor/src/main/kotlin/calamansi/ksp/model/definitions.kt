package calamansi.ksp.model

import com.google.devtools.ksp.symbol.KSClassDeclaration

sealed class Definition(val name: String, val original: KSClassDeclaration)
class ComponentDefinition(name: String, original: KSClassDeclaration, val dependencies: List<KSClassDeclaration>) :
    Definition(name, original)

class ScriptDefinition(name: String, original: KSClassDeclaration) : Definition(name, original)