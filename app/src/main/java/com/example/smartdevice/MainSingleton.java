package com.example.smartdevice;

import android.app.Activity;
import android.content.Context;
import android.view.WindowManager;
import android.view.WindowMetrics;

public class MainSingleton {
    static MainSingleton main = null;
    static EnemyManager enemy = new EnemyManager();
    static GameManager game = new GameManager();
    static NetworkManager network = new NetworkManager();

    static GameView gameView;

    public static void Init(Activity activity, GameView viewInstance, EnemyManager.EnemyManagerInitParam enemyParam, GameManager.GameManagerInitParam gameParam) {
        if(main == null){
            main = new MainSingleton();
        }
        gameView = viewInstance;
        enemy.Init(enemyParam);
        game.Init(gameParam, activity);
    }
}

