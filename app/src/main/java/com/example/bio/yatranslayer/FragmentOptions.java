package com.example.bio.yatranslayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.example.bio.yatranslayer.CONSTS.KEY_CURRENT_LANGUAGE;

public class FragmentOptions extends Fragment {
    private FragmentManager mFragmentManager;
    private Fragment fragmentPickupLanguages;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventLanguage event) {
        Toast.makeText(this.getActivity(), event.LanguageShortName, Toast.LENGTH_SHORT).show();
        final TextView textViewToChange = (TextView) getActivity().findViewById(R.id.textViewLanguage);
        textViewToChange.setText(event.LanguageShortName);
        // сохраняем язык в настройки или берем локаль в другом месте
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_options, null);


//        String recieveInfo = "";
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            recieveInfo = bundle.getString(SELECTED_LANGUAGE);
//        }

        TextView textViewLanguage = (TextView) view.findViewById(R.id.textViewLanguage);

//        if (!recieveInfo.equals("")) {
//            textViewLanguage.setText(recieveInfo);
//        }

        fragmentPickupLanguages = new FragmentPickupLanguage();


        mFragmentManager = getFragmentManager();


//        Button buttonChooseLanguage = (Button) view.findViewById(R.id.buttonChooseLanguage);
//        buttonChooseLanguage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                FragmentTransaction langFragmentTransaction = mFragmentManager
//                        .beginTransaction();
//
////                FragmentPickupLanguage fragmentLanguagesView = (FragmentPickupLanguage) mFragmentManager.findFragmentById(R.layout.fragment_pickup_language);
//                langFragmentTransaction.replace(R.id.chooseLanguagesLayout, fragmentPickupLanguages);
//
//                langFragmentTransaction.commit();
//
//
//            }
//        });

        //textViewLanguage
        String curLocale = SharedPreferencesClass.loadVariable(KEY_CURRENT_LANGUAGE, getActivity());
        if (curLocale != null){
            final TextView textViewToChange = (TextView) view.findViewById(R.id.textViewLanguage);
            textViewToChange.setText(curLocale);
        }

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        try {
            EventBus.getDefault().register(this);
        } catch (Exception e) {
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
