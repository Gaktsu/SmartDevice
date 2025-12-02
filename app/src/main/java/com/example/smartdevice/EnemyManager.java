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

    public EnemyManager(){}
    public void Init(EnemyManagerInitParam param) {
        Init(param.areaWidth, param.areaHeight, 1000, 1000L);
    }

    public void Init(int areaWidth, int areaHeight, int initialCount, long respawnIntervalMs) {

        handler.removeCallbacks(spawnInitialRunnable);
        handler.removeCallbacks(respawnRunnable);

        synchronized (enemies) {
            enemies.clear();
        }
        Log.d("EnemyManager", "All previous enemies cleared. Handler callbacks removed.");

        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
        this.respawnIntervalMs = respawnIntervalMs;
        this.initialSpawnCount = initialCount;
        this.spawnedCount = 0;
        handler.postDelayed(spawnInitialRunnable, 0);
        handler.postDelayed(respawnRunnable, respawnIntervalMs);
    }

    private float[] getSpawnPosition(float playerX, float playerY, int areaWidth, int areaHeight) {
        int side;
        float x, y;

        x = random.nextFloat() * areaWidth;
        y = random.nextFloat() * areaHeight;

        return new float[]{x, y};
    }


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

    public void killEnemy(Enemy enemy) {
        if (enemy != null) {
            enemy.killed();
        }
    }

    public List<Enemy> getEnemiesSnapshot() {
        synchronized (enemies) {
            return new ArrayList<>(enemies);
        }
    }

    public void stop() {
        handler.removeCallbacks(respawnRunnable);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}
