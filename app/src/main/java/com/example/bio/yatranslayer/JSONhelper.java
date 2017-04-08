package com.example.bio.yatranslayer;

// Разбираем тут JSON ответы верные и неверные от Яндекс переводчика

// проверяем на ошибку

// 1 - Получение списка поддерживаемых языков
// {"code":401,"message":"API key is invalid"}

// Код ответа возвращается только при неудачном выполнении запроса.
// далее функции возвращают только код без описания (message)

// 2 - Определение языка
// { "code": 200, "lang": "en" }

// 3 - Перевод текста
// { "code": 200, "lang": "en-ru", "text": ["Здравствуй, Мир!"] }

// 200 Операция выполнена успешно
// 401 Неправильный API-ключ
// 402 API-ключ заблокирован
// 404 Превышено суточное ограничение на объем переведенного текста
// 413 Превышен максимально допустимый размер текста
// 422 Текст не может быть переведен
// 501 Заданное направление перевода не поддерживается

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class JSONhelper {

    private String stringYaHTTPResponse = "";
    private JSONObject jsonObject;
    private int errorCode = -1;
    private String errorText = "";
    private String hypothesisText = "";


    JSONhelper(String stringYaHTTPResponse) {
        try {
            jsonObject = new JSONObject(stringYaHTTPResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    JSONObject getJsonObject() {
        return jsonObject;
    }

    String getLanguageHypothesis() {
        try {
            String lang = "lang";
            hypothesisText = (String) getJsonObject().get(lang);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hypothesisText;
    }

    // Возвращает код если в json ответе есть "code": 200
    int getJSONErrorCode() {
        try {
            String code = "code";
            errorCode = (int) getJsonObject().get(code);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return errorCode;
    }

    List<String> getTranslatedText() {
        List<String> translatedText = new ArrayList<>();
        try {
            String text = "text";
            translatedText = yaJsonToList((JSONArray) getJsonObject().get(text));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return translatedText;
    }


    // возвращает строку, если в json есть "message":"API key is invalid"
    String getJSONErrorMessage() {
        try {
            String message = "message";
            errorText = (String) getJsonObject().get(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return errorText;
    }

    // возвращает мапу языков ru - русский
    Map<String, String> getLanguagesMap() throws JSONException {
        Map<String, String> languagesMap = null;
        try {
            // беру мап пары k-v, ru-русский из субдиректории/архива langs
            String langs = "langs";
            languagesMap = yaJSONtoMap((JSONObject) getJsonObject().get(langs));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return languagesMap;
    }

    List<String> getSupportedTranslationDirectionsList() {
        List<String> supportedTranslationDirections = new ArrayList<>();
        try {
            String dirs = "dirs";
            supportedTranslationDirections = yaJsonToList((JSONArray) getJsonObject().get(dirs));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return supportedTranslationDirections;
    }

    // метод yaJSONtoMap конвертирует JSON Object в HashMap со строками en - английский
    private Map<String, String> yaJSONtoMap(JSONObject o) throws JSONException {
        Map<String, String> map = new HashMap<>();

        // итератор по строкам
        Iterator<String> keysItr;
        keysItr = o.keys();

        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = o.get(key);
            map.put(key, value.toString());
        }
        return map;
    }

    // метом yaJsonToList конвертирует JSON Array в список строк поддерживаемых языков на языке каком попросят в ui=ru, например
    List<String> yaJsonToList(JSONArray array) throws JSONException {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            list.add((String) value);
        }
        return list;
    }

}
