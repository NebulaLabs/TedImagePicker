package gun0912.tedimagepicker.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Cap
import android.graphics.Paint.Style
import android.util.AttributeSet
import android.view.View
import gun0912.tedimagepicker.R
import kotlin.math.min

class MyPercentage constructor(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint: Paint
    private var localWidth: Int = 0
    private var localHeight: Int = 0
    private var result: Int = 0
    var ringColor: Int = 0
    var ringProgressColor: Int = 0
    var textColor: Int = 0
    var textSize: Int
    var ringWidth: Float = 0.toFloat()
    private var max: Int = 0
    @get:Synchronized
    private var progress: Int = 0
    private var textIsShow: Boolean = true
    private val style: Int
    private var centre: Point = Point(0,0)
    private var radius: Int = 0

    init {
        this.result = 0
        this.paint = Paint()
        this.result = this.dp2px(100)
        val mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.MyPercentage)
        this.ringColor = mTypedArray.getColor(R.styleable.MyPercentage_ringColor, -16777216)
        this.ringProgressColor = mTypedArray.getColor(R.styleable.MyPercentage_ringProgressColor, -1)
        this.textColor = mTypedArray.getColor(R.styleable.MyPercentage_percTextColor, -16777216)
        this.textSize = mTypedArray.getDimensionPixelSize(R.styleable.MyPercentage_percTextSize, 20)
        this.ringWidth = mTypedArray.getDimension(R.styleable.MyPercentage_ringWidth, 5.0f)
        this.max = mTypedArray.getInteger(R.styleable.MyPercentage_max, 100)
        this.progress = mTypedArray.getInteger(R.styleable.MyPercentage_progress, 0)
        this.textIsShow = mTypedArray.getBoolean(R.styleable.MyPercentage_showText, true)
        this.style = mTypedArray.getInt(R.styleable.MyPercentage_style, 0)
        mTypedArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.centre = Point(this.width / 2, this.height / 2)
        this.radius = ((min(this.width - this.paddingStart - this.paddingEnd, this.height - this.paddingTop - this.paddingBottom) - this.ringWidth) / 2).toInt()
//        this.radius = (this.centre.toFloat() - this.ringWidth / 2.0f).toInt()
        this.drawCircle(canvas)
        this.drawTextContent(canvas)
        this.drawProgress(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        this.paint.color = this.ringColor
        this.paint.style = Style.STROKE
        this.paint.strokeWidth = this.ringWidth
        this.paint.isAntiAlias = true
        canvas.drawCircle(this.centre.x.toFloat(), this.centre.y.toFloat(), this.radius.toFloat(), this.paint)
    }

    private fun drawTextContent(canvas: Canvas) {
        this.paint.strokeWidth = 0.0f
        this.paint.color = this.textColor
        this.paint.textSize = this.textSize.toFloat()
//        this.paint.typeface = Typeface.DEFAULT
        val percent = (this.progress.toFloat() / this.max.toFloat() * 100.0f).toInt()
        val toShowText = "$percent%"

        var widthOfText = 0f
        var heightOfText = 0f

        val bounds = Rect()
        this.paint.getTextBounds(toShowText, 0, toShowText.length, bounds)
        widthOfText = bounds.width().toFloat()
        heightOfText = bounds.height().toFloat()

        widthOfText = paint.measureText(toShowText)

        if (this.textIsShow && this.style == 0) {
            canvas.drawText(
                "$percent%",
                this.centre.x.toFloat() - (widthOfText / 2.0f),
                this.centre.y.toFloat() + (heightOfText / 2.0f),
                this.paint
            )
        }

    }

    private fun drawProgress(canvas: Canvas) {
        this.paint.strokeWidth = this.ringWidth
        this.paint.color = this.ringProgressColor
        val strokeOval = RectF(
            (this.centre.x - this.radius).toFloat(),
            (this.centre.y - this.radius).toFloat(),
            (this.centre.x + this.radius).toFloat(),
            (this.centre.y + this.radius).toFloat()
        )
        val fillOval = RectF(
            (this.centre.x - this.radius).toFloat() + this.ringWidth,
            (this.centre.y - this.radius).toFloat() + this.ringWidth,
            (this.centre.x + this.radius).toFloat() - this.ringWidth,
            (this.centre.y + this.radius).toFloat() - this.ringWidth
        )
        when (this.style) {
            0 -> {
                this.paint.style = Style.STROKE
                this.paint.strokeCap = Cap.ROUND
                canvas.drawArc(strokeOval, -90.0f, (360 * this.progress / this.max).toFloat(), false, this.paint)
            }
            1 -> {
                this.paint.style = Style.FILL_AND_STROKE
                this.paint.strokeCap = Cap.ROUND
                if (this.progress != 0) {
                    canvas.drawArc(fillOval, -90.0f, (360 * this.progress / this.max).toFloat(), true, this.paint)
                }
            }
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == -2147483648) {
            this.localWidth = this.result
        } else {
            this.localWidth = widthSize
        }

        if (heightMode == -2147483648) {
            this.localHeight = this.result
        } else {
            this.localHeight = heightSize
        }

        this.setMeasuredDimension(this.localWidth, this.localHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.localWidth = w
        this.localHeight = h
    }

    @Synchronized
    fun getMax(): Int {
        return this.max
    }

    @Synchronized
    fun setMax(max: Int) {
        if (max < 0) {
            throw IllegalArgumentException("The max progress of 0")
        } else {
            this.max = max
        }
    }

    @Synchronized
    fun setProgress(progress: Int, animate: Boolean) {
        if (progress < 0) {
            throw IllegalArgumentException("The progress of 0")
        } else {
            val animator = ValueAnimator.ofInt(this.progress, progress)
            animator.duration = if (animate) 300 else 0
            animator.addUpdateListener {
                var newProg = it.animatedValue as Int

                if (newProg > this.max) {
                    newProg = this.max
                }

                if (newProg <= this.max) {
                    this.progress = newProg
                }

                this.postInvalidate()
            }
            animator.start()
        }
    }

    fun dp2px(dp: Int): Int {
        val density = this.context.resources.displayMetrics.density
        return (dp.toFloat() * density + 0.5f).toInt()
    }

    companion object {
        val STROKE = 0
        val FILL = 1
    }
}
