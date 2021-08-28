package net.pwall.util.depends

import java.io.File

data class Group(val groupId: String, val artefacts: List<Artefact>) {

    val versions: List<Version> = artefacts.flatMap { it.versions }

    override fun toString() = groupId

    companion object {

        val groups = listOf("net.pwall.*", "io.kjson")

        val repositoryRoot = "${System.getenv("HOME")}/.m2/repository"

        fun readGroups(): List<Group> {
            val result = mutableListOf<Group>()
            for (groupId in groups)
                result.addAll(readGroups(groupId))
            for (group in result) {
                for (artefact in group.artefacts) {
                    for (version in artefact.versions) {
                        for (dependency in version.dependencies) {
                            dependency.latest = result.find { it.groupId == dependency.groupId }?.artefacts?.
                                find { it.artefactId == dependency.artefactId }?.versions?.last()?.
                                    let { it.versionId == dependency.versionId } ?: false
                        }
                    }
                }
            }
            return result.sortedBy { it.groupId }
        }

        fun readGroups(groupId: String) = readGroups(groupId, groupId.split('.'), File(repositoryRoot))

        fun readGroups(groupId: String, dirs: List<String>, directory: File): List<Group> {
            check(directory.isDirectory) { "Not a directory - $directory" }
            if (dirs.size > 1)
                return readGroups(groupId, dirs.drop(1), File(directory, dirs.first()))
            if (dirs.first() == "*") {
                val groupIdTrimmed = groupId.dropLast(2)
                val result = mutableListOf<Group>()
                val subDirs = directory.list() ?: emptyArray()
                for (subDir in subDirs)
                    result.add(readGroup("$groupIdTrimmed.$subDir", File(directory, subDir)))
                return result
            }
            return listOf(readGroup(groupId, File(directory, dirs.first()))).sortedBy { it.groupId }
        }

        fun readGroup(groupId: String, directory: File): Group {
            check(directory.isDirectory) { "Not a directory - $directory" }
            val artefacts = mutableListOf<Artefact>()
            val subDirs = directory.list() ?: emptyArray()
            for (subDir in subDirs)
                artefacts.add(Artefact.readArtefact(groupId, subDir, File(directory, subDir)))
            return Group(groupId, artefacts.sortedBy { it.artefactId })
        }

    }

}
