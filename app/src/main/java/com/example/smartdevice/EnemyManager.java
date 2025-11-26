package com.example.smartdevice;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyManager {
    public static class EnemyManagerInitParam{
        int areaWidth;
        int areaHeight;

        public EnemyManagerInitParam(int areaWidth, int areaHeight) {
            this.areaWidth = areaWidth;
            this.areaHeight = areaHeight;
        }
    }
    private float playerX, playerY;
    private final List<Enemy> enemies = new ArrayList<>();
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int areaWidth;
    private int areaHeight;
    private long respawnIntervalMs;
    private int initialSpawnCount = 1000;
    private int spawnedCount = 0;
    private final int spawnBatchSize = 10;
    // spawnInitialRunnable에서 getOutsidePosition(playerX, playerY, areaWidth, areaHeight) 사용
    private final Runnable spawnInitialRunnable = new Runnable() {
        @Override
        public void run() {
            List<Enemy> currentEnemies = new ArrayList<>(10);
            int toSpawn = Math.min(spawnBatchSize, initialSpawnCount - spawnBatchSize);
            for (int i = 0; i < toSpawn; i++) {
                float[] pos = getSpawnPosition(playerX, playerY, areaWidth, areaHeight);
                synchronized (enemies) {
                    Enemy enemy = new Enemy(pos[0], pos[1]);
                    enemies.add(enemy);
                    currentEnemies.add(enemy);
                }
                spawnedCount++;

                MainSingleton.game.ConnectAttackListener(currentEnemies);
            }
            if (spawnedCount < initialSpawnCount) {
                handler.postDelayed(this, 5000L);
            }
        }
    };
    private final Runnable respawnRunnable = new Runnable() {
        @Override
        public void run() {
            respawnDeadEnemies();
            handler.postDelayed(this, respawnIntervalMs);
        }
    };

    // 기본: 시작 시 300마리, 5초마다 재생성
    public EnemyManager(){}
    public void Init(EnemyManagerInitParam param) {
        Init(param.areaWidth, param.areaHeight, 1000, 1000L);
    }

    public void Init(int areaWidth, int areaHeight, int initialCount, long respawnIntervalMs) {
        // --- 리셋 로직 추가 ---
        // 1. 예약된 모든 스폰 및 리스폰 작업을 취소합니다. (가장 중요)
        handler.removeCallbacks(spawnInitialRunnable);
        handler.removeCallbacks(respawnRunnable);

        // 2. 동기화 블록 안에서 기존의 모든 적을 리스트에서 제거합니다.
        synchronized (enemies) {
            enemies.clear();
        }
        Log.d("EnemyManager", "All previous enemies cleared. Handler callbacks removed.");

        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
        this.respawnIntervalMs = respawnIntervalMs;
        this.initialSpawnCount = initialCount;
        this.spawnedCount = 0;
        handler.postDelayed(spawnInitialRunnable, 0); // 초기 적 점진적 소환 시작
        handler.postDelayed(respawnRunnable, respawnIntervalMs); // 리스폰 루프 시작
    }

    private float[] getSpawnPosition(float playerX, float playerY, int areaWidth, int areaHeight) {
        int side; // 0:왼쪽, 1:오른쪽, 2:위, 3:아래
        float x, y;

        x = random.nextFloat() * areaWidth;
        y = random.nextFloat() * areaHeight;

        return new float[]{x, y};
    }

    // 죽어있는 적들을 랜덤 위치로 재생성
    // respawnDeadEnemies에서도 getOutsidePosition(playerX, playerY, areaWidth, areaHeight) 사용
    public void respawnDeadEnemies() {
        int respawned = 0;
        synchronized (enemies) {
            for (Enemy e : enemies) {
                if (!e.isAlive && respawned < 10) {
                    float[] pos = getSpawnPosition(playerX, playerY, areaWidth, areaHeight);
                    e.respawn(pos[0], pos[1]);
                    respawned++;
                }
            }
        }
    }

    // 게임 루프에서 호출: 적들 업데이트
    // updateAll에서 플레이어 위치 갱신
    public void updateAll(float playerX, float playerY) {
        this.playerX = playerX;
        this.playerY = playerY;
        synchronized (enemies) {
            for (Enemy e : enemies) {
                if (e.isAlive) {
                    e.update(playerX, playerY);
                }
            }
        }
    }

    // 외부에서 특정 적을 즉시 죽일 때 사용
    public void killEnemy(Enemy enemy) {
        if (enemy != null) {
            enemy.killed();
        }
    }

    // 적 목록 복사본 반환 (안전하게 읽기 전용 사용 가능)
    public List<Enemy> getEnemiesSnapshot() {
        synchronized (enemies) {
            return new ArrayList<>(enemies);
        }
    }

    // 스폰러 정지(액티비티 종료 시 호출)
    public void stop() {
        handler.removeCallbacks(respawnRunnable);
    }

    // Enemy 리스트 반환
    public List<Enemy> getEnemies() {
        return enemies;
    }
}
