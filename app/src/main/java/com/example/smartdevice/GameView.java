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



    // 플레이어
    Player player;

    // 조이스틱
    private boolean joystickActive = false;
    private float joystickCenterX, joystickCenterY;
    private float joystickCurrentX, joystickCurrentY;
    private final float joystickRadius = 120f;
    private final float stickRadius = 50f;
    float moveX, moveY;

    // 카메라
    private float offsetX, offsetY;


    // java
    public GameView(Context context) {
        super(context);
        // 화면 크기
        WindowMetrics metrics = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getCurrentWindowMetrics();
        screenX = metrics.getBounds().width();
        screenY = metrics.getBounds().height();

        paint = new Paint();
        holder = getHolder();
    }

    public void Init(){
        // 플레이어 초기화
        player = MainSingleton.game.getPlayer();
        player.setPlayerX(MainActivity.WORLD_WIDTH / 2f); // 월드 중앙에서 시작
        player.setPlayerY(MainActivity.WORLD_HEIGHT / 2f); // 월드 중앙에서 시작
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
        // 플레이어 업데이트
        player.update();

        // 플레이어 이동 범위 제한 로직 추가
        float playerRadius = 40; // draw에서 사용하는 플레이어 반지름
        player.setPlayerX(Math.max(playerRadius, Math.min(player.getPlayerX(), MainActivity.WORLD_WIDTH - playerRadius)));
        player.setPlayerY(Math.max(playerRadius, Math.min(player.getPlayerY(), MainActivity.WORLD_HEIGHT - playerRadius)));

        // 적들 업데이트
        MainSingleton.enemy.updateAll(player.getPlayerX(), player.getPlayerY());

        // 투사체 업데이트
        for (Bullet b : player.getBullets()) {
            b.update();
        }

        // 카메라 위치 계산
        offsetX = player.getPlayerX() - screenX / 2f;
        offsetY = player.getPlayerY() - screenY / 2f;

        // 카메라 이동 범위 제한
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


    // ============= 그리기 ==================
    private void draw() {
        if (!holder.getSurface().isValid()) return;

        canvas = holder.lockCanvas();
        canvas.drawColor(Color.BLACK);

        // 플레이어
        paint.setColor(Color.RED);
        canvas.drawCircle(player.getPlayerX() - offsetX, player.getPlayerY() - offsetY, 40, paint);

        // 적
        paint.setColor(Color.YELLOW);
        for (Enemy e : MainSingleton.enemy.getEnemies()) {
            if (e.isAlive)
            {
                paint.setColor(Color.BLUE);
                canvas.drawCircle(e.x - offsetX, e.y - offsetY, 30, paint);
            }
        }

        // 투사체
        paint.setColor(Color.CYAN);
        for (Bullet b : player.getBullets()) {
            if (b.active)
                canvas.drawCircle(b.x - offsetX, b.y - offsetY, 12, paint);
        }

        // 조이스틱
        if (joystickActive) {
            paint.setColor(Color.argb(120, 255, 255, 255));
            canvas.drawCircle(joystickCenterX, joystickCenterY, joystickRadius, paint);

            paint.setColor(Color.argb(200, 200, 200, 200));
            canvas.drawCircle(joystickCurrentX, joystickCurrentY, stickRadius, paint);
        }

        // ★ 경과 시간 텍스트 그리기
        paint.setColor(Color.WHITE);
        paint.setTextSize(60f);
        paint.setTextAlign(Paint.Align.CENTER); // 텍스트 중앙 정렬
        String elapsedTime = MainSingleton.game.getFormattedElapsedTime();
        canvas.drawText(elapsedTime, screenX / 2f - 100f, 200, paint);
        String score = MainSingleton.game.getFormattedScore();
        canvas.drawText(score, screenX / 2f + 100f, 200, paint);

        holder.unlockCanvasAndPost(canvas);
    }

    // FPS 조절
    private void control() {
        try { Thread.sleep(16); } catch (Exception ignored) {}
    }

    public void pause() {
        isPlaying = false;
    }

    // play() 메서드 수정
    public void resume() {
        // isPlaying이 true이거나 스레드가 이미 살아있으면 아무것도 하지 않습니다.
        if (isPlaying || (gameThread != null && gameThread.isAlive())) {
            return;
        }

        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();

        // ★ GameManager를 통해 게임 타이머 시작
        MainSingleton.game.startGameTimer();
    }
}
