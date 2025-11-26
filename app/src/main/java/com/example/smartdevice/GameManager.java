package com.example.smartdevice;

import android.app.Activity; // Activity import
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameManager {
    public static class GameManagerInitParam {
        View gameOverLayout, upgradeLayout, hudLayout;
        TextView gameOverText;
        View hpBar, expBar;

        LinearLayout[] item;
        TextView[] itemText;
        ImageView[] itemImg;
        EditText nameText;
        TextView[] names, scores, playtimes;
        Button[] gameOverButtons;

        // 생성자 수정
        public GameManagerInitParam(View hudLayout, View gameOverLayout, View upgradeLayout, TextView gameOverText, View hpBar, View expBar, LinearLayout[] item, TextView[] texts, ImageView[] imgs, EditText nameText, TextView[] names, TextView[] scores, TextView[] playtimes, Button[] gameOverButtons) {
            this.hudLayout = hudLayout;
            this.gameOverLayout = gameOverLayout;
            this.upgradeLayout = upgradeLayout;
            this.gameOverText = gameOverText;
            this.hpBar = hpBar;
            this.expBar = expBar;
            this.item = item;
            this.itemText = texts;
            this.itemImg = imgs;
            this.nameText = nameText;
            this.names = names;
            this.scores = scores;
            this.playtimes = playtimes;
            this.gameOverButtons = gameOverButtons;
        }
    }
    private Activity activity;
    private Player player;
    public UIManager ui;
    public UpgradeManager upgrade;

    // 타이머 관련 변수 추가
    private long gameStartTime;
    private boolean isGameOver = false;
    private int score = 0;
    public void Init(GameManagerInitParam param, Activity activity) {
        this.activity = activity;
        player = new Player();
        upgrade = new UpgradeManager();
        ui = new UIManager();
        ui.Init(param, this.activity);
        StartGame();
    }

    public void StartGame() {
        score = 0;
        isGameOver = false;
        // 게임 시작 시간을 현재 시간으로 설정
        gameStartTime = System.currentTimeMillis();
        Log.d("GameManager", "Game Started. Score and Time reset.");
    }
    public void ConnectAttackListener(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            enemy.setOnAttackedListener(player);
            enemy.setOnDefeatedListener(player);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void startGameTimer() {
        if (gameStartTime == 0) {
            gameStartTime = System.currentTimeMillis();
        }
        isGameOver = false;
    }

    // ★★★ 수정된 부분 ★★★
    public void setGameOver() {
        if (isGameOver) return; // 게임오버가 중복 호출되는 것을 방지this.isGameOver = true; // 게임오버 상태로 변경
        MainSingleton.gameView.pause(); // 게임 뷰(스레드) 일시정지

        // UIManager를 통해 게임오버 화면을 표시하도록 요청
        showGameOver();
    }

    public void showGameOver(){
        String finalTime = getFormattedElapsedTime();
        // UIManager가 Activity 참조를 통해 runOnUiThread를 호출하도록 함
        ui.showGameOver(finalTime);
    }

    public void savePlayerRanking(String username) {
        if (username == null || username.trim().isEmpty()) {
            Log.e("GameManager", "Username is empty. Cannot save ranking.");
            return;
        }

        // 현재 점수와 플레이 시간을 가져와 NetworkManager에 저장을 요청
        MainSingleton.network.SaveRanking(username, getScore(), getFormattedElapsedTime());
    }

    public String getFormattedElapsedTime() {
        // ... (기존 코드와 동일) ...
        long elapsedTimeMs = System.currentTimeMillis() - gameStartTime;
        if(isGameOver) { // 게임 오버 시에는 시간이 더이상 흐르지 않도록
            elapsedTimeMs = System.currentTimeMillis() - gameStartTime;
        }

        long seconds = (elapsedTimeMs / 1000) % 60;
        long minutes = (elapsedTimeMs / (1000 * 60)) % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    // ★★★ 여기에 새 함수 추가 ★★★
    // 레벨업 시 호출될 함수
    public void onLevelUp() {
        // 1. 게임을 일시정지
        MainSingleton.gameView.pause();
        // 2. 업그레이드 매니저에게 업그레이드 창을 보여달라고 요청
        upgrade.showUpgradeOptions();
    }

    public void Resume(){
        MainSingleton.gameView.resume();
    }

    // 점수 관련 함수
    public void raiseScore(int value){
        score += value;
    }

    public String getFormattedScore(){
        return String.format("%d", score);
    }

    public int getScore(){
        return score;
    }
}
