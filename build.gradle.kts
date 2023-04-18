import de.aaschmid.gradle.plugins.cpd.Cpd


plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building a CLI application
    // You can run your app via task "run": ./gradlew run
    application

    /*
     * Adds tasks to export a runnable jar.
     * In order to create it, launch the "shadowJar" task.
     * The runnable jar will be found in build/libs/projectname-all.jar
     */
    id("com.github.johnrengelman.shadow") version "8.1.0"
    id("org.danilopianini.gradle-java-qa") version "0.44.0"
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

repositories { // Where to search for dependencies
    mavenCentral()
}

dependencies {
    // Suppressions for SpotBugs
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.7.3")

    // JUnit API and testing engine
    val jUnitVersion = "5.9.2"
    // when dependencies share the same version, grouping in a val helps to keep them in sync
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")

    // to import JSON 
    implementation("com.googlecode.json-simple:json-simple:1.1.1")

    // for image manipulation
    implementation("net.coobird:thumbnailator:0.4.6")
}

application {
    // Define the main class for the application.
    mainClass.set("it.unibo.caesena.application.App")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        showStandardStreams = true
    }
}

allprojects {
    tasks.withType<Test> {
        ignoreFailures = true
        useJUnitPlatform()
    }
    tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
        ignoreFailures = true
        reports {
            create("xml") {
                enabled = true
            }
        }
    }
    pmd {
        isIgnoreFailures = true
    }
    cpd {
        isIgnoreFailures = true
    }
    tasks.withType<Cpd> {
        reports {
            xml.required.set(true)
            text.required.set(true)
        }
        language = "java"
        minimumTokenCount = 50
        ignoreFailures = true
        source = sourceSets["main"].allJava
    }
    checkstyle {
        isIgnoreFailures = true
    }
}

typealias QAInfoContainer = Iterable<QAInfo>

interface QAInfo {
    val checker: String
    val lines: IntRange
    val details: String
    val file: String
    val blamedTo: Set<String>
}

fun List<String>.commandOutput(): String {
    val process = ProcessBuilder(this).redirectOutput(ProcessBuilder.Redirect.PIPE).start()
    process.waitFor(1, TimeUnit.MINUTES)
    return process.inputStream.bufferedReader().readText()
}

val authorMatch = Regex("^author\\s+(.+)$")
fun blameFor(file: String, lines: IntRange): Set<String> =
    listOf("git", "blame", "-L", "${lines.start},${lines.endInclusive}", "-p", file)
        .commandOutput().lines()
        .flatMap { line -> authorMatch.matchEntire(line)?.destructured?.toList() ?: emptyList() }
        .toSet()
        .takeIf { it.isNotEmpty() }
        ?: throw IllegalStateException(
            "Unable to assign anything with: 'git blame -L ${lines.start},${lines.endInclusive} -p $file'"
        )

data class QAInfoForChecker(
    override val checker: String,
    override val file: String,
    override val lines: IntRange = 1..File(file).readText().lines().size,
    override val details: String = "",
    private val blamed: Set<String>? = null,
) : QAInfo {
    override val blamedTo: Set<String> = blamed ?: blameFor(file, lines)
}

fun org.w3c.dom.NamedNodeMap.iterator() = object : Iterator<org.w3c.dom.Node> {
    var index = 0
    override fun hasNext() = index < length
    override fun next() = item(index++)
}

operator fun org.w3c.dom.Node.get(attribute: String, orElse: String): String = get(attribute) { orElse }

operator fun org.w3c.dom.Node.get(
    attribute: String,
    onFailure: () -> String = {
        throw IllegalArgumentException(
            "No attribute '$attribute' in $this. Available attributes: ${attributes.iterator().asSequence().toList()}"
        )
    }
): String = attributes?.getNamedItem(attribute)?.textContent ?: onFailure()

fun org.w3c.dom.Node.childrenNamed(name: String): List<org.w3c.dom.Node> =
        childNodes.toIterable().filter { it.nodeName == name }

class PmdQAInfoExtractor(root: org.w3c.dom.Element) : QAInfoContainer by (
    root.childNodes.toIterable()
        .asSequence()
        .filter { it.nodeName == "file" }
        .flatMap { file -> file.childrenNamed("violation").map{ file to it } }
        .map { (file, violation) ->
            QAInfoForChecker(
                "Sub-optimal Java object-orientation",
                file["name"],
                violation["beginline"].toInt()..violation["endline"].toInt(),
                "[${violation["ruleset"].toUpperCase()}] ${violation.textContent.trim()}",
            )
        }
        .asIterable()
)

class CpdQAInfoExtractor(root: org.w3c.dom.Element) : QAInfoContainer by (
    root.childNodes.toIterable()
        .asSequence()
        .filter { it.nodeName == "duplication" }
        .map { duplication ->
            val files = duplication.childrenNamed("file")
            val filePaths = files.map { it["path"] }
            val lines = duplication["lines"].toInt()
            val shortFiles = files.map { "${File(it["path"]).name}:${it["line"]}" }
            val ranges = files.map {
                val begin = it["line"].toInt()
                begin..(begin + lines)
            }
            val blamed = filePaths.zip(ranges).flatMap { (file, lines) -> blameFor(file, lines) }.toSet()
            val description = "Duplication of $lines lines" +
                " and ${duplication["tokens"]} tokens across ${filePaths.toSet().size}" +
                " files: ${shortFiles.joinToString(prefix = "", postfix = "")}"
            QAInfoForChecker(
                "Duplications and violations of the DRY principle",
                files.first()["path"],
                ranges.first(),
                description,
                blamed
            )
        }
        .asIterable()
)

class CheckstyleQAInfoExtractor(root: org.w3c.dom.Element) : QAInfoContainer by (
    root.childNodes.toIterable()
        .asSequence()
        .filter { it.nodeName == "file" }
        .flatMap { file -> file.childrenNamed("error").map{ file["name"] to it } }
        .map { (file, error) ->
            val line = error["line"].toInt()
            val lineRange = line..line
            QAInfoForChecker("Style errors", file, lineRange, error["message"])
        }
        .asIterable()
)

class SpotBugsQAInfoExtractor(root: org.w3c.dom.Element) : QAInfoContainer by (
    root.childNodes.let { childNodes ->
        val sourceDirs = childNodes.toIterable()
            .filter { it.nodeName == "Project" }
            .first()
            .childrenNamed("SrcDir")
            .map { it.textContent.trim() }
            .asSequence()
        childNodes.toIterable()
            .asSequence()
            .filter { it.nodeName == "BugInstance" }
            .mapNotNull { bugDescriptor ->
                val sourceLineDescriptor = bugDescriptor.childrenNamed("SourceLine").first()
                val category = bugDescriptor["category"].takeUnless { it == "STYLE" } ?: "UNSAFE"
                val startLine = sourceLineDescriptor["start", "1"].toInt()
                val endLine = sourceLineDescriptor["end", Integer.MAX_VALUE.toString()].toInt()
                val candidateFile = sourceLineDescriptor.get("relSourcepath") {
                    sourceLineDescriptor["sourcepath"]
                }
                val actualFile = sourceDirs.flatMap { File(it).walkTopDown() }
                    .map { it.absolutePath }
                    .first { candidateFile in it }
                actualFile.takeIf { it.isNotBlank() }?.let {
                    QAInfoForChecker(
                        "Potential bugs",
                        actualFile,
                        startLine..endLine,
                        "[$category] ${bugDescriptor.childrenNamed("LongMessage").first().textContent.trim()}",
                    )
                }
            }
            .asIterable()
    }
)

fun org.w3c.dom.NodeList.toIterable() = Iterable {
    object : Iterator<org.w3c.dom.Node> {
        var index = 0;
        override fun hasNext(): Boolean = index < length - 1
        override fun next(): org.w3c.dom.Node = item(index++)
    }
}

fun String.endingWith(postfix: String): String = takeIf { endsWith(postfix) } ?: "$this$postfix"

allprojects {
    tasks.register("blame") {
        val dependencies = tasks.withType<org.gradle.api.plugins.quality.Checkstyle>() +
                tasks.withType<org.gradle.api.plugins.quality.Pmd>() +
                tasks.withType<com.github.spotbugs.snom.SpotBugsTask>() +
                tasks.withType<de.aaschmid.gradle.plugins.cpd.Cpd>()
        dependsOn(dependencies)
        val identifier = if (project == rootProject) "" else "-${project.name}"
        val output = "${project.buildDir}${File.separator}blame$identifier.md"
        outputs.file(output)
        doLast {
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            val xmlParser = factory.newDocumentBuilder();
            val errors = dependencies
                .flatMap { task -> task.outputs.files.asIterable().filter { it.exists() && it.extension == "xml" } }
                .flatMap<File, QAInfo> {
                    val root: org.w3c.dom.Element = xmlParser.parse(it).documentElement
                    when (root.tagName) {
                        "pmd" -> PmdQAInfoExtractor(root)
                        "pmd-cpd" -> CpdQAInfoExtractor(root)
                        "checkstyle" -> CheckstyleQAInfoExtractor(root)
                        "BugCollection" -> SpotBugsQAInfoExtractor(root)
                        else -> emptyList<QAInfo>().also { println("Unknown root type ${root.tagName}")}
                    }
                }
                .distinct()
            val errorsByStudentByChecker: Map<String, Map<String, List<QAInfo>>> = errors
                .flatMap { error -> error.blamedTo.map { it to error } }
                .groupBy { it.first }
                .mapValues { (_, errors) -> errors.map { it.second }.groupBy { it.checker } }
            val report = errorsByStudentByChecker.map { (student, errors) ->
                """
                |# $student
                |
                |${errors.map { it.value.size }.sum()} violations
                |${errors.map { (checker, violations) ->
                    """
                    |## $checker: ${violations.size} mistakes
                    ${ violations.sortedBy { it.details }
                        .joinToString("") {
                            val fileName = File(it.file).name
                            "|* ${it.details.endingWith(".")} In: $fileName@[${it.lines}]\n"
                        }.trimEnd()
                    }
                    """
                }.joinToString(separator = "", prefix = "", postfix = "")}
                |
                """.trimMargin()
            }.joinToString(separator = "", prefix = "", postfix = "")
            file(output).writeText(report)
        }
    }
}
