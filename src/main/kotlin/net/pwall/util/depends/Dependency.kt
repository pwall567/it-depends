package net.pwall.util.depends

data class Dependency(val groupId: String, val artefactId: String, val versionId: VersionNumber) {

    var latest: Boolean = false

    override fun toString() = "$groupId:$artefactId:$versionId"

}
