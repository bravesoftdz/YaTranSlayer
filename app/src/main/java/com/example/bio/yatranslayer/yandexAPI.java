package com.example.bio.yatranslayer;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class yaAPI {
    private static final String YANDEX_API_VERSION = "v1.5";
    private static final String YANDEX_API_KEY = "trnsl.1.1.20170315T201427Z.3d922f705d0b7acf.182766b256a136dfb71cee03a4afa34f585c9b2b";
    private static final String BASE_URL = "https://translate.yandex.net/api/" + YANDEX_API_VERSION + "/tr.json/";
    protected final static int WAIT_FOR_RESPONSE_X_SEC = 3;

    public String getTranslatedText(String stringIn, String languageDirection) {

        return null;
    }


    public String getRecognizeLanguage(String StringIn) {

        return null;
    }


    String getHTTP(String url) {
        String response = null;
        getHTTPrequestTask myTask = new getHTTPrequestTask();
        myTask.execute(BASE_URL, YANDEX_API_KEY);

        try {
            response = myTask.get(WAIT_FOR_RESPONSE_X_SEC, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            response = "no inet";
        }
        return response;
    }


    static Map<String, String> toMap(JSONObject object) throws JSONException {
        Map<String, String> map = new LinkedHashMap<>();

        Iterator<String> keysItr;
        keysItr = object.keys();

        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

//            if (value instanceof JSONArray) {
//                value = toList((JSONArray) value);
//            } else if (value instanceof JSONObject) {
//                value = toMap((JSONObject) value);
//            }

            map.put(key, value.toString());
        }
        return map;
    }

    public static List<String> toList(JSONArray array) throws JSONException {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);

//            if (value instanceof JSONArray) {
//                value = toList((JSONArray) value);
//            } else if (value instanceof JSONObject) {
//                value = toMap((JSONObject) value);
//            }
            list.add((String) value);
        }
        return list;
    }


}





