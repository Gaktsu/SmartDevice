package com.example.smartdevice;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

class UpgradeOption {
    public enum UpgradeType {
        DAMAGE,
        BULLET_SPEED,
        RELOAD_SPEED,
        MAXHP,
        HP_RECOVERY,
        HEAL,
        INCREASE_BULLET,
        PIERCING
    }

    UpgradeType itemType;
    String itemText;
    int imageResId;
    boolean isMaxLevel = false;
    int maxLevel;
    int level = 0;

    public UpgradeOption(UpgradeType itemType, String itemText, int imageResId, int maxLevel) {
        this.itemType = itemType;
        this.itemText = itemText;
        this.imageResId = imageResId;
        this.maxLevel = maxLevel;
    }
}


public class UpgradeManager {

    private List<UpgradeOption> options = new ArrayList<>(Arrays.asList(
        new UpgradeOption(UpgradeOption.UpgradeType.DAMAGE, "공격력 증가", android.R.drawable.ic_menu_add, 100),
        new UpgradeOption(UpgradeOption.UpgradeType.BULLET_SPEED , "투사체 속도 증가", android.R.drawable.ic_menu_report_image, 10),
        new UpgradeOption(UpgradeOption.UpgradeType.RELOAD_SPEED , "발사 속도 증가", android.R.drawable.ic_media_ff, 10),
        new UpgradeOption(UpgradeOption.UpgradeType.MAXHP , "최대 체력 증가", android.R.drawable.ic_media_ff, 100),
        new UpgradeOption(UpgradeOption.UpgradeType.HP_RECOVERY , "초당 회복량 증가", android.R.drawable.ic_media_ff, 30),
        new UpgradeOption(UpgradeOption.UpgradeType.HEAL, "체력 회복", android.R.drawable.ic_menu_myplaces, Integer.MAX_VALUE),
        new UpgradeOption(UpgradeOption.UpgradeType.INCREASE_BULLET, "투사체 개수 증가", android.R.drawable.ic_menu_myplaces, 3),
        new UpgradeOption(UpgradeOption.UpgradeType.PIERCING, "관통 확률 증가", android.R.drawable.ic_menu_myplaces, 30)
    ));

    public void showUpgradeOptions() {


        UpgradeOption[] currentOptions = new UpgradeOption[3];
        boolean[] alreadySelected = new boolean[options.size()];

        for(int i = 0; i < options.size(); i++){
            if(options.get(i).level >= options.get(i).maxLevel)
                alreadySelected[i] = true;
        }

        int currentIdx = 0;
        Random random = new Random();
        for(;currentIdx < currentOptions.length;){
            int idx = random.nextInt(options.size());
            if(alreadySelected[idx]) continue;

            currentOptions[currentIdx] = options.get(idx);
            alreadySelected[idx] = true;
            currentIdx++;
        }

        MainSingleton.game.ui.showUpgrade(currentOptions);
    }

    public void selectOption(UpgradeOption option){
        PlayerStat stat = MainSingleton.game.getPlayer().getStat();
        switch (option.itemType) {
            case DAMAGE:
                stat.damage += 100;
                break;
            case RELOAD_SPEED:
                stat.reloadSpeed -= 0.3f;
                break;
            case BULLET_SPEED:
                stat.bulletSpeed += 3f;
            case HEAL:
                stat.currentHP = Math.min(stat.maxHP, stat.currentHP + 100);
                break;
            case MAXHP:
                stat.maxHP += 100;
                break;
            case HP_RECOVERY:
                stat.healPerSec = 1;
                break;
            case INCREASE_BULLET:
                stat.fireBulletCount += 2;
                break;
            case PIERCING:
                stat.piercingPercent = 10;
                break;
        }
        option.level++;
        MainSingleton.game.ui.updateHP(stat.currentHP/ stat.maxHP);
    }
}
