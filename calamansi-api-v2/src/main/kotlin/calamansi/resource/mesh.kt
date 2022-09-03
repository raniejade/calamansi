package calamansi.resource

sealed class Mesh : Resource
class SimpleMesh : Mesh()
class SkinnedMesh : Mesh()