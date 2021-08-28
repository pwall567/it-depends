package net.pwall.util.depends

import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import net.pwall.xml.XML
import org.w3c.dom.Element
import org.w3c.dom.Text

data class Version(
    val groupId: String,
    val artefactId: String,
    val versionId: VersionNumber,
    val dateTime: Instant,
    val dependencies: List<Dependency>
) {

    val coordinates: String
        get() = "$groupId:$artefactId:$versionId"

    private val zonedDateTime: ZonedDateTime = ZonedDateTime.ofInstant(dateTime, ZoneId.systemDefault())

    @Suppress("UNUSED")
    val date: LocalDate = LocalDate.from(zonedDateTime)

    @Suppress("UNUSED")
    val time: LocalTime = LocalTime.from(zonedDateTime)

    override fun toString() = "$coordinates -> ${dependencies.joinToString(", ")}"

    companion object {

        const val mavenNS = "http://maven.apache.org/POM/4.0.0"
        val documentBuilder = XML.getDocumentBuilderNS()

        fun readVersion(groupId: String, artefactId: String, version: String, directory: File): Version? {
            val dependencies = mutableListOf<Dependency>()
            check(directory.isDirectory) { "Not a directory - $directory" }
            val pom = File(directory, "$artefactId-$version.pom")
            if (!pom.isFile || !pom.canRead())
                return null
            val date = Instant.ofEpochMilli(pom.lastModified())
            val dom = documentBuilder.parse(pom)
            val rootElement = XML.getDocumentElement(dom, "project", mavenNS)
            val parentDOM = rootElement.getElementChild("parent")?.let { parentElement ->
                val g = parentElement.getTextChild("groupId")
                val a = parentElement.getTextChild("artifactId")
                val v = parentElement.getTextChild("version")
                dependencies.add(Dependency(g, a, VersionNumber.of(v)))
                val parentFile = File(Group.repositoryRoot, "${g.replace('.', '/')}/$a/$v/$a/$v/$a-$v.pom")
                if (parentFile.isFile && parentFile.canRead()) documentBuilder.parse(parentFile) else null
            }
            rootElement.getElementChild("dependencies")?.let {
                for (depElement in XML.elementIterator(it)) {
                    if (depElement.tagName != "dependency")
                        throw RuntimeException("dependency expected")
                    val g = depElement.getTextChild("groupId")
                    if (groupMatchesTarget(g)) {
                        val a = depElement.getTextChild("artifactId")
                        val v = depElement.getTextChild("version")
                        dependencies.add(Dependency(g, a, VersionNumber.of(v)))
                    }
                }
            }
            return Version(groupId, artefactId, VersionNumber.of(version), date, dependencies)
        }

        private fun Element.getElementChild(name: String): Element? {
            return getElementsByTagNameNS(mavenNS, name)?.let {
                if (it.length == 0) null else it.item(0) as? Element
            }
        }

        private fun Element.getTextChild(name: String): String {
            val node = getElementChild(name) ?: throw RuntimeException("Can't find $name in $tagName")
            val children = node.childNodes
            if (children == null || children.length != 1 || children.item(0) !is Text)
                throw RuntimeException("Not text node - $name in $tagName")
            return (children.item(0) as Text).data
        }

        private fun groupMatchesTarget(groupId: String): Boolean {
            for (target in Group.groups)
                if (target.endsWith(".*") && groupId.startsWith(target.dropLast(1)) || groupId == target)
                    return true
            return false
        }

    }

}
