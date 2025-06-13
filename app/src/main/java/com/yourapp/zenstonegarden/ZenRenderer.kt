package com.yourapp.zenstonegarden

import android.content.Context
import android.graphics.*
import java.time.LocalTime
import kotlin.math.*
import kotlin.random.Random

class ZenRenderer {
    
    companion object {
        private const val GRID_SIZE = 32
        private const val BITMAP_SIZE = 512
        private const val MIN_RIPPLE_RADIUS = 4f
        private const val MAX_RIPPLE_RADIUS = 16f
        private const val RIPPLE_ALPHA_THRESHOLD = 180
    }
    
    fun generateZenGarden(context: Context): Bitmap {
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        val step = BITMAP_SIZE / GRID_SIZE
        
        // ランダムな波紋の中心点を生成
        val centerX = Random.nextInt(0, GRID_SIZE)
        val centerY = Random.nextInt(0, GRID_SIZE)
        val rippleRadius = Random.nextFloat() * (MAX_RIPPLE_RADIUS - MIN_RIPPLE_RADIUS) + MIN_RIPPLE_RADIUS
        
        // 時刻に応じた背景色を設定
        val backgroundColor = getBackgroundColor()
        canvas.drawColor(backgroundColor)
        
        // 波紋効果を描画
        drawRipples(canvas, paint, centerX, centerY, rippleRadius, step)
        
        // 固定の石を描画（グリッド上のランダムな位置）
        drawFixedStones(canvas, paint, step)
        
        // 波紋の中心に石を描画
        drawCenterStone(canvas, paint, centerX, centerY, step)
        
        return bitmap
    }
    
    private fun getBackgroundColor(): Int {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 6..17 -> Color.WHITE // 朝昼：白
            in 18..19 -> 0xFFFFE0B2.toInt() // 夕：橙
            else -> 0xFF111133.toInt() // 夜：濃紺
        }
    }
    
    private fun drawRipples(canvas: Canvas, paint: Paint, centerX: Int, centerY: Int, rippleRadius: Float, step: Int) {
        for (x in 0 until GRID_SIZE) {
            for (y in 0 until GRID_SIZE) {
                val dx = (x - centerX).toFloat()
                val dy = (y - centerY).toFloat()
                val distance = sqrt(dx * dx + dy * dy)
                
                // 波紋の透明度を計算（距離に基づく）
                val alpha = ((1f - abs(distance - rippleRadius)).coerceIn(0f, 1f) * 255).toInt()
                
                if (alpha > RIPPLE_ALPHA_THRESHOLD) {
                    paint.color = Color.argb(alpha, 0, 0, 0)
                    val left = x * step.toFloat()
                    val top = y * step.toFloat()
                    val right = (x + 1) * step.toFloat()
                    val bottom = (y + 1) * step.toFloat()
                    canvas.drawRect(left, top, right, bottom, paint)
                }
            }
        }
    }
    
    private fun drawFixedStones(canvas: Canvas, paint: Paint, step: Int) {
        paint.color = Color.BLACK
        val stoneCount = Random.nextInt(3, 8) // 3-7個の石をランダム配置
        
        repeat(stoneCount) {
            val stoneX = Random.nextInt(0, GRID_SIZE)
            val stoneY = Random.nextInt(0, GRID_SIZE)
            val left = stoneX * step.toFloat()
            val top = stoneY * step.toFloat()
            val right = (stoneX + 1) * step.toFloat()
            val bottom = (stoneY + 1) * step.toFloat()
            
            // 石を少し丸みを帯びた形で描画
            canvas.drawRoundRect(left, top, right, bottom, step * 0.2f, step * 0.2f, paint)
        }
    }
    
    private fun drawCenterStone(canvas: Canvas, paint: Paint, centerX: Int, centerY: Int, step: Int) {
        paint.color = Color.BLACK
        val left = centerX * step.toFloat()
        val top = centerY * step.toFloat()
        val right = (centerX + 1) * step.toFloat()
        val bottom = (centerY + 1) * step.toFloat()
        
        // 中央の石を特別な形で描画（円形）
        val centerXf = left + step / 2f
        val centerYf = top + step / 2f
        canvas.drawCircle(centerXf, centerYf, step * 0.4f, paint)
    }
}
