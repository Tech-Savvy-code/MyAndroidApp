package com.example.tusomeapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class CircularProgressBar extends View {
    private Paint paint;
    private float progress = 0;

    public CircularProgressBar(Context context) {
        super(context);
        init();
    }

    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.red_orange)); // Use the orange color from colors.xml
        paint.setStrokeWidth(8); // Set progress width
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float angle = 360 * (progress / 100);
        canvas.drawArc(0, 0, getWidth(), getHeight(), -90, angle, false, paint);
    }
}