package net.pwall.util

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondBytes
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import net.pwall.mustache.parser.Parser
import net.pwall.util.depends.Group

class ItDepends {

    private val groups = Group.readGroups()
    private val versions = groups.flatMap { it.versions }

    private val mustacheParser = Parser().also { parser ->
        parser.resolvePartial = { name ->
            ItDepends::class.java.getResourceAsStream("/templates/$name.mustache")?.let { return@let it.reader() } ?:
                    throw RuntimeException("Can't find template - $name")
        }
    }

    private val mainTemplate = mustacheParser.parse(mustacheParser.resolvePartial("main"))
    private val groupTemplate = mustacheParser.parse(mustacheParser.resolvePartial("group"))
    private val artefactTemplate = mustacheParser.parse(mustacheParser.resolvePartial("artefact"))
    private val versionTemplate = mustacheParser.parse(mustacheParser.resolvePartial("version"))

    private val css = ItDepends::class.java.getResourceAsStream("/css/depends.css")?.reader()?.readText() ?:
            throw RuntimeException("Can't read CSS")

    private val favicon = ItDepends::class.java.getResourceAsStream("/images/kjson32.ico")?.readBytes() ?:
            throw RuntimeException("Can't read favicon")

    private lateinit var server: ApplicationEngine

    fun serve() {
        server = embeddedServer(Netty, port = 8889) {
            routing {
                get("/") {
                    call.respondTextWriter(ContentType.Text.Html, HttpStatusCode.OK) {
                        mainTemplate.processTo(this, mapOf("groups" to groups))
                    }
                }
                get("/group/{groupId}") {
                    val groupId = call.parameters["groupId"]
                    val group = groups.find { it.groupId == groupId }
                    call.respondTextWriter(ContentType.Text.Html, HttpStatusCode.OK) {
                        groupTemplate.processTo(this, mapOf("group" to group))
                    }
                }
                get("/artefact/{groupId}/{artefactId}") {
                    val groupId = call.parameters["groupId"]
                    val group = groups.find { it.groupId == groupId }
                    val artefactId = call.parameters["artefactId"]
                    val artefact = group?.artefacts?.find { it.artefactId == artefactId }
                    call.respondTextWriter(ContentType.Text.Html, HttpStatusCode.OK) {
                        artefactTemplate.processTo(this, mapOf("artefact" to artefact))
                    }
                }
                get("/version/{groupId}/{artefactId}/{versionId}") {
                    val groupId = call.parameters["groupId"]
                    val group = groups.find { it.groupId == groupId }
                    val artefactId = call.parameters["artefactId"]
                    val artefact = group?.artefacts?.find { it.artefactId == artefactId }
                    val versionId = call.parameters["versionId"]
                    val version = artefact?.versions?.find { it.versionId.toString() == versionId }
                    val dependedOn = versions.filter { it.dependencies.any { d ->
                        d.groupId == groupId && d.artefactId == artefactId && d.versionId.toString() == versionId } }
                    call.respondTextWriter(ContentType.Text.Html, HttpStatusCode.OK) {
                        versionTemplate.processTo(this, mapOf("version" to version, "dependedOn" to dependedOn))
                    }
                }
                get("/depends.css") {
                    call.respondText(ContentType.Text.CSS, HttpStatusCode.OK) { css }
                }
                get("/kjson32.ico") {
                    call.respondBytes(ContentType("image", "vnd.microsoft.icon"), HttpStatusCode.OK) { favicon }
                }
                get("/stop") {
                    server.stop(0, 0)
                }
            }
        }.start()
    }

}
