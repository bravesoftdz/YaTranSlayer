package com.example.bio.yatranslayer;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// Обработчик фрагмента выбора языка из списка поддерживаемых языков
public class FragmentPickupLanguage extends Fragment {
    static final String SELECTED_LANGUAGE = "selected_language";
    String message;

////   не работает
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onMessageEvent (EventButtonPressed event) {
//        Toast.makeText(getActivity(), event.sourceOrDestinationButtonPressed, Toast.LENGTH_SHORT).show();
//
////        final TextView textViewToChange = (TextView) getActivity().findViewById(R.id.textViewLanguage);
////        textViewToChange.setText(event.LanguageShortName);
//        // сохраняем язык в настройки или берем локаль в другом месте
//    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onStickyEvent(EventLanguage event) {
//        Toast.makeText(getActivity(), event.message, Toast.LENGTH_SHORT).show();
        this.message = event.message;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_pickup_language, null);


        ListView listView;
        listView = (ListView) view.findViewById(R.id.langListView);

        yandexAPI yandexApi = new yandexAPI();
        Map<String, String> languagesMap = yandexApi.getSupportedLanguages(this.getActivity());

        List<String> list = new ArrayList<>(languagesMap.values());
        List<String> listShortNames = new ArrayList<>(languagesMap.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);

        final ArrayList<String> finalSupportedShortLanguages = (ArrayList<String>) listShortNames;

        SearchView search = (SearchView) view.findViewById(R.id.searchView);
        search.setActivated(true);
        search.onActionViewExpanded();
        search.setIconified(false);
        search.clearFocus();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String selectedFullLanguageName = "";
                String selectedShortLanguageName = finalSupportedShortLanguages.get(position);
//                selectedFullLanguageName = finalSupportedLanguagesFullNames.get(position);

               // Toast.makeText(getActivity(), selectedShortLanguageName + " - " + selectedFullLanguageName, Toast.LENGTH_SHORT).show();

                // еще сохранить направление перевода ru-en, если совпадает с разрешенным списком

                // переключаемся на фрагмент Настроек и передаю ему ключ "key"
                // со значением "Русский", полное название языка для вывода сообщений


                // посылаю автобусом сообщение всем кто услышит, сейчас это FragmegnOptions
                EventBus.getDefault().postSticky(new EventLanguage(selectedShortLanguageName, message));


//                SharedPreferencesClass.saveVariable(KEY_CURRENT_LANGUAGE, selectedShortLanguageName, getActivity());

                // самоуничтожаюсь
                getFragmentManager().beginTransaction().remove(FragmentPickupLanguage.this).commitAllowingStateLoss();

            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e){
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


}
