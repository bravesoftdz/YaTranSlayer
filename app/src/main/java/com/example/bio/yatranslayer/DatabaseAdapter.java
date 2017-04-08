package com.example.bio.yatranslayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.loopj.android.http.AsyncHttpClient.LOG_TAG;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "yatraslayer.db"; // название бд
    private static final int DB_VERSION = 2; // версия бд
    private static final String COLUMN_ID = "_id"; // у всех чтоли таблиц первой идет _id в sqlite

    // таблица поддерживаемых направлений перевода [ru-en, en-ru...]
    private static final String TABLE_SUPPORTED_DIRECTIONS = "supported_directions";
    private static final String COLUMN_SUPPORTED_DIRECTIONS = "supported_directions";

    // таблица поддерживаемых языков [_id, ru, Русский]
    private static final String TABLE_SUPPORTED_LANGUAGES = "supported_languages";
    private static final String COLUMN_SHORT_LANGUAGE_NAME = "short_language_name";
    private static final String COLUMN_FULL_LANGUAGE_NAME = "full_language_name";

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase sqLiteDatabase;

    // Таблица
    //      fromLanguageText, toLanguageText, directionText, favourite 1/0 да/нет
    // _id, Привет,           Hello,          ru-en,         0
    private static final String TABLE_HISTORY_DIRECTION_FAVOURITES = "history_direction_favourites"; // текст from lang, текст to lang, direction (ru-en) если разрешено как выше, и farourite 1/0
    private static final String COLUMN_FROM_LANGUAGE_TEXT = "from_language_text"; // текст оригинала запроса
    private static final String COLUMN_TO_LANGUAGE_TEXT = "to_language_text"; // текст перевода
    private static final String COLUMN_DIRECTION = "translate_direction";
    private static final String COLUMN_FAVOURITE = "favourite"; // 1/0 да/нет в избранном

    // каждый объект при создании получает контекст
    private Context context;
    private SQLiteHelper sqLiteHelper;

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        this.context = context;
    }

    DatabaseHelper openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, DB_NAME, null, DB_VERSION);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    DatabaseHelper openToWrite() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, DB_NAME, null, DB_VERSION);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    void DBclose() {
        databaseHelper.close();
    }

    // CREATE TABLE if not exists
    @Override
    public void onCreate(SQLiteDatabase db) {
        // создал первую таблицу, ru - русский
        String createTable1 = "";
        db.execSQL("CREATE TABLE " + TABLE_SUPPORTED_LANGUAGES + " (" +
                COLUMN_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, " +
                COLUMN_SHORT_LANGUAGE_NAME + " TEXT NOT NULL, " +
                COLUMN_FULL_LANGUAGE_NAME + " TEXT NOT NULL);");

        // создал вторую таблицу со списком направлений перевода ru-en, en-ru
        db.execSQL("CREATE TABLE " + TABLE_SUPPORTED_DIRECTIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SUPPORTED_DIRECTIONS + " TEXT NOT NULL);");

        // таблица с текстом Истории, Направления перевода и Избранного чучузен ван
        String sqlCreateTableExec =
                "CREATE TABLE " + TABLE_HISTORY_DIRECTION_FAVOURITES + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_FROM_LANGUAGE_TEXT + " TEXT NOT NULL, " +
                        COLUMN_TO_LANGUAGE_TEXT + " TEXT NOT NULL, " +
                        COLUMN_DIRECTION + " TEXT NOT NULL, " +
                        COLUMN_FAVOURITE + " INTEGER);"; // пока так оставлю, вдруг опять базу править буду
        db.execSQL(sqlCreateTableExec);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPORTED_LANGUAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPORTED_DIRECTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY_DIRECTION_FAVOURITES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPORTED_LANGUAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUPPORTED_DIRECTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY_DIRECTION_FAVOURITES);
        onCreate(db);
    }


    // сохраняю первую таблицу поддерживаемых языков перевода
    // ru - русский    // en - английский
    void saveMapSupportedLanguagesToDB(Map<String, String> languagesMap) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // итератор по hashMap и раскладывание в таблицу TABLE_SUPPORTED_LANGUAGES
        Iterator it = languagesMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            values.put(COLUMN_SHORT_LANGUAGE_NAME, (String) pair.getKey());
            values.put(COLUMN_FULL_LANGUAGE_NAME, (String) pair.getValue());

            db.insert(TABLE_SUPPORTED_LANGUAGES, null, values);
            it.remove();
        }
        db.close();
    }

    // Загрузить из БД таблицу ru-Русский и разложить в HashMap
    Map<String, String> getMapSupportedLanguagesFromDB() {
        Map<String, String> langsMap = new HashMap<>();
        Cursor c = null;

        SQLiteDatabase db = this.getReadableDatabase();
        //Cursor cursor = db.rawQuery(selectQuery, null);

        c = db.query(TABLE_SUPPORTED_LANGUAGES, null, null, null, null, null, null);

        if (c != null) {
            if (c.moveToFirst()) {
                String str;

                int idColIndex = c.getColumnIndex("_id"); // 0
                int shortLanguageName = c.getColumnIndex(COLUMN_SHORT_LANGUAGE_NAME); // 1
                int fullLanguageName = c.getColumnIndex(COLUMN_FULL_LANGUAGE_NAME); // 2

                do {
                    int id = c.getInt(idColIndex);
                    String shortLanguageNameS = c.getString(shortLanguageName);
                    String fullLanguageNameS = c.getString(fullLanguageName);

                    langsMap.put(shortLanguageNameS, fullLanguageNameS);
                } while (c.moveToNext());
            }
            c.close();
        } else
            Log.d(LOG_TAG, "Cursor is null");

        db.close();
        return langsMap;
    }

    ArrayList<Translate> getTranslationHistoryFromDB() {
        ArrayList<Translate> translate = new ArrayList<>();
        Cursor c = null;
        SQLiteDatabase db = this.getReadableDatabase();

        c = db.query(TABLE_HISTORY_DIRECTION_FAVOURITES, null, null, null, null, null, null);

        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    int _id = c.getInt(c.getColumnIndex(COLUMN_ID));
                    String fromLangText = c.getString(c.getColumnIndex(COLUMN_FROM_LANGUAGE_TEXT));
                    String toLangText = c.getString(c.getColumnIndex(COLUMN_TO_LANGUAGE_TEXT));
                    String dir = c.getString(c.getColumnIndex(COLUMN_DIRECTION));
                    int fav = c.getInt(c.getColumnIndex(COLUMN_FAVOURITE));

                    translate.add(new Translate(_id, fromLangText, toLangText, dir, fav));
                } while (c.moveToNext());
            }
            c.close();
        }

        db.close();

        return translate;
    }

    // возвращает количество удаленных строк, =1 если строка удалена, =0 если нет
    int deleteHistoryRowByID(int id) {
//        Cursor c = null;
//        SQLiteDatabase db = getWritableDatabase();

        String selection = COLUMN_ID + " = " + id;

        int delCount = sqLiteDatabase.delete(TABLE_HISTORY_DIRECTION_FAVOURITES, selection, null);


        Log.d(LOG_TAG, "deleted rows count = " + delCount);
//        db.close();

        return delCount;
    }

    void saveTranslationHistoryToDB(String sourceText, List<String> translatedText, String translateDirection, int favourite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FROM_LANGUAGE_TEXT, sourceText);

        // при конверсии списка в строку по краям получаются квадратные скобки [Hello], их удаляю
        String txt = String.valueOf(translatedText);
        String result = txt.replaceAll("\\[|\\]", "");

        values.put(COLUMN_TO_LANGUAGE_TEXT, result);
        values.put(COLUMN_DIRECTION, translateDirection);
        values.put(COLUMN_FAVOURITE, favourite);

        long newRowId = db.insert(TABLE_HISTORY_DIRECTION_FAVOURITES, null, values);
        db.close();
    }

    // метод сохраняет список поддерживаемых направлений перевода в бд
    void saveSupportedTranslationDirectionsListToDB(List<String> listLanguageDirections) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        Iterator<String> it = listLanguageDirections.iterator();
        while (it.hasNext()) {
            String str = it.next();
            values.put(COLUMN_SUPPORTED_DIRECTIONS, str);
            db.insert(TABLE_SUPPORTED_DIRECTIONS, null, values);
            it.remove();
        }
        db.close();
    }

    // загружаем из бд список поддерживаемых направлений перевода
    List<String> getSupportedDirectionsListFromDB() {
        List<String> supportedLanguagesList = new ArrayList<>();
        Cursor c = null;

        SQLiteDatabase db = this.getReadableDatabase();
        c = db.query(TABLE_SUPPORTED_DIRECTIONS, null, null, null, null, null, null);

        if (c != null) {
            if (c.moveToFirst()) {
                int idColIndex = c.getColumnIndex("_id"); // 0
                int supportedDirList = c.getColumnIndex(COLUMN_SUPPORTED_DIRECTIONS); // 1
                do {
                    int id = c.getInt(idColIndex);
                    String supportedDirection = c.getString(supportedDirList);

                    supportedLanguagesList.add(supportedDirection);
                } while (c.moveToNext());
            }
            c.close();
        } else
            Log.d(LOG_TAG, "Cursor is null");

        db.close();
        return supportedLanguagesList;

    }

    class SQLiteHelper extends SQLiteOpenHelper {
        SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        }


    }
}