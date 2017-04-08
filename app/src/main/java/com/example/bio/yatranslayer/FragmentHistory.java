package com.example.bio.yatranslayer;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static com.example.bio.yatranslayer.CONSTS.KEY_FAVOURITE_MODE;

// фрагмент, показывающий историю сохраненных переводов
public class FragmentHistory extends Fragment {
    public FragmentHistory() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_history, null);
        final ListView lvMain = (ListView) view.findViewById(R.id.listHistory);

        // из базы запросил всю историю
        ArrayList<Translate> translateArrayList = getHistory();
        HistoryAdapter historyAdapter = new HistoryAdapter(getActivity(), translateArrayList);
        // и назначил показать ее через кастом адаптер списком
        lvMain.setAdapter(historyAdapter);

        // провести пальцем влево по списку истории для удаления
        final SwipeDetector swipeDetector = new SwipeDetector();
        lvMain.setOnTouchListener(swipeDetector);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
                //   Toast.makeText(getActivity(), position+"", Toast.LENGTH_SHORT).show();
                if (swipeDetector.swipeDetected()) {
                    if (swipeDetector.getAction() == SwipeDetector.Action.RL) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int numDeleted = deleteHistoryByPosition(position);
                                //refresh content так себе вариант дублирования кода для обновления
                                // придумать поинтересней что-нибудь
                                ArrayList<Translate> translateArrayList = getHistory();
                                updateList(translateArrayList, getView());
                            }
                        });
                    }
                }
            }
        });

        // по долгому клику на список переключается избранное или нет
        lvMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(getActivity(), i + "", Toast.LENGTH_SHORT).show();
                updateFavouriteByPosition(i);
                // refresh
                ArrayList<Translate> translateArrayList = getHistory();
                updateList(translateArrayList, getView());
                return false;
            }
        });

        //buttonFavOnly
        final Button favOnly = (Button) view.findViewById(R.id.buttonFavOnly);

        // цвет кнопки избранное при первом запуске фрагмента
        if (isFavMode()) {
            favOnly.setBackgroundColor(Color.YELLOW);
        } else {
            favOnly.setBackgroundColor(Color.WHITE);
        }


        // нажатие на кнопку ИЗБРАННОЕ в истории, фильтр история или избранное
        favOnly.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (isFavMode()) {
                    favOnly.setBackgroundColor(Color.WHITE);
                    SharedPreferencesClass.saveVariable(KEY_FAVOURITE_MODE, "0", getActivity());
                } else {
                    favOnly.setBackgroundColor(Color.YELLOW);
                    SharedPreferencesClass.saveVariable(KEY_FAVOURITE_MODE, "1", getActivity());
                }

                ArrayList<Translate> translateArrayList = getHistory();
                updateList(translateArrayList, getView());
                String a = ";ff";
            }
        });

        return view;
    }

    // дурацкий способ обновить список заново, надо через адаптер-нотифай
    void updateList(ArrayList<Translate> updateList, View view) {
        final ListView lvMain = (ListView) view.findViewById(R.id.listHistory);
        HistoryAdapter historyAdapter = new HistoryAdapter(getActivity(), updateList);
        lvMain.setAdapter(historyAdapter);
    }


    // возвращаю список класса Translate из бд или фаворит
    ArrayList<Translate> getHistory() {
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(this.getActivity()).openToRead();

        ArrayList<Translate> translateArrayList = null;

        if (isFavMode()) {
            translateArrayList = databaseAdapter.querryHistoryFavourite();
        } else {
            translateArrayList = databaseAdapter.querryAllHistory();
        }

        databaseAdapter.DBclose();
        return translateArrayList;
    }

    // узнаю, что было выбрано последним, показывать только избранное в истории или нет
    boolean isFavMode() {
        String favouriteMode = SharedPreferencesClass.loadVariable(KEY_FAVOURITE_MODE, getActivity());
        if (favouriteMode.equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    // дурацкое удаление по позиции, от позиции высчитывается внутренний ID в базе и уже по нему удаляется
    int deleteHistoryByPosition(int position) {
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(getActivity()).openToWrite();
        int numDeleted = databaseAdapter.deleteHistoryRowByPosition(position);
        if (numDeleted == 0) {
            Toast.makeText(getActivity(), " --===^^^ [^_^][^_^] ^^^===-- \n" + "Ошибка удаления", Toast.LENGTH_SHORT).show();
        }
        databaseAdapter.DBclose();
        return numDeleted;
    }

    // обновление по позиции
    int updateFavouriteByPosition(int position) {
        DatabaseAdapter databaseAdapter = new DatabaseAdapter(getActivity()).openToWrite();
        int numUpdated = databaseAdapter.updateFavouriteByPosition(position);
        if (numUpdated == 0) {
            Toast.makeText(getActivity(), " --===^^^ [^_^][^_^] ^^^===-- \n" + "Ошибка обновления", Toast.LENGTH_SHORT).show();
        }
        databaseAdapter.DBclose();
        return numUpdated;
    }
}

