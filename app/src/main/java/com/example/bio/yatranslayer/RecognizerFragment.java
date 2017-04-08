package com.example.bio.yatranslayer;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import ru.yandex.speechkit.Error;
import ru.yandex.speechkit.Recognition;
import ru.yandex.speechkit.Recognizer;
import ru.yandex.speechkit.RecognizerListener;
import ru.yandex.speechkit.SpeechKit;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.bio.yatranslayer.CONSTS.KEY_FROM_LANGUAGE;
import static com.example.bio.yatranslayer.CONSTS.YANDEX_SPEECH_KIT_API_KEY;

/**
 * This file is a part of the samples for Yandex SpeechKit Mobile SDK.
 * <br/>
 * Version for Android © 2016 Yandex LLC.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <br/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <br/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RecognizerFragment extends Fragment implements RecognizerListener {
    private static final int REQUEST_PERMISSION_CODE = 1;

    private ProgressBar progressBar;
    private TextView currentStatus;
    private TextView partialStatus;
    private TextView recognitionResult;

    private Recognizer recognizer;

    public RecognizerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpeechKit.getInstance().configure(getActivity(), YANDEX_SPEECH_KIT_API_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        return inflater.inflate(R.layout.fragment_recognizer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button startBtn = (Button) view.findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndStartRecognizer();
            }
        });

        Button cancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRecognizer();
            }
        });

        Button buttonClose = (Button) view.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRecognizer();

                try {
                    RelativeLayout frameLayout = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
                    frameLayout.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                }
                // самоуничтожаюсь
                getFragmentManager().beginTransaction().remove(RecognizerFragment.this).commitAllowingStateLoss();
            }
        });

        Button buttonSendTextTo = (Button) view.findViewById(R.id.buttonSendTextTo);
        buttonSendTextTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    RelativeLayout frameLayout = (RelativeLayout) getActivity().findViewById(R.id.relativeLayout);
                    frameLayout.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                }

                String curText = recognitionResult.getText().toString();

                Toast.makeText(getActivity(), curText, Toast.LENGTH_LONG).show();

                // лезу изменяю текст в поле ввода текста
                // плохая идея, лучше автобус послать
                EventBus.getDefault().post(new EventLanguage(null, curText));

                // самоуничтожаюсь
                getFragmentManager().beginTransaction().remove(RecognizerFragment.this).commitAllowingStateLoss();

            }
        });


        progressBar = (ProgressBar) view.findViewById(R.id.voice_power_bar);
        currentStatus = (TextView) view.findViewById(R.id.current_state);
        partialStatus = (TextView) view.findViewById(R.id.partial_result);
        recognitionResult = (TextView) view.findViewById(R.id.result);
        //buttonSendTextTo

    }

    @Override
    public void onPause() {
        super.onPause();
        resetRecognizer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PERMISSION_GRANTED) {
            createAndStartRecognizer();
        } else {
            updateStatus(getString(R.string.record_permission_text));
        }
    }

    private void resetRecognizer() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer = null;
        }
    }

    @Override
    public void onRecordingBegin(Recognizer recognizer) {
        updateStatus(getString(R.string.recording_begin));
    }

    @Override
    public void onSpeechDetected(Recognizer recognizer) {
        updateStatus(getString(R.string.speech_detected));
    }

    @Override
    public void onSpeechEnds(Recognizer recognizer) {
        updateStatus(getString(R.string.speech_ends));
    }

    @Override
    public void onRecordingDone(Recognizer recognizer) {
        updateStatus(getString(R.string.recording_done));
    }

    @Override
    public void onSoundDataRecorded(Recognizer recognizer, byte[] bytes) {
    }

    @Override
    public void onPowerUpdated(Recognizer recognizer, float power) {
        updateProgress((int) (power * progressBar.getMax()));
    }

    @Override
    public void onPartialResults(Recognizer recognizer, Recognition recognition, boolean b) {
        updateStatusPartialResult(getString(R.string.partial_result) + recognition.getBestResultText());
    }

    @Override
    public void onRecognitionDone(Recognizer recognizer, Recognition recognition) {
        updateResult(recognition.getBestResultText());
        updateProgress(0);
    }

    @Override
    public void onError(Recognizer recognizer, ru.yandex.speechkit.Error error) {
        if (error.getCode() == Error.ERROR_CANCELED) {
            updateStatus(getString(R.string.canceled));
            updateProgress(0);
        } else {
            updateStatus(getString(R.string.error_occured) + error.getString());
            resetRecognizer();
        }
    }

    private void createAndStartRecognizer() {
        final Context context = getActivity();
        if (context == null) {
            return;
        }

        // проверка на доступ через API 23 requestPermissions
        if (ContextCompat.checkSelfPermission(context, RECORD_AUDIO) != PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
            }
        } else {
            // Reset the current recognizer.
            resetRecognizer();
            // To create a new recognizer, specify the language, the model - a scope of recognition to get the most appropriate results,
            // set the listener to handle the recognition events.
            String curLang = SharedPreferencesClass.loadVariable(KEY_FROM_LANGUAGE, getActivity());

            String langInput="";
            switch(curLang) {
                case "ru":
                    langInput = Recognizer.Language.RUSSIAN;
                    break;
                case "en":
                    langInput = Recognizer.Language.ENGLISH;
                    break;
                case "tr":
                    langInput = Recognizer.Language.TURKISH;
                    break;
                case "uk":
                    langInput = Recognizer.Language.UKRAINIAN;
                    break;
                default:
                    langInput = Recognizer.Language.RUSSIAN;
                    break;
            }

            recognizer = Recognizer.create(langInput, Recognizer.Model.NOTES, RecognizerFragment.this);
            // Don't forget to call start on the created object.
            recognizer.start();
        }
    }

    private void updateResult(String text) {
        recognitionResult.setText(text);
    }

    private void updateStatus(final String text) {
        currentStatus.setText(text);
    }

    private void updateStatusPartialResult(final String text) {
        partialStatus.setText(text);
    }

    private void updateProgress(int progress) {
        progressBar.setProgress(progress);
    }
}
