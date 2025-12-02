package com.example.smartdevice;

import android.os.Bundle;
import android.util.Log; // ★★★ 올바른 Log 클래스로 수정 ★★★
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List; // java.util.List 사용

// ★★★ Retrofit 관련 클래스들을 올바르게 import ★★★
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public static final int WORLD_WIDTH = 1080 * 3;
    public static final int WORLD_HEIGHT = 2400 * 3;
    private GameView gameView;
    View hpBar, expBar;
    TextView gameOverText;
    TextView[] itemText = new TextView[3];
    ImageView[] itemImg = new ImageView[3];
    LinearLayout[] item = new LinearLayout[3];
    String[] itemTextIdx = {"left", "center", "right"};
    EditText nameText;
    View gameOverLayout, upgradeLayout, hudLayout;
    TextView[] names = new TextView[5], scores = new TextView[5], playtimes = new TextView[5];
    Button[] gameoverButtons = new Button[2];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FrameLayout rootLayout = findViewById(android.R.id.content);

        gameView = new GameView(this);
        rootLayout.addView(gameView, 0);

        hudLayout = findViewById(R.id.hud_layout);
        hpBar = findViewById(R.id.hp_fill);
        expBar = findViewById(R.id.exp_fill);

        gameOverLayout = findViewById(R.id.game_over_layout);
        gameOverText = findViewById(R.id.game_over_text);

        upgradeLayout = findViewById(R.id.upgrade_layout);
        for(int i = 0; i < 3; i++){
            itemText[i] = findViewById(getResources().getIdentifier(itemTextIdx[i] + "Text", "id", getPackageName()));
            itemImg[i] = findViewById(getResources().getIdentifier(itemTextIdx[i] + "Img", "id", getPackageName()));
            item[i] = findViewById(getResources().getIdentifier(itemTextIdx[i] + "Item", "id", getPackageName()));
        }

        nameText = findViewById(R.id.usernameText);

        for(int i = 0; i < 5; i++){
            names[i] = findViewById(getResources().getIdentifier("nameText" + i, "id", getPackageName()));
            scores[i] = findViewById(getResources().getIdentifier("scoreText" + i, "id", getPackageName()));
            playtimes[i] = findViewById(getResources().getIdentifier("playTimeText" + i, "id", getPackageName()));
        }

        gameoverButtons[0] = findViewById(R.id.regame_button);
        gameoverButtons[1] = findViewById(R.id.register_button);

        GameManager.GameManagerInitParam gameParam = new GameManager.GameManagerInitParam(hudLayout, gameOverLayout, upgradeLayout, gameOverText, hpBar, expBar, item, itemText, itemImg, nameText, names, scores, playtimes, gameoverButtons);
        EnemyManager.EnemyManagerInitParam enemyParam = new EnemyManager.EnemyManagerInitParam(WORLD_WIDTH, WORLD_HEIGHT);
        MainSingleton.Init(this, gameView, enemyParam, gameParam);

        gameView.Init();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
}
