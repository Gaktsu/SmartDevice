package com.example.smartdevice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowMetrics;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Canvas canvas;
    private Paint paint;
    private int screenX, screenY;



    Player player;

    private boolean joystickActive = false;
    private float joystickCenterX, joystickCenterY;
    private float joystickCurrentX, joystickCurrentY;
    private final float joystickRadius = 120f;
    private final float stickRadius = 50f;
    float moveX, moveY;

    private float offsetX, offsetY;


    // java
    public GameView(Context context) {
        super(context);

        WindowMetrics metrics = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getCurrentWindowMetrics();
        screenX = metrics.getBounds().width();
        screenY = metrics.getBounds().height();

        paint = new Paint();
        holder = getHolder();
    }

    public void Init(){

        player = MainSingleton.game.getPlayer();
        player.setPlayerX(MainActivity.WORLD_WIDTH / 2f);
        player.setPlayerY(MainActivity.WORLD_HEIGHT / 2f);
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {

        player.update();

        float playerRadius = 40;
        player.setPlayerX(Math.max(playerRadius, Math.min(player.getPlayerX(), MainActivity.WORLD_WIDTH - playerRadius)));
        player.setPlayerY(Math.max(playerRadius, Math.min(player.getPlayerY(), MainActivity.WORLD_HEIGHT - playerRadius)));


        MainSingleton.enemy.updateAll(player.getPlayerX(), player.getPlayerY());


        for (Bullet b : player.getBullets()) {
            b.update();
        }

        offsetX = player.getPlayerX() - screenX / 2f;
        offsetY = player.getPlayerY() - screenY / 2f;

        offsetX = Math.max(0, Math.min(offsetX, MainActivity.WORLD_WIDTH - screenX));
        offsetY = Math.max(0, Math.min(offsetY, MainActivity.WORLD_HEIGHT - screenY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                joystickActive = true;
                joystickCenterX = event.getX();
                joystickCenterY = event.getY();
                joystickCurrentX = joystickCenterX;
                joystickCurrentY = joystickCenterY;
                break;

            case MotionEvent.ACTION_MOVE:
                joystickCurrentX = event.getX();
                joystickCurrentY = event.getY();

                float dx = joystickCurrentX - joystickCenterX;
                float dy = joystickCurrentY - joystickCenterY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist > joystickRadius) {
                    dx = dx / dist * joystickRadius;
                    dy = dy / dist * joystickRadius;
                }

                moveX = dx / joystickRadius;
                moveY = dy / joystickRadius;
                break;

            case MotionEvent.ACTION_UP:
                joystickActive = false;
                moveX = 0;
                moveY = 0;

                break;
        }

        player.setMovePos(moveX, moveY);

        return true;
    }

    private void draw() {
        if (!holder.getSurface().isValid()) return;

        canvas = holder.lockCanvas();
        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.RED);
        canvas.drawCircle(player.getPlayerX() - offsetX, player.getPlayerY() - offsetY, 40, paint);

        paint.setColor(Color.YELLOW);
        for (Enemy e : MainSingleton.enemy.getEnemies()) {
            if (e.isAlive)
            {
                paint.setColor(Color.BLUE);
                canvas.drawCircle(e.x - offsetX, e.y - offsetY, 30, paint);
            }
        }

        paint.setColor(Color.CYAN);
        for (Bullet b : player.getBullets()) {
            if (b.active)
                canvas.drawCircle(b.x - offsetX, b.y - offsetY, 12, paint);
        }

        if (joystickActive) {
            paint.setColor(Color.argb(120, 255, 255, 255));
            canvas.drawCircle(joystickCenterX, joystickCenterY, joystickRadius, paint);

            paint.setColor(Color.argb(200, 200, 200, 200));
            canvas.drawCircle(joystickCurrentX, joystickCurrentY, stickRadius, paint);
        }

        paint.setColor(Color.WHITE);
        paint.setTextSize(60f);
        paint.setTextAlign(Paint.Align.CENTER); // 텍스트 중앙 정렬
        String elapsedTime = MainSingleton.game.getFormattedElapsedTime();
        canvas.drawText(elapsedTime, screenX / 2f - 100f, 200, paint);
        String score = MainSingleton.game.getFormattedScore();
        canvas.drawText(score, screenX / 2f + 100f, 200, paint);

        holder.unlockCanvasAndPost(canvas);
    }

    private void control() {
        try { Thread.sleep(16); } catch (Exception ignored) {}
    }

    public void pause() {
        isPlaying = false;
    }

    public void resume() {

        if (isPlaying || (gameThread != null && gameThread.isAlive())) {
            return;
        }

        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();

        MainSingleton.game.startGameTimer();
    }
}
