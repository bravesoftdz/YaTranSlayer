package com.example.bio.yatranslayer;


import android.content.Context;
import android.os.AsyncTask;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;

import static com.example.bio.yatranslayer.CONSTS.KEY_CURRENT_LANGUAGE;
import static com.example.bio.yatranslayer.CONSTS.KEY_FROM_LANGUAGE;
import static com.example.bio.yatranslayer.CONSTS.KEY_FROM_LANGUAGE_FULL_NAME;
import static com.example.bio.yatranslayer.CONSTS.KEY_TO_LANGUAGE;
import static com.example.bio.yatranslayer.CONSTS.KEY_TO_LANGUAGE_FULL_NAME;

class yandexAPI {
    private static final String YANDEX_API_VERSION = "v1.5"; // ну, очевидно, что это версия и она будет вот тут меняться и приложение упадет

    // нумерация функций по номеру, подумать над передачей параметров пооптимальней чем по номерам, пока лишь бы работало для 3х методов

    static final String YANDEX_API_GET_LANGUAGES = "getLangs?";// API получить список поддерживаемых языков и направлений перевода

    static final String YANDEX_API_DETECT_LANGUAGE = "detect?"; // API определение языка

    static final String YANDEX_API_TRANSLATE = "translate?"; // функция API перевода


    // ключ доступа к яндекс переводчику, уникален для каждого
    private static final String YANDEX_API_KEY = "trnsl.1.1.20170315T201427Z.3d922f705d0b7acf.182766b256a136dfb71cee03a4afa34f585c9b2b";

    // базовая ссылка
    private static final String BASE_URL = "https://translate.yandex.net/api/" + YANDEX_API_VERSION + "/tr.json/";

    // ждем ответа от asynсTask X секунд, потом в игнор, 5 секунд сделал константу, в опции можно вынести
    private final static int WAIT_FOR_RESPONSE_X_SEC = 5;

    // errorCode и errorMessage вынесу на уровень класса
    int errorCode;
    String errorMessage;
    String curLocale;

    Context context;

    // в конструкторе обнуляю в начале код и текст ошибки
    yandexAPI() {
        this.errorCode = -1;
        this.errorMessage = null;
    }

    // взять errorCode
    private int getErrorCode() {
        return this.errorCode;
    }

    String getErrorMessage() {
        return this.errorMessage;
    }

    // http://loopj.com/android-async-http/
    // прототип, работает, но не внедрен в код, асинхронный запрос приходит с задержкой из-за http, подумать куда дальше переслать
    // может быть тут тоже eventBus подойдет
    private String getAsyncHttp(String yandexApiName, String fromLanguageText, String translateTextDirection) {
        String yandexApiNameq = null;
        RequestParams params = new RequestParams();
        params.put("key", YANDEX_API_KEY);

        switch (yandexApiName) {
            case YANDEX_API_GET_LANGUAGES:
                yandexApiNameq = "getLangs?";
                params.put("ui", curLocale);
                break;

            case YANDEX_API_DETECT_LANGUAGE:
                yandexApiNameq = "detect?";
                params.put("text", fromLanguageText);
                break;

            case YANDEX_API_TRANSLATE:
                yandexApiNameq = "translate?";
                params.put("text", fromLanguageText);
                params.put("lang", translateTextDirection);
                break;

            default:
                //оператор;
                break;
        }

        AsyncHttp.get(yandexApiNameq, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String a = "a";
            }

        });


        return "";
    }


    // метод получить данные через HTTP, ответ будет JSON в виде String
    // метод обрабатывает 3 запроса, на полечение данных, на определение языка и перевод текста,
    // это чтоли контроллер, на входе обращение к яндекс апи, на выходе текст ответа
    // 1 - response = getHTTPSresponse(yandexAPI.YANDEX_API_GET_LANGUAGES, null, null);
    // на выходе получаем список поддерживаемых имен и направлений перевода
    // 2 - String response = getHTTPSresponse(yandexAPI.YANDEX_API_DETECT_LANGUAGE, stringEncode(recognizeSourceText), null);
    // на входе текст, на выходе определение языка текста
    // 3 - String response = getHTTPSresponse(yandexAPI.YANDEX_API_TRANSLATE, stringEncode(sourceText), stringEncode(TranslationDirection));
    // перевод текста с на языки
    String getHTTPSresponse(String yandexApiName, String inputText, String translateDirection) {
        String response = null; // строка ответа


        getHTTPrequestTask myTask = new getHTTPrequestTask(); // создаю задание

        // запускаю задание, передаю базовый урл, апи ключ, имя функции
        myTask.execute(BASE_URL, YANDEX_API_KEY, yandexApiName, inputText, translateDirection);

        try {
            // жду ответа 5 секунд, мухожук, возвращаю текст хттп ответа
            response = myTask.get(WAIT_FOR_RESPONSE_X_SEC, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            response = "no inet";
        }
        return response; // возвращаю строку ответа сервера
    }


    // класс AsyncTask для HTTP запроса
    private class getHTTPrequestTask extends AsyncTask<String, Void, String> {
        StringBuffer response = null;

        protected String doInBackground(String... strings) {
            // инициализируем статус http запроса чем-нибудь
            int statusHTTP = -1;

            // код состояния http 200 = ок
            final int HTTP_STATUS_OK = 200;

            // первым параметром отдаю базовый урл
            final String BASE_URL = strings[0];

            // вторым yandex API ключ
            final String yandexApiKey = strings[1];

            // третьим параметром отдаю имя функции для вызова
            final String yandexApiName = strings[2];

            // четвертый параметр входной текст от юзера
            final String fromLanguageText = strings[3];

            // направление перевода
            final String translateTextDirection = strings[4];
            try {
                response = new StringBuffer("");
                // строка запроса по https

                String yandexApiNameq = YANDEX_API_GET_LANGUAGES;

                Object urlString = null;
                // выбираем функцию
                switch (yandexApiName) {
                    case YANDEX_API_GET_LANGUAGES:
                        yandexApiNameq = "getLangs?";
                        urlString = BASE_URL + yandexApiNameq +
                                "key=" + yandexApiKey +
                                "&ui=" + curLocale;
                        break;
                    case YANDEX_API_DETECT_LANGUAGE:
                        yandexApiNameq = "detect?";
                        urlString = BASE_URL + yandexApiNameq +
                                "key=" + yandexApiKey +
                                "&text=" + fromLanguageText;

                        break;
                    case YANDEX_API_TRANSLATE:
                        yandexApiNameq = "translate?";
                        urlString = BASE_URL + yandexApiNameq +
                                "key=" + yandexApiKey +
                                "&text=" + fromLanguageText +
                                "&lang=" + translateTextDirection;
                        break;
                    default:
                        //оператор;
                        break;
                }

                // язык вывода сообщений о названиях языков в ключе ui - вынести в настройки
//                Object urlString =
//                        BASE_URL + yandexApiNameq +
//                                "key=" + yandexApiKey +
//                                "&ui=ru";

                // формирую тип URL
                URL url = new URL((String) urlString);

                // создаю HttpURLConnection, метод POST,
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // включаю на всякий случай для HTTP POST
                connection.setDoInput(true);

                // HTTP POST
                connection.setRequestMethod("POST");

                connection.connect();
                // {"code":401,"message":"API key is invalid"}
                statusHTTP = connection.getResponseCode();
                InputStream inputStream = connection.getInputStream();


                // если ответ пришел нормальный код состояния HTTP 200 OK, то читаем ответ
                if (statusHTTP == HTTP_STATUS_OK) {
                    String line = "";
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                response.append("HTTP ERROR " + statusHTTP);
            }

            return response.toString();
        }
    }

    Map<String, String> getSupportedLanguages(Context context) {
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
        databaseAdapter.openToRead();

        Map<String, String> languagesMap = null;

        languagesMap = databaseAdapter.querrySupportedLangMap();
        if (languagesMap.size() == 0) {
            // база пустая, сэр
            // пробуем еще раз хттп запрос кинуть
            yandexAPI yandexApi = new yandexAPI();
            yandexApi.init(context);
            // и еще одна попытка прочитать из базы, вдруг хттп ответило
            // ужасно, переделать, псевдо кеширование какое-то, погуглить
            languagesMap = databaseAdapter.querrySupportedLangMap();
        }
//      если бд-хттп-бд всё еще пустая мапа, то инета нет, продумать, переделать.

        databaseAdapter.DBclose();
        // может вернуться null !!!
        return languagesMap;
    }

    boolean isDirectionSupported (String direction, Context context){
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
        databaseAdapter.openToRead();
        List<String> supportedTranslationDirectionsList = new ArrayList<>();
        supportedTranslationDirectionsList = databaseAdapter.getSupportedDirections();
        databaseAdapter.DBclose();

        if (supportedTranslationDirectionsList.contains(direction)) {
            return true;
        } else {
            return false;
        }

    }

    void init(Context context) {
        curLocale = SharedPreferencesClass.loadVariable(KEY_CURRENT_LANGUAGE, context);
        Map<String, String> languagesMap = null;

        // если ничего не сохранено в шаред насчет языка по умолчанию, то беру локаль
        // перед http запросом проверим что есть в базе
        Map<String, String> map = new HashMap<>();

        DatabaseAdapter databaseAdapter = new DatabaseAdapter(context);
        databaseAdapter.openToWrite();

        try {
            map = databaseAdapter.querrySupportedLangMap();
        } catch (Exception e){
        }

        if (map.size() == 0) {
            String error = "Алярма, списка языков в базе нет, о нет, что же делать, придется писать https письмо";
            // шлем http запрос получить список языков и направлений

            // нет проверки на наличие этих ваших интернетов
            String response = "no inet";
            response = getHTTPSresponse(yandexAPI.YANDEX_API_GET_LANGUAGES, null, null);
            // дебаг строчка
            // response = "{\"code\":401,\"message\":\"API key is invalid\"}";
            JSONhelper jsonHelper = new JSONhelper(response);

            List<String> supportedTranslationDirectionsList = null;

            try {
                supportedTranslationDirectionsList = jsonHelper.getSupportedTranslationDirectionsList();
                this.errorMessage = jsonHelper.getJSONErrorMessage();
                this.errorCode = jsonHelper.getJSONErrorCode();
                languagesMap = jsonHelper.getLanguagesMap();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // нет ошибок
            if (isErrorCodeOK(getErrorCode())) {
                // сохранил Map языков ru-русский в бд
                databaseAdapter.insertSupportedLanguages(languagesMap);


                // загрузил назад проверить
                // Map<String, String> map = new HashMap<>();
//                map = databaseHelper.querrySupportedLangMap();

                // сохраняю список доступных направлений перевода в таблицу 2
                databaseAdapter.insertSupportedTranslationDirections(supportedTranslationDirectionsList);

                // проверка назад из базы
                //   List<String> listString = new ArrayList<>();
                //   listString = databaseHelper.getSupportedDirections();
            }
        }
        databaseAdapter.DBclose();

        // остался тут LanguagesMap
        String fromLang = SharedPreferencesClass.loadVariable(KEY_FROM_LANGUAGE, context);
        String toLang = SharedPreferencesClass.loadVariable(KEY_TO_LANGUAGE, context);

        String fullNameFrom = map.get(fromLang);
        String fullNameTo = map.get(toLang);

        SharedPreferencesClass.saveVariable(KEY_FROM_LANGUAGE_FULL_NAME, fullNameFrom, context);
        SharedPreferencesClass.saveVariable(KEY_TO_LANGUAGE_FULL_NAME, fullNameTo, context);


//      запрос через библиотеку async http, надо из колбека послать eventbus попробовать
        String answer = getAsyncHttp(YANDEX_API_GET_LANGUAGES, null, null);


        // проверка гипотезис по хттп без кеша
        String testHypothesis = "Привет, как дела ?";
        String hypothesisLanguage = getHypothesisLanguage(testHypothesis);


        String sourceText = "Дикий Билл был крокодил, и потому он много пил";
        String translateDirection = "ru-en";
        List<String> translatedText = getTranslatedText(sourceText, translateDirection);
        // нет ошибок
        if (isErrorCodeOK(getErrorCode())) {
            databaseAdapter = new DatabaseAdapter(context);
            // сохранить строку текста с переводом под новым значением
            databaseAdapter.openToWrite();
//            databaseAdapter.insertTranslationHistory(sourceText, translatedText, translateDirection, 1);
//            databaseAdapter.insertTranslationHistory(sourceText, translatedText, translateDirection, 0);
//            databaseAdapter.insertTranslationHistory(sourceText, translatedText, translateDirection, 1);
//            databaseAdapter.insertTranslationHistory(sourceText, translatedText, translateDirection, 0);
            databaseAdapter.DBclose();
        }


        String a = null;
        a = "a";
    }

    List<String> getTranslatedText(String sourceText, String TranslationDirection) {
        List<String> translatedText = null;
        String translationDirection = null;

        String response = getHTTPSresponse(yandexAPI.YANDEX_API_TRANSLATE, stringEncode(sourceText), stringEncode(TranslationDirection));

        JSONhelper jsonHelper = new JSONhelper(response);
        this.errorMessage = jsonHelper.getJSONErrorMessage();
        this.errorCode = jsonHelper.getJSONErrorCode();

        if (isErrorCodeOK(getErrorCode())) {
            translationDirection = jsonHelper.getLanguageHypothesis();
            translatedText = jsonHelper.getTranslatedText();
        }
        return translatedText;
    }

    // посылаю по https запрос на определение языка
    String getHypothesisLanguage(String recognizeSourceText) {
        if (recognizeSourceText.equals("") || recognizeSourceText==null) return "";

        String response = getHTTPSresponse(yandexAPI.YANDEX_API_DETECT_LANGUAGE, stringEncode(recognizeSourceText), null);
        JSONhelper jsonHelper = new JSONhelper(response);

        this.errorMessage = jsonHelper.getJSONErrorMessage();
        this.errorCode = jsonHelper.getJSONErrorCode();
        // нет ошибок
        String hypothesisLanguage = null;
        if (isErrorCodeOK(getErrorCode())) {
            hypothesisLanguage = jsonHelper.getLanguageHypothesis();
        }
        return hypothesisLanguage;
    }

    // -1 и 200 = status ok
    private boolean isErrorCodeOK(int errorCode) {
        if (errorCode == -1 || errorCode == 200)
            return true;
        else
            return false;
    }

    private String stringEncode(String str) {
        String encodedStr = null;
        try {
            encodedStr = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedStr;
    }

    private String stringDecode(String str) {
        String decodedStr = null;
        try {
            decodedStr = URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decodedStr;
    }

}





