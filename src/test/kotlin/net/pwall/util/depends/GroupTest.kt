@file:Suppress("NO_CONSTRUCTOR")
package net.pwall.util.depends

import kotlin.test.Test

class GroupTest {

    @Test fun `should read all groups`() {
        val groups = Group.readGroups()
        val versions = groups.flatMap { it.versions }
        for (version in versions)
            println(version)
    }

    @Test fun `should find dependencies`() {
        val groups = Group.readGroups()
        val versions = groups.flatMap { it.versions }
        for (version in versions) {
            val dependedOnBy = versions.filter { it.dependencies.any { a -> a.toString() == version.coordinates } }
            println("${version.coordinates} depended on by ${dependedOnBy.joinToString { it.coordinates }}")
        }
    }

}
