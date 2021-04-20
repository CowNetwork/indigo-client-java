package network.cow.indigo.client.spigot.permission

/**
 * @author Tobias Büser
 */
class PermissionList(permissions: List<String>) {

    private val regexes = mutableMapOf<String, Regex>()

    init {
        permissions.forEach {
            regexes[it] = Regex(it.toPermissionRegexString())
        }
    }

    fun hasPermission(perm: String): Boolean {
        if (regexes.containsKey(perm)) return true
        for (regex in regexes.values) {
            if (regex.matches(perm)) {
                return true
            }
        }
        return false
    }

}

private val PERM_REGEX = Regex("^-?(?:\\w+|\\*)(?:\\.(\\w+|\\*))*\$")

private fun String.toPermissionRegexString(): String {
    if (!this.isPermission) {
        return this
    }

    val regexString: String
    if (this.endsWith(".*")) {
        regexString = this.dropLast(2)
        return regexString.toPermissionRegexString() + "(?:\\.(\\w+|\\*))+"
    }
    regexString = this.replace("*", "(?:\\w+|\\*)")
    return regexString.replace(".", "\\.")
}

private val String.isPermission: Boolean
    get() = PERM_REGEX.matches(this)
