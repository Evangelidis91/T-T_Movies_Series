package com.evangelidis.t_tmoviesseries.view.login

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.Paint.Align
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.evangelidis.t_tmoviesseries.R
import java.util.*
import kotlin.math.min
import kotlin.math.tan

class LoginButton : View {
    private var buttonTop = 0
    private var buttonBottom = 0
    private var loginButtonPaint: Paint = Paint()
    private var signUpButtonPaint: Paint = Paint()
    private val loginButtonPath = Path()
    private val signUpButtonPath = Path()
    private val r = Rect()
    private var startRight = 0
    private var currentY = 0f
    private var buttonCenter = 0
    private var currentX = 0f
    private var currentRight = 0f
    private var currentBottomY = 0f
    private var currentBottomX = 0f
    private var currentArcY = 0
    private var currentArcX = 0f
    private var paint2: Paint = Paint()
    private var loginPaint: Paint = Paint()
    private var orPaint: Paint = Paint()
    private var signUpPaint: Paint = Paint()
    private var currentLoginX = 0f
    private var currentSignUpTextX = 0f
    private var largeTextSize = 0f
    private var smallTextSize = 0f
    private var currentLoginY = 0f
    private var currentLeft = 0f
    private var signUpOrX = 0f
    private var isLogin = true
    private var currentSignUpTextY = 0f
    private var currentSignUpX = 0f
    private var currentBottomSignUpX = 0f
    private var startLeft = 0
    private var callback: OnButtonSwitchedListener? = null
    private var startSignUpTextX = 0f
    private var startSignUpTextY = 0f
    private var startLoginX = 0f
    private var startLoginY = 0f
    private var loginOrX = 0f
    private lateinit var loginButtonOutline: Rect
    private lateinit var signUpButtonOutline: Rect
    private lateinit var onSignUpListener: OnSignUpListener
    private lateinit var onLoginListener: OnLoginListener
    private lateinit var loginTextOutline: Rect
    private lateinit var signUpTextOutline: Rect

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        loginButtonPaint.apply {
            color = ContextCompat.getColor(context, R.color.secondPage)
            style = Paint.Style.FILL
        }

        signUpButtonPaint.apply {
            color = ContextCompat.getColor(context, R.color.colorPrimary)
            style = Paint.Style.FILL
        }

        paint2.apply {
            color = Color.parseColor("#ffffff")
            style = Paint.Style.FILL
        }

        loginPaint.apply {
            color = ContextCompat.getColor(context, R.color.text)
            textAlign = Align.CENTER
            textSize = dpToPixels(16)
        }

        orPaint.apply {
            color = ContextCompat.getColor(context, R.color.text_two)
            textSize = dpToPixels(16)
        }

        signUpPaint.apply {
            color = ContextCompat.getColor(context, R.color.text)
            textSize = dpToPixels(64)
            textAlign = Align.CENTER
        }
        //signUpPaint.setAlpha(255);
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buttonTop = height - bottomMargin - buttonHeight + 50
        buttonBottom = height - bottomMargin
        startRight = startButtonRight.toInt()
        buttonCenter = (buttonBottom - buttonTop) / 2 + buttonTop
        currentSignUpX = width.toFloat()
        currentBottomSignUpX = width.toFloat()
        loginOrX = dpToPixels(32)
        currentY = buttonCenter.toFloat()
        currentBottomY = buttonBottom.toFloat()
        currentRight = startRight.toFloat()
        currentLeft = width - startRight.toFloat()
        startLeft = width - startRight
        loginPaint.getTextBounds(getString(R.string.sign_up), 0, 7, r)
        currentLoginX = dpToPixels(92)
        val signUpWidth = r.right
        currentSignUpTextX = width - signUpWidth / 2 - dpToPixels(32)
        loginPaint.getTextBounds(getString(R.string.login), 0, 5, r)
        loginTextOutline = Rect()
        signUpTextOutline = Rect()
        signUpPaint.getTextBounds(getString(R.string.login), 0, 5, loginTextOutline)
        signUpPaint.getTextBounds(getString(R.string.sign_up), 0, 7, signUpTextOutline)
        loginTextOutline.offset(width / 2 - (loginTextOutline.right + loginTextOutline.left) / 2, dpToPixels(457).toInt())
        signUpTextOutline.offset(width / 2 - (signUpTextOutline.right + signUpTextOutline.left) / 2, dpToPixels(457).toInt())
        val loginWidth = r.right
        orPaint.getTextBounds(context.getString(R.string.or).toUpperCase(Locale.ROOT), 0, 2, r)
        val margin = currentLoginX - loginWidth / 2 - dpToPixels(32) - r.right
        signUpOrX = width - signUpWidth - dpToPixels(32) - r.right - margin
        currentLoginY = buttonCenter + dpToPixels(8)
        currentSignUpTextY = buttonCenter + dpToPixels(8)
        largeTextSize = dpToPixels(64)
        smallTextSize = dpToPixels(16)
        startLoginX = currentLoginX
        startLoginY = currentLoginY
        startSignUpTextX = currentSignUpTextX
        startSignUpTextY = currentSignUpTextY

        loginButtonPath.apply {
            moveTo(0f, buttonBottom.toFloat())
            lineTo(currentRight, buttonBottom.toFloat())
            lineTo(currentRight, buttonTop.toFloat())
            lineTo(0f, buttonTop.toFloat())
            close()
        }

        signUpButtonPath.apply {
            moveTo(width.toFloat(), buttonBottom.toFloat())
            lineTo(currentLeft, buttonBottom.toFloat())
            lineTo(currentLeft, buttonTop.toFloat())
            lineTo(width.toFloat(), buttonTop.toFloat())
            close()
        }

        loginButtonOutline = Rect(0, buttonTop, currentRight.toInt() + buttonHeight / 2, buttonBottom)
        signUpButtonOutline = Rect((width - currentRight - buttonHeight / 2).toInt(), buttonTop, width, buttonBottom)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isLogin) {
            canvas.drawPath(loginButtonPath, loginButtonPaint)
            canvas.drawArc(
                currentRight - buttonHeight / 2 + currentArcX,
                buttonTop.toFloat(),
                currentRight + buttonHeight / 2 - currentArcX,
                buttonBottom.toFloat(), 0f, 360f,
                false,
                loginButtonPaint
            )
            canvas.drawText(getString(R.string.or), loginOrX, buttonCenter + dpToPixels(8), orPaint)
            canvas.drawText(getString(R.string.login), currentLoginX, currentLoginY, loginPaint)
        } else {
            canvas.drawPath(signUpButtonPath, signUpButtonPaint)
            canvas.drawArc(
                currentLeft - buttonHeight / 2 + currentArcX,
                buttonTop.toFloat(),
                currentLeft + buttonHeight / 2 - currentArcX,
                buttonBottom.toFloat(), 0f, 360f,
                false,
                signUpButtonPaint
            )
            canvas.drawText(getString(R.string.or), signUpOrX, buttonCenter + dpToPixels(8), orPaint)
            canvas.drawText(getString(R.string.sign_up), currentSignUpTextX, currentSignUpTextY, signUpPaint)
        }
    }

    private val buttonHeight = resources.getDimensionPixelOffset(R.dimen.bottom_height)

    private val bottomMargin = resources.getDimensionPixelOffset(R.dimen.bottom_margin)

    fun startAnimation() {
        val start = startButtonRight
        val animator = ObjectAnimator.ofFloat(0f, 1f)
        //animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener { animation: ValueAnimator ->
            val fraction = animation.animatedValue as Float
            val currentAngle = fraction * (Math.PI.toFloat() / 2) // in radians
            val gone = (width - start) * fraction
            currentRight = start + gone
            currentLeft = startLeft - gone

            // fade out sign up text to 0
            if (isLogin) {
                signUpPaint.alpha = (255 - 255 * fraction).toInt() // fade out sign up large text
            } else {
                loginPaint.alpha = (255 - 255 * fraction).toInt() // fade out login large text
            }
            if (orPaint.alpha != 0) {
                orPaint.alpha = 0
            }

            // move login text to center and scale
            if (isLogin) {
                currentLoginX = startLoginX + (width / 2 - startLoginX) * fraction
                currentLoginY = startLoginY - (startLoginY - dpToPixels(457)) * fraction
                loginPaint.textSize = smallTextSize + (largeTextSize - smallTextSize) * fraction
            } else {
                currentSignUpTextX = startSignUpTextX - (startSignUpTextX - width / 2) * fraction
                currentSignUpTextY = startSignUpTextY - (startSignUpTextY - dpToPixels(457)) * fraction
                signUpPaint.textSize = smallTextSize + (largeTextSize - smallTextSize) * fraction
            }
            currentArcY = (fraction * dpToPixels(28)).toInt() // just hardcoded value
            currentArcX = (fraction * dpToPixels(37)) // just hardcoded value
            val y = tan(currentAngle.toDouble()) * currentRight // goes from ~ 0 to 4451
            val realY = (buttonTop - y).toFloat() // goes ~ from 1234 to -1243
            currentY = StrictMath.max(0f, realY) // goes ~ from 1234 to 0
            val realBottomY = (buttonBottom + y).toFloat()
            currentBottomY = min(height.toFloat(), realBottomY)
            if (currentY == 0f) { // if reached top, start moving to the right
                val cot = 1.0f / tan(currentAngle.toDouble())
                currentX = ((y - buttonTop) * cot).toFloat()
                currentSignUpX = width - currentX
            }
            if (currentBottomY == height.toFloat()) {
                val cot = 1.0f / tan(currentAngle.toDouble())
                currentBottomX = ((y - bottomMargin) * cot).toFloat()
                currentBottomSignUpX = width - currentBottomX
            }
            if (currentAngle == Math.PI.toFloat() / 2) {
                currentX = currentRight
                currentBottomX = currentRight
                currentY = 0f
                currentBottomY = height.toFloat()
            }
            if (isLogin) {
                loginButtonPath.apply {
                    reset()
                    moveTo(0f, buttonBottom.toFloat())
                    lineTo(currentRight, buttonBottom.toFloat())
                    lineTo(currentRight, buttonTop.toFloat())
                    lineTo(currentX, currentY)
                    lineTo(0f, currentY)
                    lineTo(0f, currentBottomY)
                    lineTo(currentBottomX, currentBottomY)
                    lineTo(currentRight, buttonBottom.toFloat())
                }
            } else {
                signUpButtonPath.apply {
                    reset()
                    moveTo(width.toFloat(), buttonBottom.toFloat())
                    lineTo(currentLeft, buttonBottom.toFloat())
                    lineTo(currentLeft, buttonTop.toFloat())
                    lineTo(currentSignUpX, currentY)
                    lineTo(width.toFloat(), currentY)
                    lineTo(width.toFloat(), currentBottomY)
                    lineTo(currentBottomSignUpX, currentBottomY)
                    lineTo(currentLeft, buttonBottom.toFloat())
                }
            }
            currentX = 0f
            currentSignUpX = width.toFloat()
            currentBottomX = 0f
            currentBottomSignUpX = width.toFloat()
            invalidate()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                orPaint.alpha = 125
                signUpPaint.alpha = 255
                signUpPaint.textSize = dpToPixels(16)
                currentArcX = 0f
                currentArcY = 0
                currentRight = startButtonRight
                currentLeft = width - startButtonRight
                isLogin = !isLogin
                if (isLogin) {
                    currentLoginX = startLoginX
                    currentLoginY = startLoginY
                    loginPaint.alpha = 255
                    loginPaint.textSize = dpToPixels(16)
                    signUpPaint.alpha = 255
                    signUpPaint.textSize = dpToPixels(64)
                }
                currentSignUpTextX = startSignUpTextX
                currentSignUpTextY = startSignUpTextY
                val hideButton = startRight + buttonHeight / 2
                if (!isLogin) {
                    currentLeft += hideButton.toFloat()
                } else {
                    currentRight -= hideButton.toFloat()
                }

                //move texts
                if (!isLogin) {
                    signUpOrX += hideButton.toFloat()
                    currentSignUpTextX += hideButton.toFloat()
                } else {
                    loginOrX -= hideButton.toFloat()
                    currentLoginX -= hideButton.toFloat()
                }

                val hiddenButtonLeft = currentLeft
                val hiddenButtonRight = currentRight
                val endSignUpOrX = signUpOrX
                val endSignUpTextX = currentSignUpTextX
                val endLoginOrX = loginOrX
                val endLoginTextX = currentLoginX

                // reset paths
                signUpButtonPath.apply {
                    reset()
                    moveTo(width.toFloat(), buttonBottom.toFloat())
                    lineTo(currentLeft, buttonBottom.toFloat())
                    lineTo(currentLeft, buttonTop.toFloat())
                    lineTo(width.toFloat(), buttonTop.toFloat())
                    close()
                }

                loginButtonPath.apply {
                    reset()
                    moveTo(0f, buttonBottom.toFloat())
                    lineTo(currentRight, buttonBottom.toFloat())
                    lineTo(currentRight, buttonTop.toFloat())
                    lineTo(0f, buttonTop.toFloat())
                    close()
                }

                callback?.onButtonSwitched(isLogin)
                val buttonBounce = ObjectAnimator.ofInt(0, hideButton).setDuration(500)
                buttonBounce.startDelay = 300
                buttonBounce.interpolator = MyBounceInterpolator(.2, 7.0)
                buttonBounce.addUpdateListener { a: ValueAnimator ->
                    val v = a.animatedValue as Int
                    if (!isLogin) {
                        currentLeft = hiddenButtonLeft - v
                        signUpOrX = endSignUpOrX - v
                        currentSignUpTextX = endSignUpTextX - v

                        signUpButtonPath.apply {
                            reset()
                            moveTo(width.toFloat(), buttonBottom.toFloat())
                            lineTo(currentLeft, buttonBottom.toFloat())
                            lineTo(currentLeft, buttonTop.toFloat())
                            lineTo(width.toFloat(), buttonTop.toFloat())
                            close()
                        }
                    } else {
                        currentRight = hiddenButtonRight + v
                        loginOrX = endLoginOrX + v
                        currentLoginX = endLoginTextX + v

                        loginButtonPath.apply {
                            reset()
                            moveTo(0f, buttonBottom.toFloat())
                            lineTo(currentRight, buttonBottom.toFloat())
                            lineTo(currentRight, buttonTop.toFloat())
                            lineTo(0f, buttonTop.toFloat())
                            close()
                        }
                    }
                    invalidate()
                }
                buttonBounce.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    private val startButtonRight = resources.getDimensionPixelOffset(R.dimen.bottom_width).toFloat()

    private fun dpToPixels(dp: Int) = resources.displayMetrics.density * dp


    fun setOnButtonSwitched(callback: OnButtonSwitchedListener?) {
        this.callback = callback
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setOnClickListener(l: OnClickListener?) {
        setOnTouchListener { v: View?, event: MotionEvent ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (isLogin && loginButtonOutline.contains(x, y)) {
                if (event.action == KeyEvent.ACTION_UP) {
                    l?.onClick(v)
                }
                return@setOnTouchListener true
            } else if (!isLogin && loginTextOutline.contains(x, y)) {
                if (event.action == KeyEvent.ACTION_UP) {
                    onLoginListener.login()
                }
                return@setOnTouchListener true
            } else if (isLogin && signUpTextOutline.contains(x, y)) {
                if (event.action == KeyEvent.ACTION_UP) {
                    onSignUpListener.signUp()
                }
                return@setOnTouchListener true
            } else {
                if (!isLogin && signUpButtonOutline.contains(x, y)) {
                    if (event.action == KeyEvent.ACTION_UP) {
                        l?.onClick(v)
                    }
                    return@setOnTouchListener true
                } else {
                    return@setOnTouchListener false
                }
            }
        }
    }

    fun setOnSignUpListener(listener: OnSignUpListener) {
        onSignUpListener = listener
    }

    fun setOnLoginListener(listener: OnLoginListener) {
        onLoginListener = listener
    }

    private fun getString(stringId: Int): String {
        return context.getString(stringId).toUpperCase(Locale.ROOT)
    }
}