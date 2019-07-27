package com.example.masami.restaurantsearch;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SearchFragment extends Fragment {

    private Spinner mRangeSpinner;
    private Button mSearchButton;
    private String mSpinnerValue;
    private GurunaviAPI mGurunaviAPI;
    private SearchFragmentListener searchFragmentListener;


    public interface SearchFragmentListener{
        void onSearchGo(String str);
    }




    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        searchFragmentListener = (SearchFragmentListener) getActivity();
        return inflater.inflate(R.layout.search_fragment,container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //スピナーのID取得
        mRangeSpinner =  view.findViewById(R.id.spinner);

        //スピナーに表示するラベル取得
        String[] rangeLabels = getResources().getStringArray(R.array.item_label);

        //アダプター生成
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                rangeLabels
        );

        //展開時の表示方法指定
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //アダプターをスピナーに設定
        mRangeSpinner.setAdapter(spinnerAdapter);
        mRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //APIに合わせた値に変換
                switch (view.toString()) {
                    case "300m":
                        mSpinnerValue = "1";
                        break;
                    case "500m":
                        mSpinnerValue = "2";
                        break;
                    case "1000m":
                        mSpinnerValue = "3";
                        break;
                    case "2000m":
                        mSpinnerValue = "4";
                        break;
                    case "3000m":
                        mSpinnerValue = "5";
                        break;
                        default:
                            mSpinnerValue = "2";
                            break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSearchButton = view.findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFragmentListener.onSearchGo(mSpinnerValue);

                ResultFragment resultFragment = new ResultFragment();
                Bundle bundle = new Bundle();
                bundle.putString("Range",mSpinnerValue);
                resultFragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.container,resultFragment);

                transaction.addToBackStack(null);
                transaction.commit();


            }
        });


    }
}
