package com.yourapp.zenstonegarden

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var previewImageView: InteractiveZenView
    private lateinit var refreshButton: Button
    private lateinit var addWidgetButton: Button
    private lateinit var infoTextView: TextView
    
    // アニメーション用のハンドラー
    private val animationHandler = Handler(Looper.getMainLooper())
    private var animationRunnable: Runnable? = null
    private var isAnimating = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupClickListeners()
        
        // 初期プレビューを表示してからアニメーション開始
        updatePreview()
        
        // 1秒後にアニメーション自動開始
        animationHandler.postDelayed({
            startAnimation()
            refreshButton.text = getString(R.string.animation_stop)
        }, 1000)
    }
    
    private fun initViews() {
        previewImageView = findViewById<InteractiveZenView>(R.id.preview_image)
        refreshButton = findViewById(R.id.refresh_button)
        addWidgetButton = findViewById(R.id.add_widget_button)
        infoTextView = findViewById(R.id.info_text)
        
        infoTextView.text = getString(R.string.app_description)
        refreshButton.text = getString(R.string.animation_start)
    }
    
    private fun setupClickListeners() {
        refreshButton.setOnClickListener {
            if (isAnimating) {
                stopAnimation()
                refreshButton.text = getString(R.string.animation_start)
                // 自動モードに戻す
                previewImageView.resetToAutoMode()
            } else {
                startAnimation()
                refreshButton.text = getString(R.string.animation_stop)
            }
        }
        
        addWidgetButton.setOnClickListener {
            openWidgetPicker()
        }
    }
    
    private fun updatePreview() {
        try {
            Log.d("MainActivity", "Starting preview update...")
            
            previewImageView.updateBitmap()
            Log.d("MainActivity", "Interactive view updated")
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error updating preview", e)
            Toast.makeText(this, "プレビュー更新エラー: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun openWidgetPicker() {
        // ウィジェット選択画面を開く
        val intent = Intent("android.appwidget.action.APPWIDGET_PICK")
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        startActivity(intent)
    }
    
    private fun startAnimation() {
        isAnimating = true
        animationRunnable = object : Runnable {
            override fun run() {
                if (isAnimating) {
                    updatePreview()
                    animationHandler.postDelayed(this, 100) // 100msごとに更新（スムーズなアニメーション）
                }
            }
        }
        animationHandler.post(animationRunnable!!)
        Log.d("MainActivity", "Animation started")
    }
    
    private fun stopAnimation() {
        isAnimating = false
        animationRunnable?.let {
            animationHandler.removeCallbacks(it)
        }
        Log.d("MainActivity", "Animation stopped")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAnimation()
    }
}
