package com.example.smartdevice;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class PlayerStat {
    public float currentHP, maxHP;
    public int damage;
    public int healPerSec;
    public float reloadSpeed;
    public float bulletSpeed;
    public int fireBulletCount;
    public int piercingPercent;

    public PlayerStat(){
        currentHP = maxHP = 1;
        damage = 100;
        healPerSec = 0;
        reloadSpeed = 3f;
        bulletSpeed = 10f;
        fireBulletCount = 1;
        piercingPercent = 0;
    }
}

public class Player implements OnAttackedListener, OnEnemyDefeatedListener { // OnEnemyDefeatedListener 구현 추가
    // 플레이어
    private float playerX, playerY;
    private float speed = 12f;
    private float moveX = 0, moveY = 0;
    private float exp = 0, maxExp = 1000;
    private int level = 1;
    private boolean isAlive = true;
    private PlayerStat stat = new PlayerStat();


    // 총알
    private static final int MAX_BULLETS = 1000;
    private List<Bullet> bullets = new ArrayList<>();
    private long lastShotTime = 0;

    // ------------------------

    // Setters and Getters
    public void setPlayerX(float playerX) {
        this.playerX = playerX;
    }
    public void setPlayerY(float playerY) {
        this.playerY = playerY;
    }
    public float getPlayerX() { return playerX; }
    public float getPlayerY() { return playerY; }
    public void setMovePos(float moveX, float moveY) {
        this.moveX = moveX;
        this.moveY = moveY;
    }
    public List<Bullet> getBullets() { return bullets; }
    public PlayerStat getStat() { return stat; }
    public Player() {
        // 총알 초기화
        for (int i = 0; i < MAX_BULLETS; i++) {
            bullets.add(new Bullet());
        }
    }

    // Update player position based on joystick input
    public void update() {
        // 플레이어 이동
        playerX += moveX * speed;
        playerY += moveY * speed;

        autoShoot();
    }

    // ========== ★ 자동 공격 (투사체 발사) =============
    private void autoShoot() {
        long now = System.currentTimeMillis();
        float shotInterval = stat.reloadSpeed * 1000;
        if (now - lastShotTime < shotInterval) return;

        lastShotTime = now;

        Enemy target = getClosestEnemy();
        if (target == null) return;

        shootBullet(target.x, target.y);
    }

    // 가장 가까운 적 찾기
    private Enemy getClosestEnemy() {
        Enemy nearest = null;
        float minDist = Float.MAX_VALUE;

        for (Enemy e : MainSingleton.enemy.getEnemies()) {
            if (!e.isAlive) continue;

            float dx = e.x - playerX;
            float dy = e.y - playerY;
            float dist = dx * dx + dy * dy;

            if (dist < minDist) {
                minDist = dist;
                nearest = e;
            }
        }
        return nearest;
    }

    // 투사체 발사 (3방향으로 수정됨)
    private void shootBullet(float tx, float ty) {
        // 기본 방향 벡터 계산
        float dx = tx - playerX;
        float dy = ty - playerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        // 정규화된 방향 벡터 (길이가 1인 벡터)
        float dirX = dx / dist;
        float dirY = dy / dist;

        // 발사할 각도 배열 (-15도, 0도, 15도)
        float[] angles = {0, -5, 5, -10, 10, -15, 15};

        // 발사할 총알 개수
        int bulletsToFire = stat.fireBulletCount;
        int firedCount = 0; // 실제로 발사된 총알 수

        Random random = new Random();
        // 사용 가능한 총알을 찾아서 3개 발사
        for (Bullet b : bullets) {
            if (firedCount >= bulletsToFire) {
                break; // 3개를 모두 발사했으면 루프 중단
            }

            if (!b.active) {
                // 현재 발사할 총알의 각도 (라디안으로 변환)
                float angleRad = (float) Math.toRadians(angles[firedCount]);

                // 2D 회전 행렬을 이용해 방향 벡터 회전
                // newDirX = dirX * cos(a) - dirY * sin(a)
                // newDirY = dirX * sin(a) + dirY * cos(a)
                float rotatedDirX = dirX * (float) Math.cos(angleRad) - dirY * (float) Math.sin(angleRad);
                float rotatedDirY = dirX * (float) Math.sin(angleRad) + dirY * (float) Math.cos(angleRad);

                float speed = stat.bulletSpeed;
                System.out.println("speed : " + speed);

                float x = playerX;
                float y = playerY;
                float vx = rotatedDirX * speed;
                float vy = rotatedDirY * speed;

                boolean pierce = false;
                if((random.nextInt(0,100) + 1) < stat.piercingPercent)
                    pierce = true;

                b.Fire(x, y, vx, vy, stat.damage, pierce);

                firedCount++; // 발사된 총알 수 증가
            }
        }
    }

    @Override
    public void OnAttacked(int damage) {
        if (!isAlive) return;

        stat.currentHP -= damage;
        if (stat.currentHP <= 0) {
            stat.currentHP = 0;

            Died();
        }

        float ratio = stat.currentHP / stat.maxHP;
        MainSingleton.game.ui.updateHP(ratio);
    }

    public void Died(){
        isAlive = false;
        MainSingleton.game.setGameOver();
    }

    // ★ 콜백 메서드: 적이 처치되었을 때 호출됨
    @Override
    public void onEnemyDefeated(float expReward) {
        if (!isAlive) return; // 죽었으면 경험치 획득 안함

        this.exp += expReward;
        System.out.println("적 처치! +" + expReward + " EXP. 현재 경험치: " + this.exp + "/" + this.maxExp);


        // 레벨업 체크
        if (this.exp >= this.maxExp) {
            levelUP();
        }

        // 경험치 UI 업데이트
        float ratio = this.exp / this.maxExp;
        MainSingleton.game.ui.updateEXP(ratio);
    }

    /**
     * 레벨업 시 호출되는 함수
     */
    public void levelUP(){
        // 레벨 1 증가
        this.level++;

        // 현재 경험치에서 최대 경험치를 차감
        this.exp -= this.maxExp;

        // 다음 레벨업에 필요한 경험치 증가
        this.maxExp *= 1.2f;

        System.out.println("LEVEL UP! 현재 레벨: " + this.level);
        // 여기에 레벨업 시 스탯 상승, 스킬 선택 등의 로직을 추가할 수 있습니다.
        MainSingleton.game.onLevelUp();
    }
}
