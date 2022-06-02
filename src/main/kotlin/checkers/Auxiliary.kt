package checkers

fun parity(x: Int, y: Int) = (x + y) % 2 == 0

class Cell (public val checker: Checker? = null) {
    fun isChecker() = checker != null
    fun isCurrent() = checker!!.current
}

// side 0 -- белые, 1 -- чёрные
class Checker (public val side: Int, public var crown: Boolean = false, public var current: Boolean = false)