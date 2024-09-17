import java.io.File
import kotlin.reflect.full.safeCast


enum class VersionBumpType { RELEASE, FIX, MANUAL }

enum class InputType { AUTOMATIC, MANUAL }

const val PROPERTY_MODULE_KEY = "modules"
const val MODULE_SEPARATOR = ","
const val PROPERTY_TYPE_KEY = "type"
const val VERSIONS_FILE_PATH = "buildSrc/src/main/kotlin/Versions.kt"
const val CHECK_MODULES_SCRIPT_PATH = "buildSrc/scripts/check_modules.sh"
const val CHECK_MODULES_OUTPUT_PATH = "buildSrc/scripts/check_modules_output.txt"
const val README_FILE_PATH = "ReadMe.md"
const val VERSION_SUFFIX = "_VERSION"
const val MODULE_CHANGED_SUFFIX = "_MODULE_CHANGED"
const val CHART_DELIMETER = "|"

// note: Must match names in Version.kt
enum class Version(var chartPosition: Int? = null) {
    BOM(1), FOUNDATION(), CORE(2), SIGN(3),
    NOTIFY(4), WEB_3_WALLET(5), WEB_3_MODAL(6), WC_MODAL(7), MODAL_CORE();

    val key: String = name + VERSION_SUFFIX
}

data class BumpVersionResult(val versionFileText: String, val readmeFileText: String)

fun bumpVersion(properties: Map<String, Any>, versionBumpType: VersionBumpType, initialValue: String, versionKey: String): String {
    val suffixRegex = Regex("-(alpha|beta|rc)(\\d+)$")
    val suffixMatch = suffixRegex.find(initialValue)

    return if (suffixMatch != null) {
        // Handle versions with suffixes like -alpha, -beta, -rc
        val (suffix, suffixNumber) = suffixMatch.destructured
        val baseVersion = initialValue.removeSuffix("-$suffix$suffixNumber")
        val incrementedSuffixNumber = suffixNumber.toInt() + 1
        "$baseVersion-$suffix${incrementedSuffixNumber.toString().padStart(2, '0')}"
    } else {
        val (major, minor, patch) = initialValue.split(".").map { it.toInt() }
        when (versionBumpType) {
            VersionBumpType.RELEASE -> "$major.${minor + 1}.0"
            VersionBumpType.FIX -> "$major.$minor.${patch + 1}"
            VersionBumpType.MANUAL -> properties[versionKey]?.run(String::class::safeCast)?.run { this } ?: initialValue
        }
    }
}

fun getBumpType(properties: Map<String, Any>): VersionBumpType {
    val type = properties[PROPERTY_TYPE_KEY]?.run(String::class::safeCast)?.run { this } ?: throw Exception("No bump type specified.")
    return VersionBumpType.valueOf(type.uppercase())
}

fun parseChangedModules(properties: Map<String, Any>): Map<String, Any> {
    val outputFile = File(CHECK_MODULES_OUTPUT_PATH)
    val modulesChangeStatus = outputFile.readLines()
        .associate {
            val (key, value) = it.split('=')
            key to value.toBoolean()
        }

    modulesChangeStatus.forEach { (key, value) -> println("$key = $value") }
    outputFile.delete()

    return mutableMapOf<String, Any>()
        .apply {
            putAll(properties)
            modulesChangeStatus
                .filter { it.value }
                .map { it.key.removeSuffix(MODULE_CHANGED_SUFFIX) }
                .joinToString(MODULE_SEPARATOR)
                .let { put(PROPERTY_MODULE_KEY, it) }
        }
}

fun bumpVersions(
    properties: Map<String, Any>,
    versionBumpType: VersionBumpType,
    inputType: InputType,
    versionsFilePath: String = VERSIONS_FILE_PATH,
    readmeFilePath: String = README_FILE_PATH,
): BumpVersionResult {

    val versions: Map<Version, Boolean> = parseInput(properties, inputType)
    val versionsFile = File(versionsFilePath)
    val readmeFile = File(readmeFilePath)

    if (!versionsFile.exists()) throw Exception("File not found: ${versionsFile.absolutePath}")
    if (!readmeFile.exists()) throw Exception("File not found: ${readmeFile.absolutePath}")

    var versionsText = versionsFile.readText()
    var readmeText = readmeFile.readText()

    val firstDataRowPattern = Regex("""\|.*\d+\.\d+\.\d+.*?\|""")
    val oldFirstRow = firstDataRowPattern.find(readmeText)?.groups?.first()?.value ?: throw Exception("No first row found in readme")
    var newFirstRow = oldFirstRow

    versions.filter { it.value }.map { it.key }.forEach { version ->
        val versionKey = version.key
        val regex = """$versionKey = "(\S+)"""".toRegex()
        val matchResult = regex.find(versionsText)
        val currentValue = matchResult?.groups?.get(1)?.value
        if (currentValue == null) throw Exception("No match for $versionKey")

        val bumpedVersion = bumpVersion(properties, versionBumpType, currentValue, versionKey)
        versionsText = versionsText.replace("$versionKey = \"$currentValue\"", "$versionKey = \"$bumpedVersion\"")
        newFirstRow = newFirstRow.replaceChartVersion(version, currentValue, bumpedVersion)
    }

    return BumpVersionResult(versionsText, readmeText.replace(oldFirstRow, newFirstRow + '\n' + oldFirstRow))
}

fun String.replaceChartVersion(version: Version, currentValue: String, bumpedValue: String): String {
    return if (version.chartPosition == null) this
    else this.split(CHART_DELIMETER)
        .toMutableList()
        .apply { this[version.chartPosition!!] = this[version.chartPosition!!].replace(currentValue, bumpedValue) }
        .joinToString(CHART_DELIMETER)
}

fun writeFiles(bumpVersionResult: BumpVersionResult, versionsFilePath: String = VERSIONS_FILE_PATH, readmeFilePath: String = README_FILE_PATH) {
    val versionsFile = File(versionsFilePath)
    val readmeFile = File(readmeFilePath)

    versionsFile.writeText(bumpVersionResult.versionFileText)
    readmeFile.writeText(bumpVersionResult.readmeFileText)
}


fun parseInput(properties: Map<String, Any>, inputType: InputType): Map<Version, Boolean> = when (inputType) {
    InputType.AUTOMATIC -> parseAutomaticInput(properties)
    InputType.MANUAL -> parseManualInput(properties)
}

// ./gradlew fixBump -Pmodules=FOUNDATION,CORE,SIGN,NOTIFY,WEB_3_WALLET,WEB_3_MODAL,WC_MODAL,MODAL_CORE
// ./gradlew releaseBump -Pmodules=FOUNDATION
fun parseAutomaticInput(properties: Map<String, Any>): Map<Version, Boolean> {
    val modules = properties[PROPERTY_MODULE_KEY]?.run(String::class::safeCast)?.run { this.uppercase().split(MODULE_SEPARATOR) } ?: throw Exception("No modules specified.")
    var versions = Version.values().associate { version -> version to modules.contains(version.name) }
    return ensureModuleDependenciesWhenBumping(versions)
}

fun Map<Version, Boolean>.bumpBOM(): Map<Version, Boolean> =
    entries.associate { (k, v) ->
        k to when (k) {
            Version.BOM -> true
            else -> v
        }
    }

fun Map<Version, Boolean>.bumpAllModules(): Map<Version, Boolean> = mapValues { true }
fun Map<Version, Boolean>.bumpCoreDependantModules(): Map<Version, Boolean> = entries.associate { (k, v) ->
    k to when (k) {
        Version.FOUNDATION -> v
        else -> true
    }
}

fun Map<Version, Boolean>.bumpModalCoreDependantModules(): Map<Version, Boolean> = entries.associate { (k, v) ->
    k to when (k) {
        Version.WEB_3_MODAL, Version.WC_MODAL -> true
        else -> v
    }
}

fun Map<Version, Boolean>.bumpSignDependantModules(): Map<Version, Boolean> = entries.associate { (k, v) ->
    k to when (k) {
        Version.WEB_3_WALLET, Version.WEB_3_MODAL, Version.WC_MODAL -> true
        else -> v
    }
}

fun Map<Version, Boolean>.bumpAuthDependantModules(): Map<Version, Boolean> = entries.associate { (k, v) ->
    k to when (k) {
        Version.WEB_3_WALLET -> true
        else -> v
    }
}


fun ensureModuleDependenciesWhenBumping(parsedVersions: Map<Version, Boolean>): Map<Version, Boolean> {
    val versions = parsedVersions.bumpBOM()

    return when {
        versions[Version.FOUNDATION] == true -> versions.bumpAllModules()
        versions[Version.CORE] == true -> versions.bumpCoreDependantModules()
        else -> {
            versions
                .run { if (this[Version.SIGN] == true) bumpSignDependantModules() else this }
                .run { if (this[Version.MODAL_CORE] == true) bumpModalCoreDependantModules() else this }
        }
    }
}

// ./gradlew manualBump -PBOM=1.0.0 -PFOUNDATION=1.0.0 -PCORE=1.0.0 -PSIGN=1.0.0 -PNOTIFY=1.0.0 -PWEB_3_WALLET=1.0.0 -PWEB_3_MODAL=1.0.0 -PWC_MODAL=1.0.0 -PMODAL_CORE=1.0.0
fun parseManualInput(properties: Map<String, Any>): Map<Version, Boolean> {
    return Version.values().associate { version ->
        version to (properties[version.name]?.run(String::class::safeCast)?.run { true } ?: false)
    }
}