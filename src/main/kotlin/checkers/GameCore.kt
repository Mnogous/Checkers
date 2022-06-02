package checkers

class GameCore {

    // Создание начальной доски
    private fun createDesk(): Array<Array<Cell>> {
        val array = Array(8){Array(8){Cell()}}
        for (y in 0 until 8) {
            for (x in 0 until 8)  array[y][x] = when {
                y in 0..2 && parity(x, y) -> Cell(Checker(0))
                y in 5..7 && parity(x, y) -> Cell(Checker(1))
                else -> Cell()
            }
        }
        return array
    }

    val gameDesk: Array<Array<Cell>> = createDesk()

    // Ход перемещения
    fun move(x1: Int, y1: Int, x2: Int, y2: Int) {
        gameDesk[y2][x2] = gameDesk[y1][x1]
        gameDesk[y1][x1] = Cell()
        if (gameDesk[y2][x2].checker!!.side == 0 && y2 == 7) gameDesk[y2][x2].checker!!.crown = true
        else if (gameDesk[y2][x2].checker!!.side == 1 && y2 == 0) gameDesk[y2][x2].checker!!.crown = true
    }

    // Ход со съеданием
    fun eat (x1: Int, y1: Int, x2: Int, y2: Int) {
        val eated = cellsToEat(x1, y1, gameDesk[y1][x1].checker!!.side).second
        gameDesk[y2][x2] = gameDesk[y1][x1]
        gameDesk[y1][x1] = Cell()
        for (c in eated) gameDesk[c.second][c.first] = Cell()
        if (gameDesk[y2][x2].checker!!.side == 0 && y2 == 7) gameDesk[y2][x2].checker!!.crown = true
        else if (gameDesk[y2][x2].checker!!.side == 1 && y2 == 0) gameDesk[y2][x2].checker!!.crown = true
    }


    // Проверка, возможны ли ходы для выбранной шашки
    fun checkerCanMove(x: Int, y: Int): Boolean {
        return gameDesk[y][x].isChecker() && if (!gameDesk[y][x].checker!!.crown) if(gameDesk[y][x].checker!!.side != 1)
            y < 7 && ((x > 0 && !gameDesk[y+1][x-1].isChecker()) || (x < 7 && !gameDesk[y+1][x+1].isChecker()))
        else y > 0 && ((x > 0 && !gameDesk[y-1][x-1].isChecker()) || (x < 7 && !gameDesk[y-1][x+1].isChecker()))
        else (y < 7 && ((x > 0 && !gameDesk[y+1][x-1].isChecker()) || (x < 7 && !gameDesk[y+1][x+1].isChecker()))) ||
                (y > 0 && ((x > 0 && !gameDesk[y-1][x-1].isChecker()) || (x < 7 && !gameDesk[y-1][x+1].isChecker())))
    }


    // Список клеток, в которые может перейти текущая шашка
    fun cellsToMove(x: Int, y: Int): List<Pair<Int, Int>> {
        val cells: MutableList<Pair<Int, Int>> = mutableListOf()
        if (gameDesk[y][x].isChecker()) if (!gameDesk[y][x].checker!!.crown) if (gameDesk[y][x].checker!!.side != 1) {
            if (x > 0 && !gameDesk[y+1][x-1].isChecker()) cells.add(Pair(x - 1, y + 1))
            if (x < 7 && !gameDesk[y+1][x+1].isChecker()) cells.add(Pair(x + 1, y + 1))
        } else {
            if (x > 0 && !gameDesk[y-1][x-1].isChecker()) cells.add(Pair(x - 1, y - 1))
            if (x < 7 && !gameDesk[y-1][x+1].isChecker()) cells.add(Pair(x + 1, y - 1))
        }
        else {
            for (i in 1 until 8) {
                if (x - i >= 0 && y + i <= 7 && !gameDesk[y+i][x-i].isChecker()) cells.add(Pair(x - i, y + i))
                if (x + i <= 7 && y + i <= 7 && !gameDesk[y+i][x+i].isChecker()) cells.add(Pair(x + i, y + i))
                if (x - i >= 0 && y - i >= 0 && !gameDesk[y-i][x-i].isChecker()) cells.add(Pair(x - i, y - i))
                if (x + i <= 7 && y - i >= 0 && !gameDesk[y-i][x+i].isChecker()) cells.add(Pair(x + i, y - i))
            }
        }
        return cells
    }


    // Проверка, может ли выбранная шашка что-либо съесть
    fun checkerCanEat(x: Int, y: Int, cSide: Int): Boolean {
        return ((y < 6 && (((x > 1 && gameDesk[y+1][x-1].isChecker() && gameDesk[y+1][x-1].checker!!.side != cSide &&
                !gameDesk[y+2][x-2].isChecker()))
                || ((x < 6 && gameDesk[y+1][x+1].isChecker() && gameDesk[y+1][x+1].checker!!.side != cSide &&
                !gameDesk[y+2][x+2].isChecker()))))
                || (y > 1 && (((x > 1 && gameDesk[y-1][x-1].isChecker() && gameDesk[y-1][x-1].checker!!.side != cSide &&
                !gameDesk[y-2][x-2].isChecker()))
                || ((x < 6 && gameDesk[y-1][x+1].isChecker() && gameDesk[y-1][x+1].checker!!.side != cSide &&
                !gameDesk[y-2][x+2].isChecker())))))
    }


    // Список допустимых ходов со схеданием
    fun cellsToEat(x: Int, y: Int, cSide: Int,
              eated: MutableList<Pair<Int, Int>> = mutableListOf()): Pair<List<Pair<Int, Int>>, List<Pair<Int, Int>>> {
        val cells: MutableList<Pair<Int, Int>> = mutableListOf()
        var currentEated: List<Pair<Int, Int>> = listOf()
        var currentCells: List<Pair<Int, Int>> = listOf()


        if (y < 6 && (x > 1 && gameDesk[y+1][x-1].isChecker() && gameDesk[y+1][x-1].checker!!.side != cSide &&
                    Pair(x - 1, y + 1) !in eated &&
                    !gameDesk[y+2][x-2].isChecker())) {
            cells.add(Pair(x - 2, y + 2))
            eated.add(Pair(x - 1, y + 1))
            if (checkerCanEat(x - 2, y + 2, cSide)) {
                cells += cellsToEat(x - 2, y + 2, cSide, eated).first
                eated += cellsToEat(x - 2, y + 2, cSide, eated).second
            }
        }
        if (eated.size > currentEated.size) {
            currentEated = eated
            currentCells = cells
        }
        if (y < 6 && (x < 6 && gameDesk[y+1][x+1].isChecker() && gameDesk[y+1][x+1].checker!!.side != cSide &&
                    Pair(x + 1, y + 1) !in eated &&
                    !gameDesk[y+2][x+2].isChecker())) {
            cells.add(Pair(x + 2, y + 2))
            eated.add(Pair(x + 1, y + 1))
            if (checkerCanEat(x + 2, y + 2, cSide)) {
                cells += cellsToEat(x + 2, y + 2, cSide, eated).first
                eated += cellsToEat(x + 2, y + 2, cSide, eated).second
            }
        }
        if (eated.size > currentEated.size) {
            currentEated = eated
            currentCells = cells
        }
        if ((y > 1 && x > 1 && gameDesk[y-1][x-1].isChecker() && gameDesk[y-1][x-1].checker!!.side != cSide &&
                    Pair(x - 1, y - 1) !in eated &&
                    !gameDesk[y-2][x-2].isChecker())) {
            cells.add(Pair(x - 2, y - 2))
            eated.add(Pair(x - 1, y - 1))
            if (checkerCanEat(x - 2, y - 2, cSide)) {
                cells += cellsToEat(x - 2, y - 2, cSide, eated).first
                cells += cellsToEat(x - 2, y - 2, cSide, eated).second
            }
        }
        if (eated.size > currentEated.size) {
            currentEated = eated
            currentCells = cells
        }
        if ((y > 1 && x < 6 && gameDesk[y-1][x+1].isChecker() && gameDesk[y-1][x+1].checker!!.side != cSide &&
                    Pair(x + 1, y - 1) !in eated &&
                    !gameDesk[y-2][x+2].isChecker())) {
            cells.add(Pair(x + 2, y - 2))
            eated.add(Pair(x + 1, y - 1))
            if (checkerCanEat(x + 2, y - 2, cSide)) {
                cells += cellsToEat(x + 2, y - 2, cSide, eated).first
                eated += cellsToEat(x + 2, y - 2, cSide, eated).second
            }
        }
        if (eated.size > currentEated.size) {
            currentEated = eated
            currentCells = cells
        }
        return Pair(currentCells, currentEated)
    }


    // Список всех допустимых ходов и съеданий для выбранной стороны
    fun cans(side: Int): Pair<MutableList<Pair<Int, Int>>, MutableList<Pair<Int, Int>>> {
        val cellsEat: MutableList<Pair<Int, Int>> = mutableListOf()
        val cellsMove: MutableList<Pair<Int, Int>> = mutableListOf()

        for (y in gameDesk.indices) for (x in gameDesk[y].indices) {
            if (gameDesk[y][x].isChecker() && gameDesk[y][x].checker!!.side == side) {
                if (checkerCanEat(x, y, gameDesk[y][x].checker!!.side)) cellsEat.add(Pair(x, y))
                if (checkerCanMove(x, y)) cellsMove.add(Pair(x, y))
            }
        }
        return Pair(cellsEat, cellsMove)
    }
}