package com.jinwei.lunarclock.widget;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
/**
 * Created by jinwei on 16-4-6.
 */
public class ClockProvider  extends AppWidgetProvider {

    // 刷新的时候执行
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // TODO Auto-generated method stub
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    // 第一个添加到屏幕上
    @Override
    public void onEnabled(Context context) {
        // TODO Auto-generated method stub
        super.onEnabled(context);
        // 启动Service
        context.startService(new Intent(context, ClockService.class));
    }

    // 最后一个widget从屏幕移除
    @Override
    public void onDisabled(Context context) {
        // TODO Auto-generated method stub
        super.onDisabled(context);
        context.stopService(new Intent(context, ClockService.class));
    }

    // 从屏幕移除
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // TODO Auto-generated method stub
        super.onDeleted(context, appWidgetIds);
    }
}
