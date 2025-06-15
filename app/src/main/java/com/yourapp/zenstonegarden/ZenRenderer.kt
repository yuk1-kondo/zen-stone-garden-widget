package com.yourapp.zenstonegarden

import android.content.Context
import android.graphics.*
import android.util.Log
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random

class ZenRenderer {
    
    companion object {
        private const val GRID_SIZE = 32
        private const val BITMAP_SIZE = 512
        private const val ANIMATION_CYCLE_DURATION = 6000L // 6秒で1サイクル
        private const val MAX_RIPPLE_RADIUS = 15f // グリッドベースのサイズ
        private const val RIPPLE_WIDTH = 1.5f
        private const val DOT_APPEAR_DURATION = 0.1f // 全体の10%でドット表示
        private const val RIPPLE_START_DURATION = 0.15f // 15%で波紋開始
        private const val DOT_DISAPPEAR_DURATION = 0.25f // 25%でドット消失
    }
    
    fun generateZenGarden(): Bitmap {
        return try {
            val currentTime = System.currentTimeMillis()
            val cyclePosition = (currentTime % ANIMATION_CYCLE_DURATION).toFloat() / ANIMATION_CYCLE_DURATION
            
            // デバッグログ
            Log.d("ZenRenderer", "Animation cycle: $cyclePosition")
            
            val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }
            
            val step = BITMAP_SIZE / GRID_SIZE
            
            // 水滴の位置を固定（画面中央）
            val dropX = GRID_SIZE / 2
            val dropY = GRID_SIZE / 2
            
            // 時刻に応じた背景色を設定
            val backgroundColor = getBackgroundColor()
            canvas.drawColor(backgroundColor)
            
            // アニメーションフェーズの実行
            when {
                cyclePosition <= DOT_APPEAR_DURATION -> {
                    // フェーズ1: ドットが現れる
                    val dotAlpha = (cyclePosition / DOT_APPEAR_DURATION)
                    drawDot(canvas, paint, dropX, dropY, step, dotAlpha)
                    Log.d("ZenRenderer", "Phase 1: Dot appearing, alpha: $dotAlpha, dropX: $dropX, dropY: $dropY")
                }
                
                cyclePosition <= RIPPLE_START_DURATION -> {
                    // フェーズ2: ドット表示中、波紋開始準備
                    drawDot(canvas, paint, dropX, dropY, step, 1.0f)
                    Log.d("ZenRenderer", "Phase 2: Dot visible, dropX: $dropX, dropY: $dropY")
                }
                
                cyclePosition <= DOT_DISAPPEAR_DURATION -> {
                    // フェーズ3: 波紋開始、ドット徐々に消失
                    val rippleProgress = (cyclePosition - RIPPLE_START_DURATION) / (DOT_DISAPPEAR_DURATION - RIPPLE_START_DURATION)
                    val dotAlpha = 1.0f - rippleProgress
                    val rippleRadius = rippleProgress * MAX_RIPPLE_RADIUS * 0.3f // 初期の小さな波紋
                    
                    if (dotAlpha > 0) {
                        drawDot(canvas, paint, dropX, dropY, step, dotAlpha)
                    }
                    drawRipple(canvas, paint, dropX, dropY, rippleRadius, step, 1.0f)
                    Log.d("ZenRenderer", "Phase 3: Ripple starting, radius: $rippleRadius, dot alpha: $dotAlpha")
                }
                
                else -> {
                    // フェーズ4: 波紋のみ、画面外まで拡大
                    val rippleProgress = (cyclePosition - DOT_DISAPPEAR_DURATION) / (1.0f - DOT_DISAPPEAR_DURATION)
                    val rippleRadius = 0.3f * MAX_RIPPLE_RADIUS + rippleProgress * MAX_RIPPLE_RADIUS * 0.7f
                    val rippleAlpha = 1.0f - smoothStep(rippleProgress)
                    
                    if (rippleAlpha > 0.1f) {
                        drawRipple(canvas, paint, dropX, dropY, rippleRadius, step, rippleAlpha)
                    }
                    Log.d("ZenRenderer", "Phase 4: Ripple expanding, radius: $rippleRadius, alpha: $rippleAlpha")
                }
            }
            
            bitmap
        } catch (e: Exception) {
            Log.e("ZenRenderer", "Error generating zen garden", e)
            // フォールバック: シンプルな単色ビットマップを返す
            createFallbackBitmap()
        }
    }
    
    private fun createFallbackBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // フォールバック時もNothingスタイルの黒背景
        canvas.drawColor(Color.BLACK)
        return bitmap
    }
    
    private fun smoothStep(t: Float): Float {
        // 滑らかなイージング関数（より自然なフェードアウト）
        return t * t * (3f - 2f * t)
    }
    
    private fun getBackgroundColor(): Int {
        // Nothingスタイル: 美しい黒背景
        return Color.BLACK
    }
    
    private fun drawDot(canvas: Canvas, paint: Paint, centerX: Int, centerY: Int, step: Int, alpha: Float) {
        // Nothingスタイル: 純白のドット
        val finalAlpha = (alpha * 255).toInt().coerceIn(0, 255)
        paint.color = Color.argb(finalAlpha, 255, 255, 255)
        paint.style = Paint.Style.FILL
        
        val centerXf = centerX * step + step / 2f
        val centerYf = centerY * step + step / 2f
        val radius = step * 1.2f // さらに大きなドット
        
        Log.d("ZenRenderer", "Drawing dot: centerXf=$centerXf, centerYf=$centerYf, radius=$radius, alpha=$finalAlpha")
        canvas.drawCircle(centerXf, centerYf, radius, paint)
        
        // より目立たせるために外周を描画
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.argb((finalAlpha * 0.8f).toInt().coerceIn(0, 255), 255, 255, 255)
        canvas.drawCircle(centerXf, centerYf, radius + 2f, paint)
    }
    
    private fun drawRipple(canvas: Canvas, paint: Paint, centerX: Int, centerY: Int, rippleRadius: Float, step: Int, alpha: Float) {
        if (rippleRadius <= 0f || alpha <= 0f) return
        
        val centerXf = centerX * step + step / 2f
        val centerYf = centerY * step + step / 2f
        val radiusInPixels = rippleRadius * step
        
        // より明確なアルファ値
        val finalAlpha = (alpha * 255).toInt().coerceIn(0, 255)
        
        Log.d("ZenRenderer", "Drawing ripple: centerXf=$centerXf, centerYf=$centerYf, radius=$radiusInPixels, alpha=$finalAlpha")
        
        // 外側の波紋
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        paint.color = Color.argb(finalAlpha, 255, 255, 255)
        canvas.drawCircle(centerXf, centerYf, radiusInPixels, paint)
        
        // 中間の波紋
        if (rippleRadius > 2f) {
            val middleRadius = radiusInPixels * 0.7f
            val middleAlpha = (alpha * 180).toInt().coerceIn(0, 255)
            paint.strokeWidth = 3f
            paint.color = Color.argb(middleAlpha, 255, 255, 255)
            canvas.drawCircle(centerXf, centerYf, middleRadius, paint)
        }
        
        // 内側の波紋
        if (rippleRadius > 1f) {
            val innerRadius = radiusInPixels * 0.4f
            val innerAlpha = (alpha * 120).toInt().coerceIn(0, 255)
            paint.strokeWidth = 2f
            paint.color = Color.argb(innerAlpha, 255, 255, 255)
            canvas.drawCircle(centerXf, centerYf, innerRadius, paint)
        }
    }
    
    fun generateTouchRipple(touchXRatio: Float, touchYRatio: Float, touchTime: Long): Bitmap {
        return try {
            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - touchTime
            val progress = (elapsed.toFloat() / 3000f).coerceIn(0f, 1f) // 3秒で完了
            
            Log.d("ZenRenderer", "Touch ripple progress: $progress at ($touchXRatio, $touchYRatio)")
            
            val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }
            
            // 背景色
            canvas.drawColor(getBackgroundColor())
            
            // タッチ位置をグリッド座標に変換
            val touchGridX = (touchXRatio * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val touchGridY = (touchYRatio * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
            val step = BITMAP_SIZE / GRID_SIZE
            
            when {
                progress <= 0.1f -> {
                    // 初期ドット表示
                    val dotAlpha = progress / 0.1f
                    drawDot(canvas, paint, touchGridX, touchGridY, step, dotAlpha)
                }
                
                progress <= 0.3f -> {
                    // ドット消失、小さな波紋開始
                    val rippleProgress = (progress - 0.1f) / 0.2f
                    val dotAlpha = 1f - rippleProgress
                    val rippleRadius = rippleProgress * MAX_RIPPLE_RADIUS * 0.5f
                    
                    if (dotAlpha > 0) {
                        drawDot(canvas, paint, touchGridX, touchGridY, step, dotAlpha)
                    }
                    drawTouchRipple(canvas, paint, touchGridX, touchGridY, rippleRadius, step, 1f)
                }
                
                else -> {
                    // 大きな波紋の拡大とフェード
                    val rippleProgress = (progress - 0.3f) / 0.7f
                    val rippleRadius = 0.5f * MAX_RIPPLE_RADIUS + rippleProgress * MAX_RIPPLE_RADIUS * 1.5f
                    val rippleAlpha = 1f - smoothStep(rippleProgress * 0.8f)
                    
                    if (rippleAlpha > 0.1f) {
                        drawTouchRipple(canvas, paint, touchGridX, touchGridY, rippleRadius, step, rippleAlpha)
                    }
                }
            }
            
            bitmap
        } catch (e: Exception) {
            Log.e("ZenRenderer", "Error generating touch ripple", e)
            createFallbackBitmap()
        }
    }
    
    private fun drawTouchRipple(canvas: Canvas, paint: Paint, centerX: Int, centerY: Int, rippleRadius: Float, step: Int, alpha: Float) {
        if (rippleRadius <= 0f || alpha <= 0f) return
        
        val centerXf = centerX * step + step / 2f
        val centerYf = centerY * step + step / 2f
        val radiusInPixels = rippleRadius * step
        
        val finalAlpha = (alpha * 255).toInt().coerceIn(0, 255)
        
        Log.d("ZenRenderer", "Drawing touch ripple: centerXf=$centerXf, centerYf=$centerYf, radius=$radiusInPixels, alpha=$finalAlpha")
        
        // より強い波紋エフェクト（タッチ用）
        paint.style = Paint.Style.STROKE
        
        // 外側の波紋（太く明るく）- 青白い光
        paint.strokeWidth = 8f
        paint.color = Color.argb(finalAlpha, 200, 220, 255) // 薄い青白
        canvas.drawCircle(centerXf, centerYf, radiusInPixels, paint)
        
        // 中間の波紋 - 純白
        if (rippleRadius > 2f) {
            val middleRadius = radiusInPixels * 0.75f
            val middleAlpha = (alpha * 220).toInt().coerceIn(0, 255)
            paint.strokeWidth = 5f
            paint.color = Color.argb(middleAlpha, 255, 255, 255)
            canvas.drawCircle(centerXf, centerYf, middleRadius, paint)
        }
        
        // 内側の波紋 - 少し暖かい白
        if (rippleRadius > 1f) {
            val innerRadius = radiusInPixels * 0.5f
            val innerAlpha = (alpha * 180).toInt().coerceIn(0, 255)
            paint.strokeWidth = 3f
            paint.color = Color.argb(innerAlpha, 255, 250, 240) // 暖かい白
            canvas.drawCircle(centerXf, centerYf, innerRadius, paint)
        }
        
        // 中心の強いドット（タッチポイント強調）
        if (rippleRadius < MAX_RIPPLE_RADIUS * 0.5f) {
            paint.style = Paint.Style.FILL
            val dotAlpha = (alpha * 200).toInt().coerceIn(0, 255)
            paint.color = Color.argb(dotAlpha, 255, 255, 255)
            canvas.drawCircle(centerXf, centerYf, step * 0.4f, paint)
            
            // 中心にハイライト
            paint.color = Color.argb((dotAlpha * 0.8f).toInt().coerceIn(0, 255), 180, 200, 255)
            canvas.drawCircle(centerXf, centerYf, step * 0.2f, paint)
        }
    }
    
    fun generateMultipleRipples(ripples: List<InteractiveZenView.TouchRipple>): Bitmap {
        return try {
            val bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply {
                isAntiAlias = true
            }
            
            // 背景色
            canvas.drawColor(getBackgroundColor())
            
            val step = BITMAP_SIZE / GRID_SIZE
            val currentTime = System.currentTimeMillis()
            
            Log.d("ZenRenderer", "Drawing ${ripples.size} touch ripples")
            
            // 各タッチ波紋を描画
            ripples.forEach { ripple ->
                val elapsed = currentTime - ripple.startTime
                val progress = (elapsed.toFloat() / 3000f).coerceIn(0f, 1f)
                
                // タッチ位置をグリッド座標に変換
                val touchGridX = (ripple.x * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
                val touchGridY = (ripple.y * GRID_SIZE).toInt().coerceIn(0, GRID_SIZE - 1)
                
                when {
                    progress <= 0.1f -> {
                        // ドット表示
                        val dotAlpha = progress / 0.1f
                        drawDot(canvas, paint, touchGridX, touchGridY, step, dotAlpha)
                    }
                    
                    progress <= 0.25f -> {
                        // 小さな波紋開始
                        val rippleProgress = (progress - 0.1f) / 0.15f
                        val dotAlpha = 1f - rippleProgress
                        val rippleRadius = rippleProgress * MAX_RIPPLE_RADIUS * 0.4f
                        
                        if (dotAlpha > 0) {
                            drawDot(canvas, paint, touchGridX, touchGridY, step, dotAlpha * 0.8f)
                        }
                        drawTouchRipple(canvas, paint, touchGridX, touchGridY, rippleRadius, step, 1f)
                    }
                    
                    else -> {
                        // 大きな波紋の拡大
                        val rippleProgress = (progress - 0.25f) / 0.75f
                        val rippleRadius = 0.4f * MAX_RIPPLE_RADIUS + rippleProgress * MAX_RIPPLE_RADIUS * 1.8f
                        val rippleAlpha = 1f - smoothStep(rippleProgress * 0.9f)
                        
                        if (rippleAlpha > 0.05f) {
                            drawTouchRipple(canvas, paint, touchGridX, touchGridY, rippleRadius, step, rippleAlpha)
                        }
                    }
                }
            }
            
            bitmap
        } catch (e: Exception) {
            Log.e("ZenRenderer", "Error generating multiple ripples", e)
            createFallbackBitmap()
        }
    }
}
