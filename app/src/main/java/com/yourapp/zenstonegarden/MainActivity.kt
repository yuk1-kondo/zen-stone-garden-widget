package com.yourapp.zenstonegarden

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var previewImageView: ImageView
    private lateinit var refreshButton: Button
    private lateinit var addWidgetButton: Button
    private lateinit var infoTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupClickListeners()
        updatePreview()
    }
    
    private fun initViews() {
        previewImageView = findViewById(R.id.preview_image)
        refreshButton = findViewById(R.id.refresh_button)
        addWidgetButton = findViewById(R.id.add_widget_button)
        infoTextView = findViewById(R.id.info_text)
        
        infoTextView.text = getString(R.string.app_description)
    }
    
    private fun setupClickListeners() {
        refreshButton.setOnClickListener {
            updatePreview()
        }
        
        addWidgetButton.setOnClickListener {
            openWidgetPicker()
        }
    }
    
    private fun updatePreview() {
        val zenRenderer = ZenRenderer()
        val zenBitmap = zenRenderer.generateZenGarden(this)
        previewImageView.setImageBitmap(zenBitmap)
    }
    
    private fun openWidgetPicker() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, ZenWidgetProvider::class.java)
        
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(componentName, null, null)
        } else {
            // フォールバック: ウィジェット選択画面を開く説明を表示
            val intent = Intent("android.appwidget.action.APPWIDGET_PICK")
            startActivity(intent)
        }
    }
}
