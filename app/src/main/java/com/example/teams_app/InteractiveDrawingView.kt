package com.example.teams_app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.InputType
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class InteractiveDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Drawing mode enum
    enum class Mode {
        DRAW, RECTANGLE, CIRCLE, LINE, TEXT
    }

    // Current drawing path
    private val currentPath = Path()

    // Collection of all drawn elements
    private val paths = mutableListOf<DrawnElement>()

    // Current paint settings
    private var currentPaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    // Available colors for user selection
    private val colors = listOf(
        Color.BLACK,
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.CYAN,
        Color.MAGENTA
    )

    // Current selected color index
    private var currentColorIndex = 0

    // Current drawing mode
    private var currentMode = Mode.DRAW

    // Drawing coordinates
    private var startX = 0f
    private var startY = 0f
    private var lastX = 0f
    private var lastY = 0f

    // Background paint
    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#F5F5F5")
    }

    // Text to add when in TEXT mode
    private var textToAdd: String? = null

    init {
        // Set background color
        setBackgroundColor(Color.WHITE)
    }

    // Element classes to store different types of drawn elements
    sealed class DrawnElement(val paint: Paint)

    class PathElement(val path: Path, paint: Paint) : DrawnElement(paint)

    class RectElement(val rect: RectF, paint: Paint) : DrawnElement(paint)

    class CircleElement(val centerX: Float, val centerY: Float, val radius: Float, paint: Paint) : DrawnElement(paint)

    class LineElement(val startX: Float, val startY: Float, val endX: Float, val endY: Float, paint: Paint) : DrawnElement(paint)

    class TextElement(val text: String, val x: Float, val y: Float, paint: Paint) : DrawnElement(paint)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw all saved elements
        for (element in paths) {
            when (element) {
                is PathElement -> canvas.drawPath(element.path, element.paint)
                is RectElement -> canvas.drawRect(element.rect, element.paint)
                is CircleElement -> canvas.drawCircle(element.centerX, element.centerY, element.radius, element.paint)
                is LineElement -> canvas.drawLine(element.startX, element.startY, element.endX, element.endY, element.paint)
                is TextElement -> canvas.drawText(element.text, element.x, element.y, element.paint)
            }
        }

        // Draw current path or shape being created
        when (currentMode) {
            Mode.DRAW -> canvas.drawPath(currentPath, currentPaint)
            Mode.RECTANGLE -> {
                val left = minOf(startX, lastX)
                val top = minOf(startY, lastY)
                val right = maxOf(startX, lastX)
                val bottom = maxOf(startY, lastY)
                canvas.drawRect(left, top, right, bottom, currentPaint)
            }
            Mode.CIRCLE -> {
                val radius = calculateDistance(startX, startY, lastX, lastY)
                canvas.drawCircle(startX, startY, radius, currentPaint)
            }
            Mode.LINE -> {
                canvas.drawLine(startX, startY, lastX, lastY, currentPaint)
            }
            Mode.TEXT -> {
                // Just show position indicator for text
                if (textToAdd == null) {
                    val indicatorPaint = Paint(currentPaint)
                    indicatorPaint.strokeWidth = 2f
                    canvas.drawLine(startX - 10, startY, startX + 10, startY, indicatorPaint)
                    canvas.drawLine(startX, startY - 10, startX, startY + 10, indicatorPaint)
                }
            }
        }

        // Draw tools panel at the top
        drawToolsPanel(canvas)

        // Draw color palette at the bottom
        drawColorPalette(canvas)
    }

    private fun drawToolsPanel(canvas: Canvas) {
        val toolBoxSize = 80f
        val startX = 20f
        val startY = 20f

        // Tool backgrounds
        val toolBackgroundPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val toolBorderPaint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val modes = listOf(Mode.DRAW, Mode.LINE, Mode.RECTANGLE, Mode.CIRCLE, Mode.TEXT)
        val toolIcons = listOf("✏️", "╱", "□", "○", "T")

        modes.forEachIndexed { index, mode ->
            val left = startX + (index * (toolBoxSize + 10))
            val rect = RectF(left, startY, left + toolBoxSize, startY + toolBoxSize)

            // Draw tool background
            canvas.drawRect(rect, toolBackgroundPaint)

            // Highlight selected tool
            if (mode == currentMode) {
                val highlightPaint = Paint().apply {
                    color = colors[currentColorIndex]
                    style = Paint.Style.STROKE
                    strokeWidth = 6f
                }
                canvas.drawRect(rect, highlightPaint)
            } else {
                canvas.drawRect(rect, toolBorderPaint)
            }

            // Draw tool icon
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 40f
                textAlign = Paint.Align.CENTER
            }
            val textX = left + toolBoxSize / 2
            val textY = startY + toolBoxSize / 2 + 15 // Adjusted for text baseline
            canvas.drawText(toolIcons[index], textX, textY, textPaint)
        }

        // Clear button
        val clearX = startX + (modes.size * (toolBoxSize + 10))
        val clearRect = RectF(clearX, startY, clearX + toolBoxSize + 40, startY + toolBoxSize)
        canvas.drawRect(clearRect, toolBackgroundPaint)
        canvas.drawRect(clearRect, toolBorderPaint)

        val clearPaint = Paint().apply {
            color = Color.RED
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("CLEAR", clearX + (toolBoxSize + 40) / 2, startY + toolBoxSize / 2 + 10, clearPaint)
    }

    private fun drawColorPalette(canvas: Canvas) {
        val colorBoxSize = 60f
        val startX = (width - (colors.size * colorBoxSize)) / 2
        val startY = height - colorBoxSize - 20f

        colors.forEachIndexed { index, color ->
            val paint = Paint().apply {
                this.color = color
                style = Paint.Style.FILL
            }

            val left = startX + (index * colorBoxSize)
            val rect = RectF(left, startY, left + colorBoxSize, startY + colorBoxSize)
            canvas.drawRect(rect, paint)

            // Highlight selected color
            if (index == currentColorIndex) {
                val highlightPaint = Paint().apply {
                    this.color = Color.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                }
                canvas.drawRect(rect, highlightPaint)
            }
        }
    }

    private fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if user is tapping on tools panel
                if (y < 100f && x < 600f) {
                    handleToolSelection(x)
                    return true
                }

                // Check if user is tapping on clear button
                if (y < 100f && x >= 600f && x < 720f) {
                    clearCanvas()
                    return true
                }

                // Check if user is tapping on color palette
                if (y > height - 80f) {
                    handleColorSelection(x)
                    return true
                }

                // Start new drawing element
                startX = x
                startY = y
                lastX = x
                lastY = y

                when (currentMode) {
                    Mode.DRAW -> {
                        currentPath.reset()
                        currentPath.moveTo(x, y)
                    }
                    Mode.TEXT -> {
                        showTextInputDialog()
                    }
                    else -> {
                        // Rectangle, Circle, and Line use the start and end positions
                    }
                }

                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                // Skip if we're in the tools or color palette area
                if (y < 100f || y > height - 80f) {
                    return true
                }

                lastX = x
                lastY = y

                if (currentMode == Mode.DRAW) {
                    currentPath.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                }

                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Skip if we're in the tools or color palette area
                if (y < 100f || y > height - 80f) {
                    return true
                }

                // Save the current element
                when (currentMode) {
                    Mode.DRAW -> {
                        val pathCopy = Path(currentPath)
                        paths.add(PathElement(pathCopy, Paint(currentPaint)))
                        currentPath.reset()
                    }
                    Mode.RECTANGLE -> {
                        val left = minOf(startX, x)
                        val top = minOf(startY, y)
                        val right = maxOf(startX, x)
                        val bottom = maxOf(startY, y)
                        paths.add(RectElement(RectF(left, top, right, bottom), Paint(currentPaint)))
                    }
                    Mode.CIRCLE -> {
                        val radius = calculateDistance(startX, startY, x, y)
                        paths.add(CircleElement(startX, startY, radius, Paint(currentPaint)))
                    }
                    Mode.LINE -> {
                        paths.add(LineElement(startX, startY, x, y, Paint(currentPaint)))
                    }
                    Mode.TEXT -> {
                        textToAdd?.let { text ->
                            val textPaint = Paint(currentPaint).apply {
                                textSize = 60f
                                style = Paint.Style.FILL
                            }
                            paths.add(TextElement(text, startX, startY, textPaint))
                            textToAdd = null
                        }
                    }
                }

                invalidate()
                return true
            }
            else -> return false
        }
    }

    private fun handleToolSelection(x: Float) {
        val toolBoxSize = 80f
        val startX = 20f

        val index = ((x - startX) / (toolBoxSize + 10)).toInt()

        if (index in 0..4) {
            currentMode = when (index) {
                0 -> Mode.DRAW
                1 -> Mode.LINE
                2 -> Mode.RECTANGLE
                3 -> Mode.CIRCLE
                4 -> Mode.TEXT
                else -> Mode.DRAW
            }

            Toast.makeText(
                context,
                "Selected: ${currentMode.name}",
                Toast.LENGTH_SHORT
            ).show()

            invalidate()
        }
    }

    private fun handleColorSelection(x: Float) {
        val colorBoxSize = 60f
        val startX = (width - (colors.size * colorBoxSize)) / 2

        // Calculate which color was selected based on x-coordinate
        val index = ((x - startX) / colorBoxSize).toInt()

        if (index in colors.indices) {
            currentColorIndex = index
            currentPaint = Paint().apply {
                color = colors[currentColorIndex]
                isAntiAlias = true
                strokeWidth = 8f
                style = Paint.Style.STROKE
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            invalidate()
        }
    }

    // Method to clear canvas
    fun clearCanvas() {
        paths.clear()
        currentPath.reset()
        textToAdd = null
        invalidate()
    }

    private fun showTextInputDialog() {
        val input = EditText(context)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.hint = "Enter text"

        AlertDialog.Builder(context)
            .setTitle("Add Text")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                textToAdd = input.text.toString()
                if (textToAdd.isNullOrEmpty()) {
                    textToAdd = null
                } else {
                    invalidate()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
                textToAdd = null
            }
            .show()
    }
}