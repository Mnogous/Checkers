package checkers

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class GraphicsDisplay(private val width: Int, private val height: Int, private val gameCore: GameCore,
                      private val cursor: Cursor) {
    // The window handle
    private var window: Long = 0
    fun run() {
        println("Hello LWJGL " + Version.getVersion() + "!")
        init()
        loop()

        // Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window)
        GLFW.glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)!!.free()
    }

    private fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        check(GLFW.glfwInit()) { "Unable to initialize GLFW" }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints() // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE) // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE) // the window will be resizable

        // Create the window
        window = GLFW.glfwCreateWindow(width, height, "Шашки", MemoryUtil.NULL, MemoryUtil.NULL)
        if (window == MemoryUtil.NULL) throw RuntimeException("Failed to create the GLFW window")

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        GLFW.glfwSetCursorPosCallback(window, cursor)

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            GLFW.glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())

            // Center the window
            GLFW.glfwSetWindowPos(
                window,
                (vidmode!!.width() - pWidth[0]) / 2,
                (vidmode.height() - pHeight[0]) / 2
            )
        }

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window)
        // Enable v-sync
        GLFW.glfwSwapInterval(1)

        // Make the window visible
        GLFW.glfwShowWindow(window)
    }

    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities()

        // Set the clear color
        GL11.glClearColor(0.3f, 0.3f, 1.0f, 0.0f)

        fun fCoord(coord: Int): Float = -1.0f + coord.toFloat() / 4

        fun drawCell(x: Int, y: Int, c: Int = -1) {
            if (parity(x, y)) glColor3d(0.82, 0.55, 0.28) else glColor3d(1.0, 0.81, 0.62)
            when (c) {
                0 -> glColor4d(0.07, 0.51, 0.67, 0.6) // Возможен ход
                1 -> glColor4d(0.33, 0.53, 0.14, 0.6) // Выбранная шашка
                2 -> glColor4d(0.99, 0.23, 0.23, 0.6) // Шашка для съедания
            }
            glBegin(GL_TRIANGLE_STRIP)
            glVertex2f(fCoord(x), fCoord(y))
            glVertex2f(fCoord(x) + 1, fCoord(y))
            glVertex2f(fCoord(x), fCoord(y) + 1)
            glVertex2f(fCoord(x) + 1, fCoord(y) + 1)
            glEnd()
        }

        fun drawChecker(x: Int, y: Int, c: Int) {
            val x = fCoord(x) + 0.125f
            val y = fCoord(y) + 0.125f
            val r = fCoord(1) / 7
            if (c == 1) glColor3f(0.0f,0.0f,0.0f)
                else glColor3f(1.0f,1.0f,1.0f)

            glBegin(GL_TRIANGLE_FAN)
            glVertex2f(x, y)
            for (i in 0..360) glVertex2d(x + Math.sin(i.toDouble()) * r, y + Math.cos(i.toDouble()) * r)
            glEnd()
        }


        fun drawCrown(x: Int, y: Int, c: Int) {
            val x = fCoord(x) + 0.125f
            val y = fCoord(y) + 0.125f
            val r = fCoord(1) / 14
            if (c == 0) glColor3f(0.0f,0.0f,0.0f)
            else glColor3f(1.0f,1.0f,1.0f)

            glBegin(GL_TRIANGLE_FAN)
            glVertex2f(x, y)
            for (i in 0..360) glVertex2d(x + Math.sin(i.toDouble()) * r, y + Math.cos(i.toDouble()) * r)
            glEnd()
        }


        fun drawAll(gameDesk: Array<Array<Cell>>){
            val cans = gameCore.cans(cursor.turn)
            for (y in gameDesk.indices) {
                for (x in gameDesk[y].indices) {
                    drawCell(x, y)
                    if (Pair(x, y) in cursor.ways) drawCell(x, y, 0) // Допустимые ходы для текущей шашки

                    if (gameDesk[y][x].isChecker()) {
                        if (gameDesk[y][x].checker!!.current) drawCell(x, y, 1) // Поле под текущей шашкой

                        //if (cursor.current == null && checkerCanMove(x, y, arr)) drawCell(x, y, 0) //Допустимые шашки
                        if (cursor.current == null && Pair(x, y) in cans.first)
                            drawCell(x, y, 0)
                        else if (cans.first.isEmpty() && cursor.current == null && Pair(x, y) in cans.second)
                            drawCell(x, y, 0)


                        drawChecker(x, y, gameDesk[y][x].checker!!.side) // Отрисовка шашке
                        if (gameDesk[y][x].checker!!.crown) drawCrown(x, y, gameDesk[y][x].checker!!.side)
                    }
                }
            }
        }

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!GLFW.glfwWindowShouldClose(window)) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            drawAll(gameCore.gameDesk)


            GLFW.glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            GLFW.glfwPollEvents()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val gameCore = GameCore()
            val cursor = Cursor(gameCore)
            val graphicsDisplay = GraphicsDisplay(640, 640, gameCore, cursor)
            graphicsDisplay.run()
        }
    }
}