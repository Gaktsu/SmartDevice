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
        if (isGameOver) return;
        MainSingleton.gameView.pause();

        showGameOver();
    }

    public void showGameOver(){
        String finalTime = getFormattedElapsedTime();

        ui.showGameOver(finalTime);
    }

    public void savePlayerRanking(String username) {
        if (username == null || username.trim().isEmpty()) {
            Log.e("GameManager", "Username is empty. Cannot save ranking.");
            return;
        }

        MainSingleton.network.SaveRanking(username, getScore(), getFormattedElapsedTime());
    }

    public String getFormattedElapsedTime() {
        long elapsedTimeMs = System.currentTimeMillis() - gameStartTime;
        if(isGameOver) {
            elapsedTimeMs = System.currentTimeMillis() - gameStartTime;
        }

        long seconds = (elapsedTimeMs / 1000) % 60;
        long minutes = (elapsedTimeMs / (1000 * 60)) % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public void onLevelUp() {
        MainSingleton.gameView.pause();
        upgrade.showUpgradeOptions();
    }

    public void Resume(){
        MainSingleton.gameView.resume();
    }

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
