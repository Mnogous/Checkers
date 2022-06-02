package checkers

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWCursorPosCallback

class Cursor(private val gameCore: GameCore): GLFWCursorPosCallback() {
    private val gameDesk = gameCore.gameDesk
    public var current: Pair<Int, Int>? = null
    public var ways: List<Pair<Int, Int>> = listOf()
    public var turn = 0
    override fun invoke(window: Long, xPos: Double, yPos: Double) {

        val x = (xPos / 80).toInt()
        val y = 7 - (yPos / 80).toInt()

        var cans = gameCore.cans(turn)

        // Выбор активной шашки
        if (gameDesk[y][x].isChecker() && gameCore.checkerCanEat(x, y, gameDesk[y][x].checker!!.side) &&
            GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) {
            gameDesk[y][x].checker!!.current = true
            if (current != null && current != Pair(x, y))
                gameDesk[current!!.second][current!!.first].checker!!.current = false
            current = Pair(x, y)
            ways = gameCore.cellsToEat(x, y, turn).first
        } else if (Pair(x, y) in cans.second && cans.first.isEmpty() &&
            GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) {
            gameDesk[y][x].checker!!.current = true
            if (current != null && current != Pair(x, y))
                gameDesk[current!!.second][current!!.first].checker!!.current = false
            current = Pair(x, y)
            ways = gameCore.cellsToMove(x, y)
        }

            // Снятие активной шашки на ПКМ
        if (current != null &&
            GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS) {
            gameDesk[current!!.second][current!!.first].checker!!.current = false
            current = null
            ways = listOf()
        }

        // Выбор шашки для хода
        if (Pair(x, y) in ways &&
            GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) {
            if (current in cans.first) gameCore.eat(current!!.first, current!!.second, x, y)
            else gameCore.move(current!!.first, current!!.second, x, y)
            gameDesk[y][x].checker!!.current = false
            current = null
            ways = listOf()
            turn = 1
        }


        // Случайные ходы
        if (turn == 1) {
            cans = gameCore.cans(turn)
            if (cans.first.isNotEmpty()) {
                val cur = cans.first.random()
                val way = gameCore.cellsToEat(cur.first, cur.second, turn).first.random()
                gameCore.eat(cur.first, cur.second, way.first, way.second)
            } else if (cans.first.isEmpty() && cans.second.isNotEmpty()) {
                val cur = cans.second.random()
                val way = gameCore.cellsToMove(cur.first, cur.second).random()
                gameCore.move(cur.first, cur.second, way.first, way.second)
            }
            turn = 0
        }
    }
}