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
        private const val RIPPLE_CYCLE_DURATION = 10000L // 10秒で1サイクル
        private const val MAX_RIPPLE_RADIUS = 18f
        private const val RIPPLE_ALPHA_THRESHOLD = 50
        private const val FIXED_STONE_COUNT = 5
    }
    
    fun generateZenGarden(context: Context): Bitmap {
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        val step = BITMAP_SIZE / GRID_SIZE
        
        // 時間ベースで波紋の位置と進行状況を計算
        val currentTime = System.currentTimeMillis()
        val cyclePosition = (currentTime % RIPPLE_CYCLE_DURATION).toFloat() / RIPPLE_CYCLE_DURATION
        
        // 固定の石の位置を時間ベースのシードで決定（10秒ごとに新しい配置）
        val dropCycle = currentTime / RIPPLE_CYCLE_DURATION
        val dropRandom = Random(dropCycle)
        val centerX = dropRandom.nextInt(4, GRID_SIZE - 4)
        val centerY = dropRandom.nextInt(4, GRID_SIZE - 4)
        
        // 波紋の半径を時間に基づいて滑らかに計算
        val rippleRadius = if (cyclePosition < 0.8f) {
            // 最初の8秒で波紋が広がる
            val progress = cyclePosition / 0.8f
            val smoothProgress = smoothStep(progress)
            smoothProgress * MAX_RIPPLE_RADIUS
        } else {
            // 最後の2秒で波紋が消える
            val fadeProgress = (cyclePosition - 0.8f) / 0.2f
            val fadeSmooth = 1f - smoothStep(fadeProgress)
            fadeSmooth * MAX_RIPPLE_RADIUS
        }
        
        // 時刻に応じた背景色を設定
        val backgroundColor = getBackgroundColor()
        canvas.drawColor(backgroundColor)
        
        // 固定の石を描画（変わらない位置）
        drawFixedStones(canvas, paint, step, dropRandom)
        
        // 波紋効果を描画（時間ベース）
        drawTimeBasedRipples(canvas, paint, centerX, centerY, rippleRadius, step, cyclePosition)
        
        // 波紋の中心に水滴の石を描画
        if (cyclePosition < 0.9f) {
            drawDropStone(canvas, paint, centerX, centerY, step, cyclePosition)
        }
        
        return bitmap
    }
    
    private fun smoothStep(t: Float): Float {
        // 滑らかなイージング関数
        return t * t * (3f - 2f * t)
    }
    
    private fun getBackgroundColor(): Int {
        val hour = LocalTime.now().hour
        return when (hour) {
            in 6..17 -> Color.WHITE // 朝昼：白
            in 18..19 -> 0xFFFFE0B2.toInt() // 夕：橙
            else -> 0xFF111133.toInt() // 夜：濃紺
        }
    }
    
    private fun drawTimeBasedRipples(canvas: Canvas, paint: Paint, centerX: Int, centerY: Int, rippleRadius: Float, step: Int, cyclePosition: Float) {
        if (rippleRadius <= 0f) return
        
        for (x in 0 until GRID_SIZE) {
            for (y in 0 until GRID_SIZE) {
                val dx = (x - centerX).toFloat()
                val dy = (y - centerY).toFloat()
                val distance = sqrt(dx * dx + dy * dy)
                
                // 波紋の幅を狭くして、より繊細な表現に
                val rippleWidth = 2f
                val distanceFromRipple = abs(distance - rippleRadius)
                
                if (distanceFromRipple < rippleWidth) {
                    // 波紋の強度を計算（中心から離れるほど弱くなる）
                    val rippleIntensity = (1f - distanceFromRipple / rippleWidth)
                    
                    // サイクル位置に基づいて透明度を調整
                    val timeAlpha = if (cyclePosition < 0.8f) {
                        1f
                    } else {
                        1f - smoothStep((cyclePosition - 0.8f) / 0.2f)
                    }
                    
                    val alpha = (rippleIntensity * timeAlpha * 180).toInt().coerceIn(0, 255)
                    
                    if (alpha > RIPPLE_ALPHA_THRESHOLD) {
                        paint.color = Color.argb(alpha, 30, 30, 30)
                        val left = x * step.toFloat()
                        val top = y * step.toFloat()
                        val right = (x + 1) * step.toFloat()
                        val bottom = (y + 1) * step.toFloat()
                        canvas.drawRect(left, top, right, bottom, paint)
                    }
                }
            }
        }
    }
    
    private fun drawFixedStones(canvas: Canvas, paint: Paint, step: Int, random: Random) {
        paint.color = Color.argb(180, 60, 60, 60)
        
        // 固定された数の石を配置
        repeat(FIXED_STONE_COUNT) {
            val stoneX = random.nextInt(2, GRID_SIZE - 2)
            val stoneY = random.nextInt(2, GRID_SIZE - 2)
            val left = stoneX * step.toFloat()
            val top = stoneY * step.toFloat()
            val right = (stoneX + 1) * step.toFloat()
            val bottom = (stoneY + 1) * step.toFloat()
            
            // 石を少し丸みを帯びた形で描画
            canvas.drawRoundRect(left, top, right, bottom, step * 0.3f, step * 0.3f, paint)
        }
    }
    
    private fun drawDropStone(canvas: Canvas, paint: Paint, centerX: Int, centerY: Int, step: Int, cyclePosition: Float) {
        // 水滴が落ちる石の大きさをアニメーション
        val stoneAlpha = if (cyclePosition < 0.1f) {
            // 最初の1秒で水滴が現れる
            (cyclePosition * 10f).coerceIn(0f, 1f)
        } else {
            1f
        }
        
        paint.color = Color.argb((stoneAlpha * 120).toInt(), 20, 20, 20)
        val left = centerX * step.toFloat()
        val top = centerY * step.toFloat()
        val right = (centerX + 1) * step.toFloat()
        val bottom = (centerY + 1) * step.toFloat()
        
        // 中央の石を円形で描画
        val centerXf = left + step / 2f
        val centerYf = top + step / 2f
        val radius = step * 0.4f * stoneAlpha
        canvas.drawCircle(centerXf, centerYf, radius, paint)
    }
}
