package com.example.smartdevice;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Enemy {

    public float x, y;
    public float speed = 4f;
    public float radius = 40f;
    public boolean isAlive = true;

    // 체력 변수 추가
    private float maxHP = 100f;
    private float currentHP;

    // 이벤트 관련
    public OnEnemyDefeatedListener enemyDefeatedListener;
    private OnAttackedListener attackedListener;

    // 공격 관련
    private long lastAttackTime = 0;
    private long attackInterval = 1000; // 1초마다 공격
    private int attackDamage = 10;

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
        this.currentHP = this.maxHP; // 생성 시 체력 초기화
    }

    // 캐릭터를 향해 이동
    public void update(float playerX, float playerY) {
        if (!isAlive) return; // 죽었으면 업데이트 중지

        float dx = playerX - x;
        float dy = playerY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }

        // 근접 시 공격
        if (distance < radius + 30f) { // 30f는 근접 판정 거리
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

    /**
     * 적이 공격받았을 때 호출되는 함수
     * @param damage 받은 데미지
     */
    public void attacked(int damage) {
        if (!isAlive) return; // 이미 죽은 적은 공격받지 않음

        currentHP -= damage;
        if (currentHP <= 0) {
            currentHP = 0;
            killed(); // 체력이 0 이하가 되면 사망 처리
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
            enemyDefeatedListener.onEnemyDefeated(300); // 경험치 300 전달
            MainSingleton.game.raiseScore(100); // 점수 100 전달
        }
    }

    public void respawn(float newX, float newY) {
        x = newX;
        y = newY;
        isAlive = true;
        currentHP = maxHP; // 리스폰 시 체력을 최대로 회복
    }
}
