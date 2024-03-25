package com.gttcgf.nanoscan;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MovingSpotsView extends View {

    private List<Spot> spots;
    private Paint paint;
    private Random random;
    private RenderScript rs;
    private ScriptIntrinsicBlur blurScript;
    private Bitmap blurredBitmap;
    private Allocation inputAllocation;
    private Allocation outputAllocation;

    public MovingSpotsView(Context context) {
        super(context);
        init(null, 0);
    }

    public MovingSpotsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MovingSpotsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        spots = new ArrayList<>();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        random = new Random();

        rs = RenderScript.create(getContext());
        blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (blurredBitmap == null) {
            blurredBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas tempCanvas = new Canvas(blurredBitmap);
        tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        for (Spot spot : spots) {
            paint.setColor(spot.color);
            tempCanvas.drawCircle(spot.x, spot.y, spot.radius, paint);
        }

        inputAllocation = Allocation.createFromBitmap(rs, blurredBitmap);
        outputAllocation = Allocation.createTyped(rs, inputAllocation.getType());
        blurScript.setRadius(25);
        blurScript.setInput(inputAllocation);
        blurScript.forEach(outputAllocation);
        outputAllocation.copyTo(blurredBitmap);
// Create a linear gradient from the bottom of the view to the top
//        LinearGradient gradient = new LinearGradient(0, getHeight(), 0, 0, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP);
//        paint.setShader(gradient);

        // Draw the gradient on top of the existing drawing
        tempCanvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        canvas.drawBitmap(blurredBitmap, 0, 0, null);

        moveSpots();
        invalidate(); // Keep redrawing to animate spots
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 创建初始的Spot对象
        for (int i = 0; i < 10; i++) {
            float x = random.nextFloat() * getWidth();
            float y = random.nextFloat() * getHeight();
            float radius = random.nextFloat() * (1000 - 10) + 10; // 生成随机半径
            int color = Color.argb(100, random.nextInt(256), random.nextInt(256), random.nextInt(256));
            spots.add(new Spot(x, y, radius, color));
        }
    }
    public void moveSpots() {
        for (Spot spot : spots) {
            // 为速度添加一个随机的加速度（这里使用-0.5到0.5的范围）
            float ax = random.nextFloat() - 0.5f;
            float ay = random.nextFloat() - 0.5f;

            // 更新速度
            spot.vx += ax;
            spot.vy += ay;

            // 限制速度，以防它变得太快
            float speedLimit = 5.0f;
            spot.vx = Math.max(-speedLimit, Math.min(speedLimit, spot.vx));
            spot.vy = Math.max(-speedLimit, Math.min(speedLimit, spot.vy));

            // 更新位置
            spot.x += spot.vx;
            spot.y += spot.vy;

            // 使斑点保持在视图范围内
            spot.x = Math.max(spot.radius, Math.min(getWidth() - spot.radius, spot.x));
            spot.y = Math.max(spot.radius, Math.min(getHeight() - spot.radius, spot.y));
        }
    }

    private static class Spot {
        float x;
        float y;
        float radius;
        int color;
        float vx; // x方向上的速度
        float vy; // y方向上的速度

        Spot(float x, float y, float radius, int color) {
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.color = color;
            this.vx = 0; // 初始速度为0
            this.vy = 0; // 初始速度为0
        }
    }

    // Method to add a new spot, might be called from outside
    public void addSpot(float x, float y, float radius, int color) {
        spots.add(new Spot(x, y, radius, color));
    }

    // Call this method to clear all spots
    public void clearSpots() {
        spots.clear();
    }

}


