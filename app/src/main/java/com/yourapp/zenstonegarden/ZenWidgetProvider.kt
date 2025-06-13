package com.yourapp.zenstonegarden

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.*
import java.util.concurrent.TimeUnit

class ZenWidgetProvider : AppWidgetProvider() {
    
    companion object {
        private const val ACTION_REFRESH = "com.yourapp.zenstonegarden.REFRESH"
        private const val WORK_TAG = "ZenWidgetWork"
    }
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        
        // 定期更新のワーカーをスケジュール
        schedulePeriodicUpdate(context)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_REFRESH -> {
                // 手動リフレッシュ
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, ZenWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
        }
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicUpdate(context)
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // ワーカーをキャンセル
        WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
    
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.zen_widget)
        
        // 新しい禅庭のビットマップを生成
        val zenRenderer = ZenRenderer()
        val zenBitmap = zenRenderer.generateZenGarden(context)
        
        // ImageViewにビットマップを設定
        views.setImageViewBitmap(R.id.zen_garden_image, zenBitmap)
        
        // タップでリフレッシュできるようにPendingIntentを設定
        val refreshIntent = Intent(context, ZenWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, refreshIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.zen_garden_image, pendingIntent)
        
        // ウィジェットを更新
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun schedulePeriodicUpdate(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<ZenWidgetWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .addTag(WORK_TAG)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}

class ZenWidgetWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    
    override fun doWork(): Result {
        return try {
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val componentName = ComponentName(applicationContext, ZenWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            if (appWidgetIds.isNotEmpty()) {
                val widgetProvider = ZenWidgetProvider()
                widgetProvider.onUpdate(applicationContext, appWidgetManager, appWidgetIds)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
