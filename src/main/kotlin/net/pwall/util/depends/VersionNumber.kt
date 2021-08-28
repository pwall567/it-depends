package net.pwall.util.depends

class VersionNumber(val rawFormat: Boolean, val rawString: String, val major: Int, val minor: Int,
        val patch: Int? = null, val snapshot: Boolean = false) : Comparable<VersionNumber> {

    override fun compareTo(other: VersionNumber): Int {
        if (rawFormat || other.rawFormat)
            return toString().compareTo(other.toString())
        if (major < other.major)
            return -1
        if (major > other.major)
            return 1
        if (minor < other.minor)
            return -1
        if (minor > other.minor)
            return 1
        if (patch == null && other.patch != null)
            return -1
        if (patch != null && other.patch == null)
            return 1
        if (patch != null && other.patch != null && patch < other.patch)
            return -1
        if (patch != null && other.patch != null && patch > other.patch)
            return 1
        if (snapshot && !other.snapshot)
            return -1
        if (!snapshot && other.snapshot)
            return 1
        return 0
    }

    override fun equals(other: Any?) = this === other || other is VersionNumber && rawFormat == other.rawFormat &&
            rawString == other.rawString && major == other.major && minor == other.minor && patch == other.patch &&
            snapshot == other.snapshot

    override fun hashCode() = (if (rawFormat) 1 else 0) xor rawString.hashCode() xor major xor minor xor
            (patch ?: 0) xor (if (snapshot) 1 else 0)

    override fun toString() = if (rawFormat) rawString else
            "$major.$minor${if (patch != null) ".$patch" else ""}${if (snapshot) SNAPSHOT else ""}"

    companion object {

        const val SNAPSHOT = "-SNAPSHOT"
        val regex = Regex("^[0-9]+\\.[0-9]+(\\.[0-9]+)?(-SNAPSHOT)?$")

        fun of(str: String): VersionNumber {
            if (!regex.containsMatchIn(str))
                return VersionNumber(true, str, 0, 0)
            val snapshot = str.endsWith(SNAPSHOT)
            val parts = (if (snapshot) str.dropLast(SNAPSHOT.length) else str).split('.')
            return VersionNumber(false, "", parts[0].toInt(), parts[1].toInt(),
                    if (parts.size > 2) parts[2].toInt() else null, snapshot)
        }

    }

}
