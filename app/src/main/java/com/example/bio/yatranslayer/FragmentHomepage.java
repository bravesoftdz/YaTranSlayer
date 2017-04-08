package com.example.bio.yatranslayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.SpeechKit;
import ru.yandex.speechkit.Synthesis;
import ru.yandex.speechkit.Vocalizer;
import ru.yandex.speechkit.VocalizerListener;

import static com.example.bio.yatranslayer.CONSTS.KEY_FROM_LANGUAGE;
import static com.example.bio.yatranslayer.CONSTS.KEY_FROM_LANGUAGE_FULL_NAME;
import static com.example.bio.yatranslayer.CONSTS.KEY_TO_LANGUAGE;
import static com.example.bio.yatranslayer.CONSTS.KEY_TO_LANGUAGE_FULL_NAME;
import static com.example.bio.yatranslayer.CONSTS.YANDEX_SPEECH_KIT_API_KEY;


public class FragmentHomepage extends Fragment implements VocalizerListener {
    //    Activity activity;
//    yandexAPI yaAPIrequest;
//    String eventMessage = null;
//    yandexAPI yandexApi;

    private Vocalizer vocalizer;
    private static final String API_KEY = YANDEX_SPEECH_KIT_API_KEY;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventLanguage event) {
        String language = event.LanguageShortName;
        String message = event.message;
        String key = null;
        String keyFullName = null;

        // вот тут событие выбора языка
        if (event.LanguageShortName != null) {
            DatabaseAdapter databaseAdapter = new DatabaseAdapter(getActivity());
            databaseAdapter.openToRead();
            Map<String, String> map = new HashMap<>();
            map = databaseAdapter.querrySupportedLangMap();
            databaseAdapter.DBclose();

            Toast.makeText(getActivity(), language + ", " + message, Toast.LENGTH_SHORT).show();

            if (message.equals(CONSTS.SOURCE_BUTTON_EVENT_MESSAGE)) {
                key = KEY_FROM_LANGUAGE;
                keyFullName = KEY_FROM_LANGUAGE_FULL_NAME;
            }

            if (message.equals(CONSTS.DESTINATION_BUTTON_EVENT_MESSAGE)) {
                key = KEY_TO_LANGUAGE;
                keyFullName = KEY_TO_LANGUAGE_FULL_NAME;
            }
            String fullName = map.get(language);

            if (key != null) SharedPreferencesClass.saveVariable(key, language, this.getActivity());
            if (keyFullName != null)
                SharedPreferencesClass.saveVariable(keyFullName, fullName, this.getActivity());

            try {
                TextView textViewTranslated = (TextView) getActivity().findViewById(R.id.textViewDestination);
                textViewTranslated.setText("");
            } catch (Exception e) {
            }

            updateFromToButtonsText(getView());
            showInterface();
        }

        // тут событие выбор языка на фрагменте голосового ввода
        if (!message.equals(CONSTS.SOURCE_BUTTON_EVENT_MESSAGE) && !message.equals(CONSTS.DESTINATION_BUTTON_EVENT_MESSAGE)) {
            // send message to edit text, обернуть в функцию ShowText
            try {
                EditText editTextSource = (EditText) getActivity().findViewById(R.id.editTextSource);
                editTextSource.setText(message);
            } catch (Exception e) {
            }
        }

    }


    void updateFromToButtonsText(View view) {
        String fromLang = SharedPreferencesClass.loadVariable(KEY_FROM_LANGUAGE_FULL_NAME, getActivity());
        String toLang = SharedPreferencesClass.loadVariable(KEY_TO_LANGUAGE_FULL_NAME, getActivity());

        try {
            Button buttonFromLanguage = (Button) view.findViewById(R.id.buttonFromLanguage);
            Button buttonToLanguage = (Button) view.findViewById(R.id.buttonToLanguage);

            buttonFromLanguage.setText(fromLang);
            buttonToLanguage.setText(toLang);
        } catch (Exception e) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpeechKit.getInstance().configure(getActivity(), API_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_homepage, null);

        // обработка кнопки Х - очистить входное и выходное текстовые поля
        Button buttonClearSourceText = (Button) view.findViewById(R.id.buttonClearSourceText);
        buttonClearSourceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    EditText editTextSource = (EditText) getActivity().findViewById(R.id.editTextSource);
                    editTextSource.setText("");
                } catch (Exception e) {
                }

                try {
                    TextView textViewTranslated = (TextView) getActivity().findViewById(R.id.textViewDestination);
                    textViewTranslated.setText("");
                } catch (Exception e) {
                }

            }
        });

        final Button buttonSpeakSoucetext = (Button) view.findViewById(R.id.buttonSpeakSouceText);
        buttonSpeakSoucetext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // speakSourcetext

                String langSource = SharedPreferencesClass.loadVariable(KEY_FROM_LANGUAGE, getActivity());

                // только Русский, Английский, Турецкий, Украинский
                if (langSource.equals("ru") ||
                        langSource.equals("en") ||
                        langSource.equals("tr") ||
                        langSource.equals("uk")) {
                    // старт
                    String text = ((EditText) getActivity().findViewById(R.id.editTextSource)).getText().toString();

                    if (TextUtils.isEmpty(text)) {
                        Toast.makeText(getActivity(), "Write smth to be vocalized!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Reset the current vocalizer.
                        resetVocalizer();
                        // To create a new vocalizer, specify the language, the text to be vocalized, the auto play parameter
                        // and the voice.
                        vocalizer = Vocalizer.createVocalizer(Vocalizer.Language.RUSSIAN, text, true, Vocalizer.Voice.ERMIL);
                        // Set the listener.
                        vocalizer.setListener(FragmentHomepage.this);
                        // Don't forget to call start.
                        vocalizer.start();
                    }

                } else {
                    Toast.makeText(getActivity(), "Только рус, англ, укр, тур", Toast.LENGTH_LONG).show();
                }

            }
        });

        /// да это жутко, сократить ))
        final Button buttonSpeakTranslatedText = (Button) view.findViewById(R.id.buttonSpeakTranslatedText);
        buttonSpeakTranslatedText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // speakSourcetext

                String langTranslated = SharedPreferencesClass.loadVariable(KEY_TO_LANGUAGE, getActivity());

                // только Русский, Английский, Турецкий, Украинский
                if (langTranslated.equals("ru") ||
                        langTranslated.equals("en") ||
                        langTranslated.equals("tr") ||
                        langTranslated.equals("uk")) {
                    // старт
                    String text = ((TextView) getActivity().findViewById(R.id.textViewDestination)).getText().toString();

                    if (TextUtils.isEmpty(text)) {
                        Toast.makeText(getActivity(), "Write smth to be vocalized!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Reset the current vocalizer.
                        resetVocalizer();
                        // To create a new vocalizer, specify the language, the text to be vocalized, the auto play parameter
                        // and the voice.
                        vocalizer = Vocalizer.createVocalizer(langTranslated, text, true, Vocalizer.Voice.ALYSS);
                        // Set the listener.
                        vocalizer.setListener(FragmentHomepage.this);
                        // Don't forget to call start.
                        vocalizer.start();
                    }

                } else {
                    Toast.makeText(getActivity(), "Только рус, англ, укр, тур", Toast.LENGTH_LONG).show();
                }

            }
        });

        // нажатие кнопки перевод,
        final Button buttonTranslate = (Button) view.findViewById(R.id.toggleTranslateButton);
        final EditText mEdit = (EditText) view.findViewById(R.id.editTextSource);

        buttonTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // проверка поддерживается ли направление
                yandexAPI yandexApi = new yandexAPI();

                String dir = SharedPreferencesClass.getTranslationDirection(getActivity());
                boolean isDirectionSupportedOK = yandexApi.isDirectionSupported(dir, getActivity());

                String textToTranslate = mEdit.getText().toString();

                if (textToTranslate != null && !textToTranslate.equals("")) {
                    if (isDirectionSupportedOK) {
                        List<String> translatedText = yandexApi.getTranslatedText(textToTranslate, dir);

                        TextView textViewDesdinationOut = (TextView) getActivity().findViewById(R.id.textViewDestination);
                        textViewDesdinationOut.setText(translatedText.toString());
                    } else {
                        // направление перевода не поддерживается
                        Toast.makeText(getActivity(), R.string.translation_dir_not_supported, Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getActivity(), R.string.input_text_is_empty, Toast.LENGTH_LONG).show();
                }
            }
        });
        /////////

        // buttonLanguageHypothesis
        Button buttonLanguageHypothesis = (Button) view.findViewById(R.id.buttonLanguageHypothesis);
        buttonLanguageHypothesis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mEdit = (EditText) view.findViewById(R.id.editTextSource);
                String textToTranslate = mEdit.getText().toString();

                yandexAPI yandexAPI = new yandexAPI();
                String langSuggest = yandexAPI.getHypothesisLanguage(textToTranslate);

                if (langSuggest == null || langSuggest.equals("")) {
                    String helpText = "Type or say something first\nNothing to check";
                    Toast.makeText(getActivity(), helpText, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), langSuggest, Toast.LENGTH_LONG).show();
                }
            }
        });

        // buttonLanguageHypothesis
        Button buttonSaveTranslation = (Button) view.findViewById(R.id.buttonSaveTranslation);
        buttonSaveTranslation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mEdit = (EditText) view.findViewById(R.id.editTextSource);
                String textToTranslate = mEdit.getText().toString();

                TextView textViewDestination = (TextView) view.findViewById(R.id.textViewDestination);
                String translatedText = textViewDestination.getText().toString();

                if (!textToTranslate.equals("") && !translatedText.equals("")) {
                    DatabaseAdapter databaseAdapter = new DatabaseAdapter(getActivity());
                    databaseAdapter.openToWrite();

                    String direction = SharedPreferencesClass.getTranslationDirection(getActivity());

                    databaseAdapter.insertTranslationHistory(textToTranslate, translatedText, direction, 0);

                    databaseAdapter.DBclose();

                    Toast.makeText(getActivity(), "Сохранено в историю", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Одно из полей пустое", Toast.LENGTH_LONG).show();
                }
            }
        });


        // buttonSaveToFavourite
        Button buttonSaveToFavourite = (Button) view.findViewById(R.id.buttonSaveToFavourite);
        buttonSaveToFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText mEdit = (EditText) view.findViewById(R.id.editTextSource);
                String textToTranslate = mEdit.getText().toString();

                TextView textViewDestination = (TextView) view.findViewById(R.id.textViewDestination);
                String translatedText = textViewDestination.getText().toString();

                if (!textToTranslate.equals("") && !translatedText.equals("")) {
                    DatabaseAdapter databaseAdapter = new DatabaseAdapter(getActivity());
                    databaseAdapter.openToWrite();

                    String direction = SharedPreferencesClass.getTranslationDirection(getActivity());

                    databaseAdapter.insertTranslationHistory(textToTranslate, translatedText, direction, 1);

                    databaseAdapter.DBclose();

                    Toast.makeText(getActivity(), "Сохранено в избранное", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Одно из полей пустое", Toast.LENGTH_LONG).show();
                }
            }
        });

        // buttonMicrophoneInput
        Button buttonMicrophoneInput = (Button) view.findViewById(R.id.buttonMicrophoneInput);
        buttonMicrophoneInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String langSource = SharedPreferencesClass.loadVariable(KEY_FROM_LANGUAGE, getActivity());

                // только Русский, Английский, Турецкий, Украинский
                if (langSource.equals("ru") ||
                        langSource.equals("en") ||
                        langSource.equals("tr") ||
                        langSource.equals("uk")) {
                    // старт распознавания
                    hideInterfaceBottom();
                    startVoiceToTextFragment();

                } else {
                    Toast.makeText(getActivity(), "Только рус, англ, укр, тур", Toast.LENGTH_LONG).show();
                }

            }
        });


        // обработка выбора исходного языка
        Button buttonFromLanguage = (Button) view.findViewById(R.id.buttonFromLanguage);
        buttonFromLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new EventLanguage(null, CONSTS.SOURCE_BUTTON_EVENT_MESSAGE));
                hideInterface();
                startPickupLanguageFragment();
            }
        });

        Button buttonToLanguage = (Button) view.findViewById(R.id.buttonToLanguage);
        buttonToLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new EventLanguage(null, CONSTS.DESTINATION_BUTTON_EVENT_MESSAGE));
                hideInterface();
                startPickupLanguageFragment();
            }
        });
        //textViewLanguage
//        String fromLanguage = SharedPreferencesClass.loadVariable(KEY_FROM_LANGUAGE, getActivity());
//        if (fromLanguage != null) {
//            final TextView textViewToChange = (TextView) view.findViewById(R.id.buttonFromLanguage);
//            textViewToChange.setText(fromLanguage);
//        }


        updateFromToButtonsText(view);

        //////////
        return view;
    }

    void startVoiceToTextFragment() {
        Fragment fragmentVoiceToText = new RecognizerFragment();
        FragmentManager mFragmentManager = getFragmentManager();

        FragmentTransaction voiceInputFragment = mFragmentManager.beginTransaction();
        voiceInputFragment.replace(R.id.frameVoiceToText, fragmentVoiceToText);
        voiceInputFragment.commit();

    }

    void startPickupLanguageFragment() {
        FragmentPickupLanguage fragmentPickupLanguage = new FragmentPickupLanguage();
        android.app.FragmentManager mFragmentManager = getFragmentManager();

        FragmentTransaction langFragmentTransaction = mFragmentManager.beginTransaction();
        langFragmentTransaction.replace(R.id.frameLanguages, fragmentPickupLanguage);
        langFragmentTransaction.commit();
    }

    void showInterface() {
        try {
            RelativeLayout frameLayout4 = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout4);
            frameLayout4.setVisibility(View.VISIBLE);

            RelativeLayout frameLayout = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
            frameLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }

    }

    void hideInterface() {
        try {
            RelativeLayout frameLayout4 = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout4);
            frameLayout4.setVisibility(View.INVISIBLE);

            RelativeLayout frameLayout = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
            frameLayout.setVisibility(View.INVISIBLE);

            Fragment fragment = getFragmentManager().findFragmentById(R.id.frameVoiceToText);
            if (fragment != null)
                getFragmentManager().beginTransaction().remove(fragment).commit();
        } catch (Exception e) {

        }
    }

    void hideInterfaceBottom() {
        try {
            RelativeLayout frameLayout = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
            frameLayout.setVisibility(View.INVISIBLE);
        } catch (Exception e) {

        }
    }

    void showInterfaceBottom() {
        try {
            RelativeLayout frameLayout = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
            frameLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {

        }
    }

    private void resetVocalizer() {
        if (vocalizer != null) {
            vocalizer.cancel();
            vocalizer = null;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
        }
    }


    @Override
    public void onStop() {
        Log.e("DEBUG", "OnStop of Fragment");
        try {
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
        }

        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        resetVocalizer();
    }

    @Override
    public void onSynthesisBegin(Vocalizer vocalizer) {

    }

    @Override
    public void onSynthesisDone(Vocalizer vocalizer, Synthesis synthesis) {

    }

    @Override
    public void onPlayingBegin(Vocalizer vocalizer) {

    }

    @Override
    public void onPlayingDone(Vocalizer vocalizer) {

    }

    @Override
    public void onVocalizerError(Vocalizer vocalizer, Error error) {
        resetVocalizer();
    }
}




