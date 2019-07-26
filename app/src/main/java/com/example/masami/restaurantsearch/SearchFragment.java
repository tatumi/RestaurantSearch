package com.example.masami.restaurantsearch;


import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SearchFragment extends Fragment {

    private Spinner mRangeSpinner;
    private Button mSearchButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.search_fragment,container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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

        mSearchButton = view.findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        super.onViewCreated(view, savedInstanceState);
    }
}
