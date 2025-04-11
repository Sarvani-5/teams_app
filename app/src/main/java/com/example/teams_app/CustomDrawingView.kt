package com.example.teams_app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CustomDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#E8F5E9") // Light green background
        style = Paint.Style.FILL
    }

    private val logoPaint = Paint().apply {
        color = Color.parseColor("#388E3C") // Green
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#1B5E20") // Dark green
        textSize = 60f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val subtitlePaint = Paint().apply {
        color = Color.parseColor("#2E7D32") // Medium green
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val decorationPaint = Paint().apply {
        color = Color.parseColor("#4CAF50") // Lighter green
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        // Draw background
        canvas.drawRect(0f, 0f, width, height, backgroundPaint)

        // Draw decorative border
        val borderPadding = 20f
        canvas.drawRect(
            borderPadding,
            borderPadding,
            width - borderPadding,
            height - borderPadding,
            decorationPaint
        )

        // Draw logo (a simple stylized tree representing "roots")
        val centerX = width / 2
        val treeTrunkWidth = 30f
        val treeTrunkHeight = 150f
        val treeTop = height / 3

        // Draw trunk
        canvas.drawRect(
            centerX - treeTrunkWidth / 2,
            treeTop,
            centerX + treeTrunkWidth / 2,
            treeTop + treeTrunkHeight,
            logoPaint
        )

        // Draw tree branches/leaves using a circle
        canvas.drawCircle(centerX, treeTop - 80f, 120f, logoPaint)

        // Draw roots (using paths)
        path.reset()
        // Root 1 (left)
        path.moveTo(centerX - treeTrunkWidth / 2, treeTop + treeTrunkHeight)
        path.lineTo(centerX - 100f, treeTop + treeTrunkHeight + 70f)
        // Root 2 (center)
        path.moveTo(centerX, treeTop + treeTrunkHeight)
        path.lineTo(centerX, treeTop + treeTrunkHeight + 90f)
        // Root 3 (right)
        path.moveTo(centerX + treeTrunkWidth / 2, treeTop + treeTrunkHeight)
        path.lineTo(centerX + 100f, treeTop + treeTrunkHeight + 70f)
        canvas.drawPath(path, logoPaint)

        // Draw team name
        canvas.drawText(
            "Tamil Roots",
            centerX,
            treeTop + treeTrunkHeight + 150f,
            textPaint
        )

        // Draw team subtitle
        canvas.drawText(
            "Innovating for Tomorrow",
            centerX,
            treeTop + treeTrunkHeight + 200f,
            subtitlePaint
        )

        // Draw decorative elements (circles in the corners)
        val cornerRadius = 50f
        canvas.drawCircle(borderPadding * 2, borderPadding * 2, cornerRadius, decorationPaint)
        canvas.drawCircle(width - borderPadding * 2, borderPadding * 2, cornerRadius, decorationPaint)
        canvas.drawCircle(borderPadding * 2, height - borderPadding * 2, cornerRadius, decorationPaint)
        canvas.drawCircle(width - borderPadding * 2, height - borderPadding * 2, cornerRadius, decorationPaint)
    }
}