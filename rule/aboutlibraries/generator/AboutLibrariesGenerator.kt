package top.fifthlight.aboutlibraries.generator

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import com.mikepenz.aboutlibraries.plugin.mapping.SpdxLicense
import com.ubertob.kondor.json.JsonStyle
import com.ubertob.kondor.json.toJson
import kotlinx.coroutines.*
import org.apache.maven.model.building.DefaultModelBuildingRequest
import org.apache.maven.model.building.ModelBuilder
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactDescriptorRequest
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.spi.localrepo.LocalRepositoryManagerFactory
import org.eclipse.sisu.launch.Main
import org.slf4j.LoggerFactory
import top.fifthlight.aboutlibraries.config.Config
import top.fifthlight.touchcontroller.common.about.Developer
import top.fifthlight.touchcontroller.common.about.Library
import top.fifthlight.touchcontroller.common.about.Libs
import top.fifthlight.touchcontroller.common.about.License
import top.fifthlight.touchcontroller.common.about.serialization.JLibs
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Named
import org.apache.maven.model.License as MavenLicense

@Named
class AboutLibrariesGenerator @Inject constructor(
    val repositorySystem: RepositorySystem,
    val localRepositoryManagerFactory: LocalRepositoryManagerFactory,
    val modelBuilder: ModelBuilder,
) : CliktCommand() {
    val config: Path by option().path().required().help("Config file")
    val output: Path by option().path().required().help("Output file")

    private val jsonStyle = JsonStyle(
        appendFieldSeparator = JsonStyle.Companion::comma,
        appendValueSeparator = JsonStyle.Companion::colonSpace,
        appendNewline = JsonStyle.Companion::appendNewLineIndent,
        sortedObjectFields = false,
        explicitNulls = false,
    )

    override fun run(): Unit = runBlocking {
        val config = withContext(Dispatchers.IO) { Config(config) }

        val localRepoDirectory = Path.of(System.getProperty("user.home"), ".m2", "repository").toFile()
        val session = DefaultRepositorySystemSession().apply {
            setLocalRepositoryManager(
                localRepositoryManagerFactory.newInstance(
                    this,
                    LocalRepository(localRepoDirectory)
                )
            )
        }

        val repositories = config.repositories.map { (key, uri) ->
            RemoteRepository.Builder(key, "default", uri.toString()).build()
        }

        val descriptors = config.coordinates.map { (groupId, artifactId, version) ->
            val request = ArtifactDescriptorRequest().apply {
                this.repositories = repositories
                this.artifact = DefaultArtifact(groupId, artifactId, null, "pom", version)
            }
            async(Dispatchers.IO) {
                repositorySystem.readArtifactDescriptor(session, request)
            }
        }.awaitAll()

        val artifacts = withContext(Dispatchers.IO) {
            val requests = descriptors.map {
                ArtifactRequest(it.artifact, repositories, null)
            }
            repositorySystem.resolveArtifacts(session, requests)
        }

        val models = withContext(Dispatchers.IO) {
            artifacts.map { artifact ->
                modelBuilder.build(DefaultModelBuildingRequest().apply {
                    this.pomFile = artifact.artifact.file
                }).effectiveModel
            }
        }

        fun findLicense(license: MavenLicense) = SpdxLicense.entries.find { entry ->
            val idMatch = license.name == entry.id
            val nameMatch = license.name == entry.name
            val fullNameMatch = license.name == entry.fullName
            val customMatcherMatch = entry.customMatcher?.let { it(license.name, license.url) } ?: false

            idMatch || nameMatch || fullNameMatch || customMatcherMatch
        }

        val usedLicenses = mutableSetOf<SpdxLicense>()
        val libraries = models.map { model ->
            Library(
                uniqueId = "${model.groupId}:${model.artifactId}",
                name = model.name,
                artifactVersion = model.version,
                description = model.description,
                developers = model.developers.map {
                    Developer(
                        name = it.name,
                    )
                },
                licenses = model.licenses.mapNotNull {
                    findLicense(it)
                        ?.also { license -> usedLicenses.add(license) }
                        ?.id
                },
                website = model.url,
            )
        }
        val client = HttpClient.newHttpClient()
        val licenses = usedLicenses.map {
            async(Dispatchers.IO) {
                val request = HttpRequest.newBuilder(URI.create(it.getTxtUrl())).build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                check(response.statusCode() == 200) { "Failed to fetch license text for ${it.fullName}" }
                it.id to License(
                    name = it.fullName,
                    content = response.body(),
                    url = it.getUrl(),
                )
            }
        }.awaitAll().toMap()

        val libs = Libs(
            libraries = libraries,
            licenses = licenses,
        )
        withContext(Dispatchers.IO) {
            Files.writeString(output, JLibs.toJson(libs, jsonStyle))
        }
    }
}

fun main(args: Array<String>) {
    val root = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
    root.setLevel(Level.INFO)

    Main.boot(AboutLibrariesGenerator::class.java, args).main(args)
}
