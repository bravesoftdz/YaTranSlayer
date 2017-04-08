package com.example.bio.yatranslayer;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.rollbar.android.Rollbar;

import ru.yandex.speechkit.SpeechKit;

import static com.example.bio.yatranslayer.CONSTS.ROLLBAR_API_KEY;
import static com.example.bio.yatranslayer.CONSTS.YANDEX_SPEECH_KIT_API_KEY;


public class MainActivity extends AppCompatActivity {

    FragmentHomepage fragmentHomepage;
    FragmentHistory fragmentHistory;
    FragmentOptions fragmentOptions;

    final static String TAG_1 = "FragmentHomepage";
    final static String TAG_2 = "FragmentHistory";
    final static String TAG_3 = "FragmentOptions";
    final static String TAG_4 = "FragmentChooseLanguages";




    private FragmentManager mFragmentManager;

    final static String _defaultFullLanguageName = "_defaultFullLanguageName";
    final static String _defaultShortLanguageName = "_defaultShortLanguageName";


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            boolean b = false;

            android.app.FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.animator.anim1, R.animator.anim1);

            switch (item.getItemId()) {
                case R.id.navigation_homepage:
                    FragmentHomepage fragmentHomepage1 = (FragmentHomepage) mFragmentManager.findFragmentById(R.layout.fragment_homepage);

                    if (fragmentHomepage1 == null) {
                        fragmentTransaction.replace(R.id.container, fragmentHomepage, TAG_1);
                        b = true;
                    }

                    break;

                case R.id.navigation_memopage:
                    FragmentHistory fragmentMemo1 = (FragmentHistory) mFragmentManager.findFragmentById(R.layout.fragment_history);

                    if (fragmentMemo1 == null) {
                        fragmentTransaction.replace(R.id.container, fragmentHistory, TAG_2);
                        b = true;
                    }

                    break;

                case R.id.navigation_options:
                    FragmentOptions fragmentOptions1 = (FragmentOptions) mFragmentManager.findFragmentById(R.layout.fragment_options);

                    if (fragmentOptions1 == null) {
                        fragmentTransaction.replace(R.id.container, fragmentOptions, TAG_3);
                        b = true;
                    }

                    break;
            }

            fragmentTransaction.commit();
            return b;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {
        }

        setContentView(R.layout.activity_main);

        try {
            // инициализация журнала падений Rollbar.com
            Rollbar.init(this, ROLLBAR_API_KEY, "production");
            // включаем отправку Logcat
            Rollbar.setIncludeLogcat(true);
            // оно по дефолту true, на всякий случай)
            Rollbar.setSendOnUncaughtException(true);
        } catch (Exception ignored) {
        }

        SpeechKit.getInstance().configure(getApplicationContext(), YANDEX_SPEECH_KIT_API_KEY);

        SharedPreferencesClass.init(this);


        // заполняю бд
        // список поддерживаемых языков короткий, длинный и направление перевода
        yandexAPI yandexAPI = new yandexAPI();
        yandexAPI.init(getApplicationContext());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        fragmentHomepage = new FragmentHomepage();
        fragmentHistory = new FragmentHistory();
        fragmentOptions = new FragmentOptions();

        mFragmentManager = getFragmentManager();

        if (savedInstanceState == null) {
            // добавляем фрагмент
            android.app.FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.animator.anim1, R.animator.anim1);

            fragmentTransaction.add(R.id.container, fragmentHomepage, TAG_1);
            fragmentTransaction.commit();
        }

    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

    }
}
