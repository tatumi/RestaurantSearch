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
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchFragment extends Fragment implements GurunaviAPI.ResponseListener{

    private Spinner mRangeSpinner;
    private Button mSearchButton;
    private Spinner mCategorySpinner;
    private String mRangeValue;
    private int mCategoryValue;
    private ArrayList<String> mCategoryCodes;
    private final String GURUNAVI_URL = "https://api.gnavi.co.jp/api/scope/";
    private final String CATEGORY_URL = "https://api.gnavi.co.jp/master/CategoryLargeSearchAPI/v3/";
    private final String ACCESSKEY = "a353b5c33a17db00f464d89dbc5621a9";





    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.search_fragment,container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        //スピナーのID取得
        mRangeSpinner =  view.findViewById(R.id.rangeSpinner);
        mCategorySpinner = view.findViewById(R.id.categorySpinner);

        getCategory();

        //スピナーに表示するラベル取得
        String[] rangeLabels = getResources().getStringArray(R.array.item_label);

        //アダプター生成
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
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
                mRangeValue = Integer.toString(position+1);

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
                bundle.putString("Range", mRangeValue);
                bundle.putString("category",mCategoryCodes.get(mCategoryValue));
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

    void getCategory(){
        GurunaviAPI gurunaviAPI = new GurunaviAPI(this);

        HashMap<String,String> map = new HashMap<>();
        map.put("url",CATEGORY_URL);
        map.put("keyid",ACCESSKEY);
        map.put("lang","ja");

        gurunaviAPI.search(map);

    }

    void openBrowser(String urlstr){
        Uri uri = Uri.parse(urlstr);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }


    @Override
    public void onResponseDataReceived(String responseData) {

        ArrayList<String> categories = new ArrayList<>();
        mCategoryCodes = new ArrayList<>();

        categories.add("カテゴリ:未選択");
        mCategoryCodes.add("");

        try {

            JSONArray categoryArray = (new JSONObject(responseData)).getJSONArray("category_l");
            for(int i = 0; i<categoryArray.length();i++){
                categories.add(categoryArray.getJSONObject(i).getString("category_l_name"));
                mCategoryCodes.add(categoryArray.getJSONObject(i).getString("category_l_code"));
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        ArrayAdapter adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpinner.setAdapter(adapter);

    }
}
