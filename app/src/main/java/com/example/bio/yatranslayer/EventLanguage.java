package com.example.bio.yatranslayer;

// для eventBus кидаю этот класс между фрагментами для обмена
class EventLanguage {
    final String LanguageShortName;
    final String message;

    EventLanguage(String LanguageShortName, String message) {
        this.LanguageShortName = LanguageShortName;
        this.message = message;
    }
}