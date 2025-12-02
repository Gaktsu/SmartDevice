// java
package com.example.smartdevice;

public class Bullet {
    float x, y;
    float vx, vy;
    float traveled;
    float maxDistance = 2000f;
    boolean active = false;
    private int damage;
    private boolean pierce;
    public void Fire(float x, float y, float vx, float vy, int damage, boolean pierce) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.traveled = 0;
        this.active = true;
        this.damage = damage;
        this.pierce = pierce;
    }

    public void update() {
        if (!active) return;

        x += vx;
        y += vy;

        if (x < 0 || x > MainActivity.WORLD_WIDTH || y < 0 || y > MainActivity.WORLD_HEIGHT) {
            active = false;
        }

        for (Enemy e : MainSingleton.enemy.getEnemies()) {
            if (!e.isAlive) continue;

            float dx = e.x - x;
            float dy = e.y - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < e.radius) {
                if(!pierce)
                    active = false;

                e.attacked(this.damage);
                break;
            }
        }
    }

    private boolean segmentCircleIntersect(float x1, float y1, float x2, float y2, float cx, float cy, float r) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float fx = x1 - cx;
        float fy = y1 - cy;

        float a = dx * dx + dy * dy;
        float b = 2 * (fx * dx + fy * dy);
        float c = fx * fx + fy * fy - r * r;

        float discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return false;

        discriminant = (float)Math.sqrt(discriminant);
        float t1 = (-b - discriminant) / (2 * a);
        float t2 = (-b + discriminant) / (2 * a);

        return (t1 >= 0 && t1 <= 1) || (t2 >= 0 && t2 <= 1);
    }
}
