package com.jinwei.lunarclock.widget;

import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews.RemoteView;
import com.jinwei.lunarclock.R;

/**
 * This widget display an analogic clock with two hands for hours and minutes.
 * Created by jinwei on 16-4-6.
 */
@RemoteView
public class AnalogLunarClock extends View {
    private Time mCalendar;
    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private Drawable mDial;

    private int mDialWidth;
    private int mDialHeight;

    private boolean mAttached;

    private float mMinutes;
    private float mHour;

    private boolean mChanged;

    // /增加了秒针显示所用到的秒表
    private static String debug = "MyAnalogClock";

    private static int SECONDS_FLAG = 0;
    private Message secondsMsg;
    private float mSeconds;
    // /////end

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0:
                    onTimeChanged();// 重新获取的系统的当前时间，得到时，分，秒
                    invalidate();// 强制绘制，调用自身的onDraw();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public AnalogLunarClock(Context context) {
        this(context, null);
        Log.v(debug, "Start");
    }

    public AnalogLunarClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        Log.v(debug, "Start");

    }

    public AnalogLunarClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Resources r = context.getResources();
        Log.v(debug, "Start");
        if (mDial == null) {
            mDial = r.getDrawable(R.drawable.clock_dial);
        }

        if (mHourHand == null) {
            mHourHand = r.getDrawable(R.drawable.clock_hour);
        }

        if (mMinuteHand == null) {
            mMinuteHand = r.getDrawable(R.drawable.clock_minute);
        }

        mCalendar = new Time();

        mDialWidth = mDial.getIntrinsicWidth();
        mDialHeight = mDial.getIntrinsicHeight();

    }

    @Override
        /*
         * * 吸附到窗体上
         */
    protected void onAttachedToWindow() {
        Log.e(debug, "onAttachedToWindow");
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            // /////////修改到秒针显示后，不需要广播接收器
            // IntentFilter filter = new IntentFilter();
            //
            // filter.addAction(Intent.ACTION_TIME_TICK);// 每隔一分钟会发出这样的一个action
            // filter.addAction(Intent.ACTION_TIME_CHANGED);//
            // 外部修改系统时间，发出这样的action
            // filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);//
            // 外部修改系统的时区，发出action

            // getContext().registerReceiver(mIntentReceiver, filter);
            // null,handler 这两个参数暂时无效，故去掉，
            // ///////end
        }

        // NOTE: It's safe to do these after registering the receiver since the
        // receiver always runs
        // in the main thread, therefore the receiver can't run before this
        // method returns.

        // The time zone may have changed while the receiver wasn't registered,
        // so update the Time
        mCalendar = new Time();

        // Make sure we update to the current time
        onTimeChanged();

        initSecondsThread();
    }

    private void initSecondsThread() {
        secondsMsg = mHandler.obtainMessage(SECONDS_FLAG);
        Thread newThread = new Thread() {
            @Override
            public void run() {
                while (mAttached) {
                    // 如果这个消息不重新获取的话，
                    // 会抛一个this message is already in use 的异常
                    secondsMsg = mHandler.obtainMessage(SECONDS_FLAG);
                    // /end
                    mHandler.sendMessage(secondsMsg);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        newThread.start();

    }

    @Override
        /*
         * * 脱离窗体 在按home按键，不触发这个事件，所以这个应用的监听还是持续监听着。
         * 如果外部修改系统事件，action=Intent.ACTION_TIME_CHANGED 按back按键，触发事件，下次从onCreate从新载入
         */
    protected void onDetachedFromWindow() {
        Log.e(debug, "onDetachedFromWindow");
        super.onDetachedFromWindow();
        if (mAttached) {
            // getContext().unregisterReceiver(mIntentReceiver); 增加秒针，不需要接收器了。
            mAttached = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(debug, "onMeasure");
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float) heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSize((int) (mDialWidth * scale),
                widthMeasureSpec), resolveSize((int) (mDialHeight * scale),
                heightMeasureSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.e(debug, "onSizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e(debug, "canvas");
        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int availableWidth = getWidth();
        int availableHeight = getHeight();

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                    (float) availableHeight / (float) h);
            canvas.save();
            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);
        // //////以上是绘制12个小时背景图

        canvas.save();
        canvas.rotate(mHour / 12.0f * 360.0f, x, y);

        final Drawable hourHand = mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
                    + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();
        // //////以上是绘制时针

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
                    + (h / 2));
        }
        minuteHand.draw(canvas);
        canvas.restore();
        // //////以上是绘制分针

        // /增加秒针的绘制
        canvas.save();
        canvas.rotate(mSeconds / 60.0f * 360.0f, x, y);

        final Drawable secondHand = mMinuteHand;// //用时针来代替秒针
        if (changed) {
            w = secondHand.getIntrinsicWidth();
            h = secondHand.getIntrinsicHeight();
            secondHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y
                    + (h / 2));
        }
        secondHand.draw(canvas);
        canvas.restore();
        // /////end

        if (scaled) {
            canvas.restore();
        }
    }

    /**
     * 改变时间
     */
    private void onTimeChanged() {
        Log.e(debug, "onTimeChanged");
        mCalendar.setToNow();// ///获取手机自身的当前时间，而非实际中的标准的北京时间

        int hour = mCalendar.hour;// 小时
        int minute = mCalendar.minute;// 分钟
        int second = mCalendar.second;// 秒

        mSeconds = second;
        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;

        mChanged = true;
    }

    /**
     * 这个接收器，只接受三个action， 1.Intent.ACTION_TIME_TICK，每分钟发出一次
     * 2.Intent.ACTION_TIME_CHANGE， 外部修改系统时间 3.Intent.ACTION_TIMEZONE_CHANGED
     * 外部系统的时区 按home，还能继续监听 按back，监听销毁
     */
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(debug, "onReceive");
            Log.e(debug, "intent action=" + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
            }
            onTimeChanged();
            invalidate();
        }
    };
}