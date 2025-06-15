package com.yourapp.zenstonegarden

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView

class InteractiveZenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    
    private val zenRenderer = ZenRenderer()
    private val touchRipples = mutableListOf<TouchRipple>()
    private var isManualTouch = false
    
    data class TouchRipple(
        val x: Float,
        val y: Float,
        val startTime: Long,
        var isActive: Boolean = true
    )
    
    companion object {
        private const val TOUCH_RIPPLE_DURATION = 3000L // 3秒でタッチ波紋完了
        private const val MAX_CONCURRENT_RIPPLES = 5 // 最大同時波紋数
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 新しいタッチ波紋を追加
                val currentTime = System.currentTimeMillis()
                val touchRipple = TouchRipple(
                    x = event.x / width.toFloat(),
                    y = event.y / height.toFloat(),
                    startTime = currentTime
                )
                
                // 最大数を超えた場合、古い波紋を削除
                if (touchRipples.size >= MAX_CONCURRENT_RIPPLES) {
                    touchRipples.removeAt(0)
                }
                
                touchRipples.add(touchRipple)
                isManualTouch = true
                
                Log.d("InteractiveZenView", "Touch detected at: (${touchRipple.x}, ${touchRipple.y})")
                
                // 即座にビットマップを更新
                updateBitmap()
                
                // タッチフィードバック
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
    
    fun updateBitmap() {
        try {
            // 期限切れの波紋を削除
            val currentTime = System.currentTimeMillis()
            touchRipples.removeAll { currentTime - it.startTime > TOUCH_RIPPLE_DURATION }
            
            val bitmap = if (isManualTouch && touchRipples.isNotEmpty()) {
                // 複数のタッチ波紋を生成
                zenRenderer.generateMultipleRipples(touchRipples)
            } else {
                // 通常の自動アニメーション
                isManualTouch = false
                zenRenderer.generateZenGarden()
            }
            
            setImageBitmap(bitmap)
            
            // すべての波紋が終了したかチェック
            if (touchRipples.isEmpty()) {
                isManualTouch = false
                Log.d("InteractiveZenView", "All ripples completed, returning to auto mode")
            }
            
        } catch (e: Exception) {
            Log.e("InteractiveZenView", "Error updating bitmap", e)
        }
    }
    
    fun resetToAutoMode() {
        isManualTouch = false
        touchRipples.clear()
    }
}
