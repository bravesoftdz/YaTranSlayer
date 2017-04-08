package com.example.bio.yatranslayer;

// класс контейнер
class Translate {
    long _id;
    String fromLang;
    String toLang;
    String direction;
    int favourite;

    Translate(int _id, String fromLang, String toLang, String direction, int favourite) {
        this._id = _id;
        this.fromLang = fromLang;
        this.toLang = toLang;
        this.direction = direction;
        this.favourite = favourite;
    }
}
