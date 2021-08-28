package net.pwall.util.depends

import java.io.File

class Artefact(val groupId: String, val artefactId: String, val versions: List<Version>) {

    override fun toString() = "$groupId:$artefactId:[${versions.joinToString(separator = ", ") { it.versionId.toString() }}]"

    companion object {

        fun readArtefact(groupId: String, artefactId: String, directory: File): Artefact {
            check(directory.isDirectory) { "Not a directory - $directory" }
            val versions = mutableListOf<Version>()
            val names = directory.list() ?: emptyArray()
            for (name in names)
                if (!name.endsWith(".xml") && !name.endsWith(".properties") && !name.endsWith(".sha1"))
                    Version.readVersion(groupId, artefactId, name, File(directory, name))?.let { versions.add(it) }
            return Artefact(groupId, artefactId, versions.sortedBy { it.versionId })
        }

    }

}
