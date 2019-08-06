package com.example.masami.restaurantsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultFragment extends Fragment implements GurunaviAPI.ResponseListener,ImageGetTask.OnImageResponseListener{
    /*-------定数-------*/
    private final String ACCESSKEY = "a353b5c33a17db00f464d89dbc5621a9";    //APIのアクセスキー
    private final String SEARCHURL = "https://api.gnavi.co.jp/RestSearchAPI/v3/";   //APIのURL
    private final int PAGENUM = 10;     //1ページに表示するレストランの数
    /*------/定数-------*/

    /*------検索パラメータ-------*/
    private String mRange;      //現在地からの検索範囲
    private String mCategory;   //絞り込む大業態コード
    private String mFreeWord;   //フリーワード検索機能のフリーワード
    /*------/検索パラメータ-------*/

    /*------View-------*/
    private JSONObject mJsonObject;     //APIからのJSONを保存
    private ListView mListView;         //レストラン情報を表示するListView
    private View mHeader;               //ListViewのヘッダー．ページ番号などを表示
    private View mFooter;               //ListViewのフッター．次ページへのボタンなどを配置
    /*------/View-------*/

    /*------ページ管理関係-------*/
    private int mPageCount=1;           //表示中のページ番号
    private int mTotalHit;              //検索条件でヒットした店舗の総数
    private int mOffset;                //ページジャンプSpinnerの要素をずらすための変数
    private ResultFragment mResultFragment;         //ページの状態保存用のインスタンス
    private ArrayList<Restaurant> mRestaurants;     //店舗情報をまとめるためのインスタンスをまとめるリスト
    /*------/ページ管理関係-------*/



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        //フラグメントを貼り付け
        return  inflater.inflate(R.layout.result_fragment,container,false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ページの状態保存用のインスタンス
        mResultFragment = this;
        if(savedInstanceState!=null) {//savedInsの中身があれば
            //各検索パラメータ復帰
            mRange = savedInstanceState.getString("Range");
            mPageCount = savedInstanceState.getInt("PageCount");
            mCategory = savedInstanceState.getString("Category");
            mFreeWord = savedInstanceState.getString("FreeWord");

        }else {//中身がない場合

            //bundle取得
            Bundle bundle = getArguments();
            if(bundle != null) {//bundleの中身があれば
                //各検索パラメータ入力
                mRange = bundle.getString("Range");
                mPageCount = bundle.getInt("PageCount");
                mCategory = bundle.getString("Category");
                mFreeWord = bundle.getString("FreeWord");
            }

            if(mPageCount==0)mPageCount=1;


        }

        //検索を開始
        searchGo();

        //ListViewのデータ以外を設定
        mListView = view.findViewById(R.id.restaurantList);
        mListView.setEmptyView(getView().findViewById(R.id.emptyView));     //読み込み画面登録
        mListView.addHeaderView(getHeader());       //ヘッダー追加
        mListView.addFooterView(getFooter());       //フッター追加

        //リスナー登録
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(position != 0) {
                    //詳細画面へ画面遷移
                    //遷移先のインスタンス生成
                    DetailFragment detailFragment = new DetailFragment();

                    Bundle detailBundle = new Bundle();     //遷移先のbundle
                    Bundle thisBundle = new Bundle();       //このFragmentのbundle
                    try {
                        //選択された店のデータを詳細画面に転送
                        detailBundle.putString("rest", mJsonObject.getJSONArray("rest").getJSONObject(position - 1).toString());

                        //検索パラメータを保存
                        thisBundle.putInt("PageCount", mPageCount);
                        thisBundle.putString("Range", mRange);
                        thisBundle.putString("FreeWord",mFreeWord);
                        thisBundle.putString("Category",mCategory);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //bundle登録
                    detailFragment.setArguments(detailBundle);
                    mResultFragment.setArguments(thisBundle);


                    //画面遷移
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.container, detailFragment);

                    //戻るボタン挙動設定
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //検索パラメータ保存
        outState.putString("Range",mRange);
        outState.putInt("PageCount",mPageCount);
        outState.putString("FreeWord",mFreeWord);
        outState.putString("Category",mCategory);
    }


    //GPSを使った検索を開始
    public void searchGo(){
        //非同期処理担当のクラスをインスタンス化
        GurunaviAPI gurunaviAPI = new GurunaviAPI(this);

        String latitude = "35.657575";
        String longitude = "139.702234";
        //位置情報を取得
        //    mLatitude = ((MainActivity)getActivity()).getLatitude();
        //    mLongitude = ((MainActivity)getActivity()).getLongitude();

        //クエリに設定するパラメータをHashMapでまとめる
        HashMap<String,String> map = new HashMap<>();
        map.put("url",SEARCHURL);
        map.put("keyid",ACCESSKEY);
        map.put("latitude",latitude);
        map.put("longitude",longitude);
        map.put("range",mRange);
        map.put("offset_page",Integer.toString(mPageCount));

        //カテゴリが指定されていれば
        if(!mCategory.equals(""))
            map.put("category_l",mCategory);

        //フリーワードが入力されていれば
        if(!mFreeWord.equals(""))
            map.put("freeword",mFreeWord);

        //処理開始
        gurunaviAPI.search(map);
    }

    //リストビューのフッターを返す
    private View getHeader() {
        if (mHeader == null) {
            mHeader = getLayoutInflater().inflate(R.layout.listview_header, null);

        }
        return mHeader;
    }

    //リストビューのフッターを返す
    private View getFooter() {
        if (mFooter == null) {
            mFooter = getLayoutInflater().inflate(R.layout.listview_footer, null);

            //次のページへ進むボタンの挙動
            mFooter.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ページを進める
                    mPageCount++;
                    searchGo();
                }
            });

            //前のページへ戻るボタンの挙動
            mFooter.findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ページを戻る
                    mPageCount--;
                    searchGo();
                }
            });

            //ページジャンプ機能を持ったSpinnerの動作を登録
            ((Spinner)mFooter.findViewById(R.id.pageIndex)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //現在のページ以外が指定されたら
                    if(position != mPageCount-mOffset-1) {
                        //ページをジャンプ
                        mPageCount = position+mOffset+1;
                        searchGo();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });



        }
        return mFooter;
    }

    //APIの返事が来たら呼び出される
    @Override
    public void onResponseDataReceived(String responseData) {
        //アダプタ登録用のレストランリスト
        mRestaurants = new ArrayList<>();
        int pageNum = 0;
        String[] imageURLs = new String[PAGENUM];
        try {
            //JSONObjectで情報を取得
            mJsonObject = new JSONObject(responseData);
            //総ヒット件数
            mTotalHit = mJsonObject.getInt("total_hit_count");

            JSONArray restArray = mJsonObject.getJSONArray("rest");

            //今回ヒットした件数
            pageNum = restArray.length();
            for (int i = 0; i < pageNum; i++) {
                //Restaurantインスタンスを作成
                Restaurant restaurant = new Restaurant();
                //レストラン情報各種をRestaurantのメンバに登録
                restaurant.setAll(restArray.getJSONObject(i));
                imageURLs[i] = restArray.getJSONObject(i).getJSONObject("image_url").getString("shop_image1");
                //リストに追加
                mRestaurants.add(restaurant);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ImageGetTask imageGetTask = new ImageGetTask(this);
        imageGetTask.execute(imageURLs);


        //ヘッダに文字列を設定
        ((TextView)mHeader.findViewById(R.id.pageAnnounce)).setText(
                String.format(getResources().getString(R.string.pageAnnounce),
                        mTotalHit,(PAGENUM*mPageCount-PAGENUM-1),PAGENUM*mPageCount-(PAGENUM-pageNum)));


        int totalpage = (mTotalHit/PAGENUM)+1;

        if(totalpage <= mPageCount) {//次のページが存在するかどうか
            //存在しない場合，進むボタンを無効化
            mFooter.findViewById(R.id.nextButton).setEnabled(false);
        }else{
            //存在する場合，進むボタンを有効化
            mFooter.findViewById(R.id.nextButton).setEnabled(true);
        }

        if(mPageCount<2) {//前のページが存在するかどうか
            //存在しない場合，戻るボタンを無効化
            mFooter.findViewById(R.id.backButton).setEnabled(false);
        }else{
            //存在する場合，戻るボタンを有効化
            mFooter.findViewById(R.id.backButton).setEnabled(true);
        }




        if(5<mPageCount){
            mOffset = mPageCount-5;

            if((totalpage-mPageCount)<5)mOffset -= totalpage-mPageCount;

        }

        ArrayList<String> item = new ArrayList<>();

        for(int i = 1;i<=10;i++){
            if((i+mOffset)==mPageCount){
                item.add(Integer.toString(i+mOffset)+"(現在のページ)");
            }else {
                item.add(Integer.toString(i+mOffset));
            }
            if(totalpage<=(i+mOffset)){
                break;
            }
        }

        //アダプター生成
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)mFooter.findViewById(R.id.pageIndex)).setAdapter(spinnerAdapter);
        ((Spinner)mFooter.findViewById(R.id.pageIndex)).setSelection(mPageCount-mOffset-1);



    }

    @Override
    public void onImageDataReceived(Bitmap[] bitmap) {
        for(int i=0;i<bitmap.length;i++){
            if(bitmap[i] != null)
            mRestaurants.get(i).setThumbnail1(bitmap[i]);
        }
        mListView.setAdapter(new RestaurantAdapter(this.getContext(), 0,mRestaurants ));
    }


    public class RestaurantAdapter extends ArrayAdapter<Restaurant>{

        private LayoutInflater mLayoutInflater;

        RestaurantAdapter(Context context, int resource, List<Restaurant> restaurants) {
            super(context, resource, restaurants);
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView == null){
                convertView = mLayoutInflater.inflate(
                        R.layout.listlayout,
                        parent,
                        false
                );
            }

            Restaurant restaurant =  getItem(position);


            if(restaurant.mShopURL1.equals("")){//URLが空の場合
                //NOIMAGE画像を設定
                Bitmap shop = BitmapFactory.decodeResource(getResources(),R.drawable.noimage);
                shop = Bitmap.createScaledBitmap(shop,800,600,false);
                ((ImageView)convertView.findViewById(R.id.thumbnail)).setImageBitmap(shop);
            }else {

                ((ImageView) convertView.findViewById(R.id.thumbnail)).setImageBitmap(restaurant.getThumbnail1());

            }
                ((TextView) convertView.findViewById(R.id.name))
                        .setText(restaurant.getName());
                ((TextView) convertView.findViewById(R.id.accessTextView))
                        .setText(restaurant.getAccess());


                return convertView;

        }
    }

    public class Restaurant{
        private Bitmap mThumbnail1;
        private Bitmap mThumbnail2;



        private String mShopURL1 = "";
        private String mShopURL2 = "";
        private String mName;
        private String mAccess;

        public Bitmap getThumbnail1() {
            return mThumbnail1;
        }

        public void setThumbnail1(Bitmap thumbnail) {
            this.mThumbnail1 = thumbnail;
        }

        public Bitmap getThumbnail2() {
            return mThumbnail2;
        }

        public void setThumbnail2(Bitmap Thumbnail2) {
            this.mThumbnail2 = Thumbnail2;
        }

        public String getShopURL1() {
            return mShopURL1;
        }

        public void setShopURL1(String ShopURL1) {
            this.mShopURL1 = ShopURL1;
        }

        public String getShopURL2() {
            return mShopURL2;
        }

        public void setShopURL2(String ShopURL2) {
            this.mShopURL2 = ShopURL2;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            this.mName = name;
        }

        public String getAccess() {
            return mAccess;
        }

        public void setAccess(String access) {
            this.mAccess = access;
        }


        public void setAll(JSONObject obj) {
            try {
                mName = obj.getString("name");
                if(obj.getJSONObject("access").getString("line").equals("")) {
                    mAccess = getString(R.string.nodataAccess);
                }else{
                    StringBuffer access = new StringBuffer();
                    access.append(obj.getJSONObject("access").getString("line"));
                    access.append(" ");
                    access.append(obj.getJSONObject("access").getString("station"));
                    access.append(" ");
                    access.append(obj.getJSONObject("access").getString("station_exit"));
                    access.append(" ");
                    access.append(obj.getJSONObject("access").getString("walk"));
                    access.append("分");
                    mAccess = access.toString();
                }

                mShopURL1 = obj.getJSONObject("image_url").getString("shop_image1");
                mShopURL2 = obj.getJSONObject("image_url").getString("shop_image2");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }





}
