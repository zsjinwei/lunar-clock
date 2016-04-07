package com.jinwei.lunarclock.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RemoteViews;
import com.jinwei.lunarclock.R;

/**
 * 更新小组件事件的服务
 * Created by jinwei on 16-4-6.
 */
public class ClockService extends Service {
    //private RemoteViews rViews;
    // 表针
    //private Drawable mHourHand;
    //private Drawable mMinuteHand;
    //private Bitmap mMinuteBitmap;
    //指针Bitmap
    private Bitmap clock_pbm;
    //表盘Bitmap
    //private Bitmap clock_dbm;
    // 定时器
    private Timer timer;
    // 日期格式
    //private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // 时分秒指针paint定义
    Paint sec_paint;
    Paint min_paint;
    Paint hour_paint;
    Paint dial_paint;
    // 时分秒指针长度
    int sec_plen = 72;
    int min_plen = 68;
    int hour_plen = 50;
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        // 秒针指针样式
        sec_paint = new Paint();
        sec_paint.setStyle(Paint.Style.FILL);
        sec_paint.setColor(Color.RED);
        sec_paint.setAntiAlias(true);
        sec_paint.setTypeface(Typeface.MONOSPACE);
        sec_paint.setStrokeWidth(1.8f);

        // 分钟指针样式
        min_paint = new Paint();
        min_paint.setStyle(Paint.Style.FILL);
        min_paint.setColor(Color.WHITE);
        min_paint.setAntiAlias(true);
        min_paint.setTypeface(Typeface.MONOSPACE);
        min_paint.setStrokeWidth(2.4f);

        // 小时指针样式
        hour_paint = new Paint();
        hour_paint.setStyle(Paint.Style.FILL);
        hour_paint.setColor(Color.WHITE);
        hour_paint.setAntiAlias(true);
        hour_paint.setTypeface(Typeface.MONOSPACE);
        hour_paint.setStrokeWidth(2.8f);

        // 表盘样式
        dial_paint = new Paint();
        dial_paint.setStyle(Paint.Style.FILL);
        dial_paint.setAntiAlias(true);
        dial_paint.setTypeface(Typeface.MONOSPACE);

        //rViews = new RemoteViews(getPackageName(),
        //        R.layout.widget_clock);
        clock_pbm = Bitmap.createBitmap(wWidth, wHeight, Bitmap.Config.ARGB_4444);
        // TODO 绘制表盘
        //Drawable clock_dialhand = ContextCompat.getDrawable(this, R.drawable.clock_lunar_dial);
        //clock_dbm = drawableToBitmap(clock_dialhand);
        //Canvas dc = new Canvas();
        //dc.drawBitmap(clock_dbm,0,0,dial_paint);
        //rViews.setImageViewBitmap(R.id.clock_dial, clock_dbm);
        // 刷新
        //AppWidgetManager manager = AppWidgetManager
        //        .getInstance(getApplicationContext());
        //ComponentName cName = new ComponentName(getApplicationContext(),
        //        ClockProvider.class);
        //manager.updateAppWidget(cName, rViews);

        timer = new Timer();
        /**
         * 参数：1.事件2.延时事件3.执行间隔事件
         */
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                updateView();
            }
        }, 0, 1000);
    }

    /**
     * 更新事件的方法
     */
    private void updateView() {
        // 时间
        //String time = sdf.format(new Date());
        /**
         * 参数：1.包名2.小组件布局
         */
        RemoteViews rViews = new RemoteViews(getPackageName(), R.layout.widget_clock);
        if(rViews==null)
            return;
        // 显示当前事件
        //rViews.setTextViewText(R.id.tv_clock, time);
        Calendar calen = Calendar.getInstance();
        int hour = calen.get(Calendar.HOUR_OF_DAY);
        int minute = calen.get(Calendar.MINUTE);
        int second = calen.get(Calendar.SECOND);
        //Bitmap clock_pbm = Bitmap.createBitmap(wWidth, wHeight, Bitmap.Config.ARGB_8888);
        clock_pbm.eraseColor(Color.TRANSPARENT);//清空图形
        //clock_pbm.reconfigure(wWidth, wHeight, Bitmap.Config.ARGB_8888);
        clock_pbm.prepareToDraw();
        drawClockHand(clock_pbm, sec_paint, (int) (second / 60.0f * 360.0f), sec_plen);
        drawClockHand(clock_pbm, min_paint, (int)(minute/60.0f*360.0f+second/60.0f*6.0f), min_plen);
        drawClockHand(clock_pbm, hour_paint, (int)(hour/24.0f*360.0f+minute/60.0f*15.0f), hour_plen);
        rViews.setImageViewBitmap(R.id.clock_pointer, clock_pbm);
        // 刷新
        AppWidgetManager manager = AppWidgetManager
                .getInstance(getApplicationContext());
        ComponentName cName = new ComponentName(getApplicationContext(),
                ClockProvider.class);
        try {
            manager.updateAppWidget(cName, rViews);
        } catch (OutOfMemoryError ex) {
            // 建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。.
            return;
        }

        //if(clock_pbm!=null )
        //    clock_pbm.recycle();
        //manager.updateAppWidget(R.id.minute_clock, rViews);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        timer.cancel();
        timer = null;
        if(clock_pbm!=null && (!clock_pbm.isRecycled()))
            clock_pbm.recycle();
        //if(clock_dbm!=null && (!clock_dbm.isRecycled()))
        //    clock_dbm.recycle();
    }

    private int wHeight = 180;
    private int wWidth = 180;
    public void drawClockHand(Bitmap bmp, Paint paint, int degree, int len){
        int short_len = 6;
        int long_len = len;
        double rdx = degree * Math.PI / 180;
        float x_s = (float)(wWidth/2.0f - short_len * Math.sin(rdx));
        float y_s = (float)(wHeight/2.0f + short_len * Math.cos(rdx));
        float x_e = (float)(wWidth/2.0f + long_len * Math.sin(rdx));
        float y_e = (float)(wHeight/2.0f - long_len * Math.cos(rdx));
        Canvas c = new Canvas(bmp);
        c.drawLine(x_s,y_s,x_e,y_e,paint);
    }

    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    //b.recycle();  //Android开发网再次提示Bitmap操作完应该显示的释放
                    //b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // 建议大家如何出现了内存不足异常，最好return 原始的bitmap对象。.
                return b;
            }
        }
        return b;
    }

    private Drawable zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(drawable);
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
                matrix, true);
        return new BitmapDrawable(null, newbmp);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }
}
