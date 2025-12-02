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

public class Player implements OnAttackedListener, OnEnemyDefeatedListener {

    private float playerX, playerY;
    private float speed = 12f;
    private float moveX = 0, moveY = 0;
    private float exp = 0, maxExp = 1000;
    private int level = 1;
    private boolean isAlive = true;
    private PlayerStat stat = new PlayerStat();


    private static final int MAX_BULLETS = 1000;
    private List<Bullet> bullets = new ArrayList<>();
    private long lastShotTime = 0;

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

        for (int i = 0; i < MAX_BULLETS; i++) {
            bullets.add(new Bullet());
        }
    }

    public void update() {

        playerX += moveX * speed;
        playerY += moveY * speed;

        autoShoot();
    }

    private void autoShoot() {
        long now = System.currentTimeMillis();
        float shotInterval = stat.reloadSpeed * 1000;
        if (now - lastShotTime < shotInterval) return;

        lastShotTime = now;

        Enemy target = getClosestEnemy();
        if (target == null) return;

        shootBullet(target.x, target.y);
    }

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

    private void shootBullet(float tx, float ty) {
        float dx = tx - playerX;
        float dy = ty - playerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        float dirX = dx / dist;
        float dirY = dy / dist;

        float[] angles = {0, -5, 5, -10, 10, -15, 15};

        int bulletsToFire = stat.fireBulletCount;
        int firedCount = 0;

        Random random = new Random();
        for (Bullet b : bullets) {
            if (firedCount >= bulletsToFire) {
                break;
            }

            if (!b.active) {

                float angleRad = (float) Math.toRadians(angles[firedCount]);

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

                firedCount++;
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

    @Override
    public void onEnemyDefeated(float expReward) {
        if (!isAlive) return;

        this.exp += expReward;
        System.out.println("적 처치! +" + expReward + " EXP. 현재 경험치: " + this.exp + "/" + this.maxExp);


        if (this.exp >= this.maxExp) {
            levelUP();
        }

        float ratio = this.exp / this.maxExp;
        MainSingleton.game.ui.updateEXP(ratio);
    }

    public void levelUP(){
        this.level++;

        this.exp -= this.maxExp;


        this.maxExp *= 1.2f;

        System.out.println("LEVEL UP! 현재 레벨: " + this.level);

        MainSingleton.game.onLevelUp();
    }
}
