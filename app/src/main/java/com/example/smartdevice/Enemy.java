package com.example.smartdevice;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Enemy {

    public float x, y;
    public float speed = 4f;
    public float radius = 40f;
    public boolean isAlive = true;

    private float maxHP = 100f;
    private float currentHP;

    public OnEnemyDefeatedListener enemyDefeatedListener;
    private OnAttackedListener attackedListener;

    private long lastAttackTime = 0;
    private long attackInterval = 1000;
    private int attackDamage = 10;

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
        this.currentHP = this.maxHP;
    }

    public void update(float playerX, float playerY) {
        if (!isAlive) return;

        float dx = playerX - x;
        float dy = playerY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }

        if (distance < radius + 30f) {
            long now = System.currentTimeMillis();
            if (now - lastAttackTime > attackInterval) {
                lastAttackTime = now;
                attack();
            }
        }
    }

    void attack() {
        if (attackedListener != null) {
            attackedListener.OnAttacked(attackDamage);
        }
    }

    public void attacked(int damage) {
        if (!isAlive) return;

        currentHP -= damage;
        if (currentHP <= 0) {
            currentHP = 0;
            killed();
        }
    }

    public void setOnAttackedListener(OnAttackedListener listener) {
        this.attackedListener = listener;
    }

    public void setOnDefeatedListener(OnEnemyDefeatedListener listener) {
        this.enemyDefeatedListener = listener;
    }

    public void killed() {
        isAlive = false;

        if(enemyDefeatedListener != null) {
            enemyDefeatedListener.onEnemyDefeated(300);
            MainSingleton.game.raiseScore(100);
        }
    }

    public void respawn(float newX, float newY) {
        x = newX;
        y = newY;
        isAlive = true;
        currentHP = maxHP;
    }
}
