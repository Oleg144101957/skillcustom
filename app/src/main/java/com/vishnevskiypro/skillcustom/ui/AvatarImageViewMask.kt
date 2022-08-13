package com.vishnevskiypro.skillcustom.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.toRectF
import com.vishnevskiypro.skillcustom.R
import com.vishnevskiypro.skillcustom.extensions.dpToPx

@SuppressLint("AppCompatCustomView")
class AvatarImageViewMask @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_BORDER_WIDTH = 2
        private const val DEFAULT_BORDER_COLOR = Color.WHITE
        private const val DEFAULT_SIZE = 40
    }

    @Px
    var borderWidth: Float = context.dpToPx(DEFAULT_BORDER_WIDTH)
    @ColorInt
    private var borderColor: Int = Color.WHITE
    private var initials: String = "??"

    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val viewRect = Rect()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private lateinit var resultBM: Bitmap
    private lateinit var maskBM: Bitmap
    private lateinit var srcBm: Bitmap

    init {
        if (attrs != null){
            val ta = context.obtainStyledAttributes(attrs, R.styleable.AvatarImageViewMask)
            borderWidth = ta.getDimension(
                R.styleable.AvatarImageViewMask_aivm_borderWidth,
                context.dpToPx(DEFAULT_BORDER_WIDTH)
            )

            borderColor = ta.getColor(R.styleable.AvatarImageViewMask_aivm_borderColor, DEFAULT_BORDER_COLOR)
            initials = ta.getString(R.styleable.AvatarImageViewMask_aivm_initials) ?: "??"
            ta.recycle()
        }

        scaleType = ScaleType.CENTER_CROP
        setup()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        Log.d("AAAAA", "onMeasure")

        val initSize = resolveDefaultSize(widthMeasureSpec)
        setMeasuredDimension(initSize, initSize)
        Log.d("AAAAA", "onMeasure after set size: $measuredWidth $measuredHeight")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("AAAAA", "onSizeChanged")
        if(w == 0) return
        with(viewRect){
            left = 0
            top = 0
            right = w
            bottom = h
        }

        prepareBitmaps(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas)
        // NOT ALLOCATE, ONLY DRAW
        canvas.drawBitmap(resultBM, viewRect, viewRect, null)
        //resize rect
        val half = (borderWidth / 2).toInt()
        viewRect.inset(half, half)
        canvas.drawOval(viewRect.toRectF(), borderPaint)
        Log.d("AAAAA", "onDraw")
    }

    private fun resolveDefaultSize(spec: Int): Int {
        return when (MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPx(DEFAULT_SIZE).toInt()
            MeasureSpec.AT_MOST -> MeasureSpec.getSize(spec)
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(spec)
            else -> MeasureSpec.getSize(spec)
        }
    }

    private fun setup() {
        with(maskPaint){
            color = Color.RED
            style = Paint.Style.FILL
        }
        with(borderPaint){
            style = Paint.Style.STROKE
            strokeWidth = borderWidth
            color = borderColor
        }
    }

    private fun prepareBitmaps(w: Int, h : Int){
        //prepare buffer this

        maskBM = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        resultBM = maskBM.copy(Bitmap.Config.ARGB_8888, true)
        val maskCanvas = Canvas(maskBM)
        maskCanvas.drawOval(viewRect.toRectF(), maskPaint)
        maskPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        srcBm = drawable.toBitmap(w, h, Bitmap.Config.ARGB_8888)

        val resultCanvas = Canvas(resultBM)

        resultCanvas.drawBitmap(maskBM, viewRect, viewRect, null)
        resultCanvas.drawBitmap(srcBm, viewRect, viewRect, maskPaint)
    }

}