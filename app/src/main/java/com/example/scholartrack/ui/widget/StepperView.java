package com.example.scholartrack.ui.widget;

// Unit 3: StepperView – custom View that draws a vertical stage stepper
//         (circles on a line; completed stages filled green, current stage primary-blue).
//         Demonstrates custom View, onDraw with Canvas, Paint.

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class StepperView extends View {

    private String[] stageLabels = new String[0];
    private boolean[] done       = new boolean[0];
    private int       currentStage = 0;

    private final Paint circlePaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint numberPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int COLOR_COMPLETED = Color.parseColor("#4CAF50");
    private static final int COLOR_CURRENT   = Color.parseColor("#1A4D8F");
    private static final int COLOR_PENDING   = Color.parseColor("#BDBDBD");
    private static final int COLOR_LINE      = Color.parseColor("#E0E0E0");
    private static final int CIRCLE_RADIUS   = 36;
    private static final int ROW_HEIGHT      = 120;
    private static final int LEFT_PADDING    = 80;
    private static final int TEXT_LEFT       = LEFT_PADDING + CIRCLE_RADIUS + 20;

    public StepperView(Context context) { super(context); init(); }
    public StepperView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
    public StepperView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); init();
    }

    private void init() {
        linePaint.setStrokeWidth(4f);
        linePaint.setColor(COLOR_LINE);

        numberPaint.setColor(Color.WHITE);
        numberPaint.setTextSize(28f);
        numberPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.setColor(Color.parseColor("#1B2430"));
        textPaint.setTextSize(30f);
    }

    public void setStages(String[] labels, boolean[] done, int currentStage) {
        this.stageLabels   = labels;
        this.done          = done;
        this.currentStage  = currentStage;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = stageLabels.length * ROW_HEIGHT + 40;
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = stageLabels.length;

        for (int i = 0; i < n; i++) {
            int cx = LEFT_PADDING;
            int cy = i * ROW_HEIGHT + ROW_HEIGHT / 2;

            // Connector line (drawn before circle so circle overlaps it)
            if (i < n - 1) {
                linePaint.setColor(done != null && i < done.length && done[i]
                        ? COLOR_COMPLETED : COLOR_LINE);
                canvas.drawLine(cx, cy + CIRCLE_RADIUS,
                        cx, cy + ROW_HEIGHT - CIRCLE_RADIUS, linePaint);
            }

            // Circle
            boolean isDone    = done != null && i < done.length && done[i];
            boolean isCurrent = (i + 1) == currentStage;
            circlePaint.setColor(isDone ? COLOR_COMPLETED
                    : isCurrent ? COLOR_CURRENT : COLOR_PENDING);
            canvas.drawCircle(cx, cy, CIRCLE_RADIUS, circlePaint);

            // Stage number inside circle
            String num = String.valueOf(i + 1);
            Rect bounds = new Rect();
            numberPaint.getTextBounds(num, 0, num.length(), bounds);
            canvas.drawText(num, cx, cy + bounds.height() / 2f, numberPaint);

            // Stage label
            textPaint.setColor(isDone ? COLOR_COMPLETED
                    : isCurrent ? COLOR_CURRENT : Color.parseColor("#9E9E9E"));
            textPaint.setTextSize(isCurrent ? 34f : 28f);
            canvas.drawText(stageLabels[i], TEXT_LEFT, cy + 10f, textPaint);
        }
    }
}
