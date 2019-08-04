package com.example.masami.restaurantsearch;


import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

public class SearchFragment extends Fragment{

    private Spinner mRangeSpinner;
    private Button mSearchButton;
    private String mSpinnerValue;
    private final String GURUNAVI_URL = "https://api.gnavi.co.jp/api/scope/";





    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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
                mSpinnerValue = Integer.toString(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //500mを初期状態に
        mRangeSpinner.setSelection(1);

        //ボタンにイベントリスナー登録
        mSearchButton = view.findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //検索結果画面へ画面遷移
                //遷移先のインスタンス生成
                ResultFragment resultFragment = new ResultFragment();

                //フォームの内容を検索結果画面に転送
                Bundle bundle = new Bundle();
                if(mSpinnerValue.equals("0"))mSpinnerValue = "2";
                bundle.putString("Range",mSpinnerValue);
                resultFragment.setArguments(bundle);

                //画面遷移
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.container,resultFragment);

                //戻るボタン挙動
                transaction.addToBackStack(null);
                transaction.commit();


            }
        });

        (view.findViewById(R.id.gurunaviImage)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBrowser(GURUNAVI_URL);
            }
        });


    }
    void openBrowser(String urlstr){
        Uri uri = Uri.parse(urlstr);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }


}
