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

    // 투사체 발사: 위치는 외부에서 설정, 방향과 속도만 지정
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

        // 화면 밖으로 나가면 비활성화 (월드 크기 기준으로 변경)
        if (x < 0 || x > MainActivity.WORLD_WIDTH || y < 0 || y > MainActivity.WORLD_HEIGHT) {
            active = false;
        }

        // 적 충돌 체크
        for (Enemy e : MainSingleton.enemy.getEnemies()) {
            if (!e.isAlive) continue;

            float dx = e.x - x;
            float dy = e.y - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // 충돌 판정
            if (dist < e.radius) {
                if(!pierce)
                    active = false; // 총알 비활성화

                // ★ 변경된 부분: Enemy의 attacked 메서드를 damage와 함께 호출
                e.attacked(this.damage);
                break; // 충돌했으므로 루프 종료
            }
        }
    }

    // 선분-원 교차 판정 함수
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
