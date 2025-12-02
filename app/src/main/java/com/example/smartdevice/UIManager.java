package com.example.smartdevice;

import android.app.Activity; // Activity import
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class UIManager {
    private Activity activity;
    private View gameOverLayout, upgradeLayout, hudLayout;
    private TextView gameOverText;
    private View hpBar, expBar;
    private LinearLayout[] item;
    private TextView[] itemText;
    private ImageView[] itemImg;
    private EditText nameText;
    private TextView[] names, scores, playtimes;
    private Button[] gameOverButtons;

    public void Init(GameManager.GameManagerInitParam param, Activity activity) {
        this.activity = activity;
        this.gameOverLayout = param.gameOverLayout;
        this.upgradeLayout = param.upgradeLayout;
        this.gameOverText = param.gameOverText;
        this.hpBar = param.hpBar;
        this.expBar = param.expBar;
        this.item = param.item;
        this.itemText = param.itemText;
        this.itemImg = param.itemImg;
        this.nameText = param.nameText;
        this.names = param.names;
        this.scores = param.scores;
        this.playtimes = param.playtimes;
        this.gameOverButtons = param.gameOverButtons;
        this.names = param.names;
        this.scores = param.scores;
        this.playtimes = param.playtimes;
        this.hudLayout = param.hudLayout;

        hpBar.setPivotX(0f);
        expBar.setPivotX(0f);
    }

    public void showGameOver(String finalTime) {
        activity.runOnUiThread(() -> {
            if (gameOverText != null) {
                if(hudLayout != null) hudLayout.setVisibility(View.GONE);
                if(hpBar != null) hpBar.setVisibility(View.GONE);
                if(expBar != null) expBar.setVisibility(View.GONE);

                gameOverLayout.setVisibility(View.VISIBLE);
                gameOverText.setText("생존 시간 : " + finalTime);
            }

            gameOverButtons[0].setOnClickListener(v -> {
                Intent intent = activity.getIntent();
                activity.finish();
                activity.startActivity(intent);
            });

            gameOverButtons[1].setOnClickListener(v -> {
                String username = nameText.getText().toString();
                if (username.trim().isEmpty()) {
                    Toast.makeText(activity, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    MainSingleton.game.savePlayerRanking(username);
                    Toast.makeText(activity, "저장되었습니다!", Toast.LENGTH_SHORT).show();
                }
            });

            loadAndDisplayRankings();
        });
    }

    private void loadAndDisplayRankings() {
        MainSingleton.network.GetRanking(new NetworkManager.RankingCallback() {
            @Override
            public void onRankingsReceived(List<Ranking> rankings) {
                activity.runOnUiThread(() -> {
                    int loopCount = Math.min(rankings.size(), names.length);
                    for (int i = 0; i < loopCount; i++) {
                        Ranking rank = rankings.get(i);
                        names[i].setText(rank.username);
                        scores[i].setText(String.valueOf(rank.score));

                        playtimes[i].setText(rank.play_time);
                    }
                });
            }

            @Override
            public void onError(String message) {
                Log.e("APITest", "랭킹 보드 업데이트 실패: " + message);
            }
        });
    }

    public void showUpgrade(UpgradeOption[] options) {
        activity.runOnUiThread(() -> {
            if (upgradeLayout != null) {
                upgradeLayout.setVisibility(View.VISIBLE);
                for(int i = 0; i < 3; i++){
                    final int idx = i;
                    itemText[idx].setText(options[idx].itemText);
                    itemImg[idx].setImageResource(options[idx].imageResId);
                    item[idx].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MainSingleton.game.upgrade.selectOption(options[idx]);
                            upgradeLayout.setVisibility(View.GONE);
                            MainSingleton.game.Resume();
                        }
                    });

                }
            }
        });
    }
    public void updateHP(float ratio) {
        if (hpBar != null) {
            hpBar.setScaleX(ratio);
        }
    }

    public void updateEXP(float ratio) {
        if (expBar != null) {
            expBar.setScaleX(ratio);
        }
    }
}
