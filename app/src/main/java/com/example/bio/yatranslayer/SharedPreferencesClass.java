package com.example.bio.yatranslayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Locale;

import static com.example.bio.yatranslayer.CONSTS.KEY_CURRENT_LANGUAGE;
import static com.example.bio.yatranslayer.CONSTS.KEY_FAVOURITE_MODE;
import static com.example.bio.yatranslayer.CONSTS.KEY_FROM_LANGUAGE;
import static com.example.bio.yatranslayer.CONSTS.KEY_TO_LANGUAGE;

// простые настройки
class SharedPreferencesClass {
    SharedPreferences sPref;

    // сохранить ключ-значение
    static void saveVariable(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    // загрузить ключ-значение
    static String loadVariable(String key, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String token = prefs.getString(key, null);
        return token;
    }

    // взять текущее направление перевода ru-en
    static String getTranslationDirection(Context context) {
        String fromLanguage = SharedPreferencesClass.loadVariable(KEY_FROM_LANGUAGE, context);
        String toLanguage = SharedPreferencesClass.loadVariable(KEY_TO_LANGUAGE, context);

        return fromLanguage + "-" + toLanguage;
    }

    // инициализация
    static void init(Context context){
        // инит переменных проги, сохранил локаль по умолчанию
        String locale = Locale.getDefault().getLanguage();
        SharedPreferencesClass.saveVariable(KEY_CURRENT_LANGUAGE, locale, context);

//        String curLocale = SharedPreferencesClass.loadVariable(KEY_CURRENT_LANGUAGE, this);
//        if (curLocale == null) { // если ничего нет в настройках, сохраняю по умолчанию
//            SharedPreferencesClass.saveVariable(KEY_CURRENT_LANGUAGE, locale, this);
//        }

        String favouriteMode = SharedPreferencesClass.loadVariable(KEY_FAVOURITE_MODE, context);
        if (favouriteMode == null) {
            SharedPreferencesClass.saveVariable(KEY_FAVOURITE_MODE, "0", context);
        }

        String fromLanguage = SharedPreferencesClass.loadVariable(KEY_FROM_LANGUAGE, context);
        if (fromLanguage == null) {
            fromLanguage = locale;
            SharedPreferencesClass.saveVariable(KEY_FROM_LANGUAGE, fromLanguage, context);

        }

        String toLanguage = SharedPreferencesClass.loadVariable(KEY_TO_LANGUAGE, context);
        if (toLanguage == null) {
            toLanguage = "en";
            if (locale.equals("en")) {
                toLanguage = "ru";
            }
            SharedPreferencesClass.saveVariable(KEY_TO_LANGUAGE, toLanguage, context);
        }
        // конец инит шаред настроек
    }
}

