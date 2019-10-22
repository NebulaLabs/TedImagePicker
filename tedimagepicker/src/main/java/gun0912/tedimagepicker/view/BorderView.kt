package gun0912.tedimagepicker.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import gun0912.tedimagepicker.R
import kotlin.math.min


class BorderView(context: Context, attributeSet: AttributeSet): View(context, attributeSet) {

    var borderBackgroundColor: Int? = null
    private var paint: Paint = Paint()

    private var localWidth: Int = 0
    private var localHeight: Int = 0

    var allThickness: Int = -1
    var leftThickness: Int = -1
    var rightThickness: Int = -1
    var topThickness: Int = -1
    var bottomThickness: Int = -1

    init {
        val mtypedArray = context.obtainStyledAttributes(attributeSet, R.styleable.BorderView)
        borderBackgroundColor = if (borderBackgroundColor == null) mtypedArray.getColor(R.styleable.BorderView_backgroundColor, ContextCompat.getColor(context, R.color.ted_image_picker_duration_background)) else borderBackgroundColor
        allThickness = if (allThickness > 0) allThickness else  mtypedArray.getDimensionPixelSize(R.styleable.BorderView_thickness, 5)
        leftThickness = if (leftThickness > 0) leftThickness else  mtypedArray.getDimensionPixelSize(R.styleable.BorderView_leftThickness, -1)
        rightThickness = if (rightThickness > 0) rightThickness else  mtypedArray.getDimensionPixelSize(R.styleable.BorderView_rightThickness, -1)
        topThickness = if (topThickness > 0) topThickness else  mtypedArray.getDimensionPixelSize(R.styleable.BorderView_topThickness, -1)
        bottomThickness = if (bottomThickness > 0) bottomThickness else mtypedArray.getDimensionPixelSize(R.styleable.BorderView_bottomThickness, -1)
        mtypedArray.recycle()
    }
    
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        this.paint.color = borderBackgroundColor?: ContextCompat.getColor(context, R.color.ted_image_picker_duration_background)
        this.paint.style = Paint.Style.FILL

        val selectedTopThickness = (if (topThickness > 0) topThickness else allThickness).toFloat()
        val selectedLeftThickness = (if (leftThickness > 0) leftThickness else allThickness).toFloat()
        val selectedRightThickness = (if (rightThickness > 0) rightThickness else allThickness).toFloat()
        val selectedBottomThickness = (if (bottomThickness > 0) bottomThickness else allThickness).toFloat()

        Log.i("SIZE", "local view is $localWidth x $localHeight")
        Log.i("SIZE", "view is $width x $height")
        Log.i("SIZE", "===================================================")

        canvas?.drawRect(0F, 0F, localWidth.toFloat(), selectedTopThickness, this.paint) //TOP
        canvas?.drawRect(0F, localHeight - selectedBottomThickness , localWidth.toFloat(), localHeight.toFloat(), this.paint) //BOTTOM
        canvas?.drawRect(0F, selectedTopThickness, selectedLeftThickness, localHeight - selectedBottomThickness, this.paint) //LEFT
        canvas?.drawRect(localWidth - selectedRightThickness, selectedTopThickness, localWidth.toFloat(),localHeight - selectedBottomThickness, this.paint) //RIGHT
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = dp2px(137)
        if (widthMode == MeasureSpec.EXACTLY) {
            localWidth= widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            localWidth = min(desiredWidth, widthSize)
        } else {
            localWidth = desiredWidth
        }

        val desiredHeight = dp2px(137)
        if (heightMode == MeasureSpec.EXACTLY) {
            localHeight= heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            localHeight = min(desiredHeight, heightSize)
        } else {
            localHeight = desiredHeight
        }

        Log.i("MEASURED", "result $localWidth x $localHeight")

        this.setMeasuredDimension(this.localWidth, this.localHeight)

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.localWidth = w
        this.localHeight = h
        invalidate()
    }

    fun dp2px(dp: Int): Int {
        val density = this.context.resources.displayMetrics.density
        return (dp.toFloat() * density + 0.5f).toInt()
    }
}