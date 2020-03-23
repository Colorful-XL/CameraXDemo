package com.example.cameraxdemo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View


class ViewFinderView : View {
    /**
     * 定义扫描框宽高
     */
    private var paint: Paint? = null
    private var resultBitmap: Bitmap? = null
    private var maskColor: Int = 0
    private var resultColor: Int = 0
    private var resultPointColor: Int = 0

    private val ANIMATION_DELAY = 10L
    private val OPAQUE = 0xFF


    private var ScreenRate: Int = 0


    private val CORNER_WIDTH = 5


    private val SPEEN_DISTANCE = 5


    private var density: Float = 0.toFloat()
    private val TEXT_SIZE = 16

    private val TEXT_PADDING_TOP = 30

    private var slideTop: Int = 0
    private var slideBottom: Int = 0
    private var isFirst: Boolean = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        // Initialize these once for performance rather than calling them every
        // time in onDraw().
        paint = Paint()
        maskColor = R.color.mask
        resultColor = Color.BLUE
        resultPointColor = Color.GREEN

        density = context.resources.displayMetrics.density
        ScreenRate = (15 * density).toInt()
    }

    @SuppressLint("DrawAllocation")
    public override fun onDraw(canvas: Canvas) {
        val frame = Rect(330, 450, 880, 1000)

        if (!isFirst) {
            isFirst = true
            slideTop = frame.top
            slideBottom = frame.bottom
        }
        val width = canvas.width
        val height = canvas.height

        // 罩一层半透明mask
        paint?.color = if (resultBitmap != null) resultColor else maskColor
        paint?.let {
            canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), it)

            canvas.drawRect(
                0f,
                frame.top.toFloat(),
                frame.left.toFloat(),
                (frame.bottom + 1).toFloat(),
                it
            )
            canvas.drawRect(
                (frame.right + 1).toFloat(),
                frame.top.toFloat(),
                width.toFloat(),
                (frame.bottom + 1).toFloat(),
                it
            )

            canvas.drawRect(
                0f, (frame.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), it)
        }



        if (resultBitmap != null) {
            //扫描框的4个角
            paint?.alpha = OPAQUE
            canvas.drawBitmap(resultBitmap!!, null, frame, paint)
        } else {

            paint?.color = Color.BLUE
            paint?.let {
                canvas.drawRect(
                    frame.left.toFloat(), frame.top.toFloat(), (frame.left + ScreenRate).toFloat(),
                    (frame.top + CORNER_WIDTH).toFloat(), it
                )
                canvas.drawRect(
                    frame.left.toFloat(),
                    frame.top.toFloat(),
                    (frame.left + CORNER_WIDTH).toFloat(),
                    (frame.top + ScreenRate).toFloat(),
                    it
                )

                canvas.drawRect(
                    (frame.right - ScreenRate).toFloat(),
                    frame.top.toFloat(),
                    frame.right.toFloat(),
                    (frame.top + CORNER_WIDTH).toFloat(),
                    it
                )
                canvas.drawRect(
                    (frame.right - CORNER_WIDTH).toFloat(),
                    frame.top.toFloat(),
                    frame.right.toFloat(),
                    (frame.top + ScreenRate).toFloat(),
                    it
                )
                canvas.drawRect(
                    frame.left.toFloat(),
                    (frame.bottom - CORNER_WIDTH).toFloat(),
                    (frame.left + ScreenRate).toFloat(),
                    frame.bottom.toFloat(),
                    it
                )
                canvas.drawRect(
                    frame.left.toFloat(),
                    (frame.bottom - ScreenRate).toFloat(),
                    (frame.left + CORNER_WIDTH).toFloat(),
                    frame.bottom.toFloat(),
                    it
                )
                canvas.drawRect(
                    (frame.right - ScreenRate).toFloat(),
                    (frame.bottom - CORNER_WIDTH).toFloat(),
                    frame.right.toFloat(),
                    frame.bottom.toFloat(),
                    it
                )
                canvas.drawRect(
                    (frame.right - CORNER_WIDTH).toFloat(),
                    (frame.bottom - ScreenRate).toFloat(),
                    frame.right.toFloat(),
                    frame.bottom.toFloat(),
                    it
                )
            }

            //中间上下滚动的扫描条
            slideTop += SPEEN_DISTANCE //后面的postInvalidateDelayed方法会刷新
            if (slideTop >= frame.bottom) {
                slideTop = frame.top
            }
            val lineRect = Rect()
            lineRect.left = frame.left
            lineRect.right = frame.right
            lineRect.top = slideTop
            lineRect.bottom = slideTop + 18
            canvas.drawBitmap(
                (resources.getDrawable(R.drawable.fle) as BitmapDrawable).bitmap, null, lineRect,
                paint
            )

            paint?.setColor(Color.WHITE)
            paint?.setTextSize(TEXT_SIZE * density)
            paint?.setAlpha(0x40)
            paint?.setTypeface(Typeface.create("System", Typeface.BOLD))
            val text = "对准二维码以进行扫描"
            val textWidth = paint?.measureText(text)

            paint?.let {
                canvas.drawText(
                    text,
                    (width - textWidth!!) / 2,
                    (frame.bottom + TEXT_PADDING_TOP.toFloat() * density).toFloat(),
                    it
                )
            }
        }

        postInvalidateDelayed(
            ANIMATION_DELAY, frame.left, frame.top,
            frame.right, frame.bottom
        )
    }
}