package com.example.bio.yatranslayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class HistoryAdapter extends BaseAdapter {
    private static final int CONST_TRUE = 1;

    Context context;
    LayoutInflater lInflater;
    ArrayList<Translate> translateArrayList;

    HistoryAdapter(Context context, ArrayList<Translate> translate) {
        this.context = context;
        translateArrayList = translate;
        lInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return translateArrayList.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return translateArrayList.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    // пункт списка
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.history_view, parent, false);
        }

        Translate p = getHistory(position);

        // заполняем View в пункте списка данными
        ((TextView) view.findViewById(R.id.textViewSourceText)).setText(p.fromLang);
        ((TextView) view.findViewById(R.id.textViewDestinationText)).setText(p.toLang);

        String[] fromToLang = p.direction.split("-");
        ((TextView) view.findViewById(R.id.textViewFromLanguage)).setText(fromToLang[0]);
        ((TextView) view.findViewById(R.id.textViewDestinationLanguage)).setText(fromToLang[1]);

        // если у объекта избранное, рисую подсвеченную звездочку
        if (p.favourite == CONST_TRUE) {
            ((ImageView) view.findViewById(R.id.imageViewFav)).setImageResource(android.R.drawable.btn_star_big_on);
        } else {
//            ((ImageView) view.findViewById(R.id.imageViewFav)).setImageResource(0);
        }

        return view;
    }

    Translate getHistory(int position) {
        return ((Translate) getItem(position));
    }
}