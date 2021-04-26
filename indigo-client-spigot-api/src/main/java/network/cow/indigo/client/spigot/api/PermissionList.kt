package network.cow.indigo.client.spigot.api

private val PERM_REGEX = Regex("^-?(?:\\w+|\\*)(?:\\.(\\w+|\\*))*\$")

/**
 * @author Tobias Büser
 */
class PermissionList(permissions: List<String>) : Iterable<String> {

    private val regexes = mutableMapOf<String, Regex>()

    private val positivePermissions: List<String>
    private val negativePermissions: List<String>

    private val size: Int
        get() = regexes.size

    init {
        permissions.forEach {
            regexes[it] = Regex(it.toPermissionRegexString())
        }

        positivePermissions = regexes.keys.filter { !it.isNegative }.sortedWith(compareBy(
            { it.depth }, { it.wildcardCount * -1 }
        ))
        negativePermissions = regexes.keys.filter { it.isNegative }.sortedWith(compareBy(
            { it.depth }, { it.wildcardCount * -1 }
        ))
    }

    fun negatesPermission(perm: String) = hasPermissionRaw("-$perm", true)

    fun hasPermission(perm: String): Boolean {
        if (regexes.containsKey(perm)) return true

        // first check for negative permissions
        if (negatesPermission(perm)) return false

        return hasPermissionRaw(perm)
    }

    /**
     * Checks if the list has a [Regex] that matches the literal [perm].
     */
    fun hasPermissionRaw(perm: String, negative: Boolean = false): Boolean {
        if (regexes.containsKey(perm)) return true
        val permissionsToCheck = if (negative) negativePermissions else positivePermissions

        for (permissionKey in permissionsToCheck) {
            val regex = regexes[permissionKey] ?: continue
            if (regex.matches(perm)) {
                return true
            }
        }
        return false
    }

    /**
     * Compacts the permissions by checking if one permission
     * either negates or is more general than other permissions.
     * In short: Remove all unnecessary permissions.
     */
    fun compacted() = compacted(0)

    private fun compacted(byIndex: Int): PermissionList {
        if (this.size == 0 || byIndex >= this.size) return this
        val copy = this.toMutableList()
        val elem = copy[byIndex]
        val elemPermissionList = PermissionList(listOf(elem))

        copy.removeIf {
            if (it == elem) return@removeIf false
            if (it.isNegative) {
                return@removeIf elemPermissionList.hasPermissionRaw(it, true)
            }
            return@removeIf elemPermissionList.hasPermission(it) || elemPermissionList.negatesPermission(it)
        }
        return PermissionList(copy).compacted(byIndex + 1)
    }

    fun mergeWith(other: PermissionList, superior: Boolean = false): PermissionList {
        if (!superior) {
            return PermissionList(this + other)
        }

        val mergedList = this.toMutableList()

        mergedList.removeIf {
            if (it.isNegative) {
                return@removeIf other.hasPermission(it.drop(1))
            }
            return@removeIf other.negatesPermission(it)
        }

        mergedList += other
        return PermissionList(mergedList)
    }

    /**
     * Sorted iterator.
     */
    override fun iterator(): Iterator<String> {
        return (negativePermissions + positivePermissions).iterator()
    }

    override fun toString(): String {
        return regexes.keys.toString()
    }

}

private val String.isNegative: Boolean
    get() = this.startsWith("-")

private val String.depth: Int
    get() = this.count { it == '.' }

private val String.wildcardCount: Int
    get() = this.count { it == '*' }

private fun String.toPermissionRegexString(): String {
    if (!this.isPermission) {
        return this
    }
    if (this.isNegative) {
        return "-" + this.drop(1).toPermissionRegexString()
    }

    if (this == "*") {
        return "(?:\\w+|\\*)" + "(?:\\.(\\w+|\\*))+"
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
