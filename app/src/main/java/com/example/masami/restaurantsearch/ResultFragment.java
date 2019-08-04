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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;

public class ResultFragment extends Fragment implements GurunaviAPI.ResponseListener{

    private String mRange;
    private String mLatitude;
    private String mLongitude;
    private JSONObject mJsonObject;
    private ListView mListView;
    private View mHeader;
    private View mFooter;
    private int mPageCount=1;
    private int mTotalHit;
    private int mOffset;
    private RestaurantAdapter mAdapter;
    private ResultFragment mResultFragment;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return  inflater.inflate(R.layout.result_fragment,container,false);

    }

    //GPSを使った検索を開始
    public void searchGo(String range,String page){
        GurunaviAPI gurunaviAPI = new GurunaviAPI(this);

        mLatitude = "35.657575";
        mLongitude = "139.702234";
        //    mLatitude = ((MainActivity)getActivity()).getLatitude();
        //    mLongitude = ((MainActivity)getActivity()).getLongitude();

        gurunaviAPI.searchGPS(mLatitude, mLongitude, range, page);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mResultFragment = this;
        if(savedInstanceState==null) {
            Log.d("DEBUG","ONCREATE");

            Bundle bundle = getArguments();
            if(bundle != null) {

                mRange = bundle.getString("Range");
                mPageCount = bundle.getInt("PageCount");
            }

            if(mPageCount==0)mPageCount=1;

        }else {
            mRange = savedInstanceState.getString("Range");
            mPageCount = savedInstanceState.getInt("PageCount");


        }

        searchGo(mRange, Integer.toString(mPageCount));


        mListView = view.findViewById(R.id.restaurantList);
        //アダプターをリストビューにセット
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(getView().findViewById(R.id.emptyView));     //読み込み画面登録
        mListView.addHeaderView(getHeader());
        mListView.addFooterView(getFooter());

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {        //リスナー登録
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //詳細画面へ画面遷移
                //遷移先のインスタンス生成
                DetailFragment detailFragment = new DetailFragment();
                Bundle detailBundle = new Bundle();
                Bundle thisBundle = new Bundle();
                try {
                    //選択された店のデータを詳細画面に転送
                    detailBundle.putString("rest",mJsonObject.getJSONArray("rest").getJSONObject(position-1).toString());
                    detailBundle.putInt("posi",position);
                    thisBundle.putInt("PageCount",mPageCount);
                    thisBundle.putString("Range",mRange);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                detailFragment.setArguments(detailBundle);
                mResultFragment.setArguments(thisBundle);


                //画面遷移
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.container,detailFragment);

                //戻るボタン挙動
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });




    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("Range",mRange);
        outState.putInt("PageCount",mPageCount);
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
                    mPageCount++;
                    searchGo(mRange,Integer.toString(mPageCount));
                }
            });

            //前のページへ戻るボタンの挙動
            mFooter.findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPageCount--;
                    searchGo(mRange,Integer.toString(mPageCount));
                }
            });



            ((Spinner)mFooter.findViewById(R.id.pageIndex)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(position != mPageCount-mOffset-1) {
                        mPageCount = position+mOffset;
                        searchGo(mRange, Integer.toString(position + mOffset));
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
        ArrayList<Restaurant> restaurants = new ArrayList<>();

        try {
            //JSONObjectで情報を取得
            mJsonObject = new JSONObject(responseData);
            //今回ヒットした店数
            int pageNum = mJsonObject.getInt("hit_per_page");
            //総ヒット件数
            mTotalHit = mJsonObject.getInt("total_hit_count");
            for (int i = 0; i < pageNum; i++) {
                //Restaurantインスタンスを作成
                Restaurant restaurant = new Restaurant();
                //レストラン情報各種をRestaurantのメンバに登録
                restaurant.setAll(mJsonObject.getJSONArray("rest").getJSONObject(i));
                //リストに追加
                restaurants.add(restaurant);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //RestaurantAdapterのインスタンス作成
        mAdapter = new RestaurantAdapter(this.getContext(), 0,restaurants );
        mListView.setAdapter(mAdapter);


        //ヘッダに文字列を設定
        ((TextView)mHeader.findViewById(R.id.pageAnnounce)).setText(
                String.format(getResources().getString(R.string.pageAnnounce),
                        mTotalHit,(10*mPageCount-9),10*mPageCount));


        int totalpage = (int)Math.ceil(mTotalHit/10);

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

        ((Spinner)mFooter.findViewById(R.id.pageIndex)).setAdapter(spinnerAdapter);
        ((Spinner)mFooter.findViewById(R.id.pageIndex)).setSelection(mPageCount-mOffset-1);



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
                //URLから画像を取得する
                ImageGetTask imageGetTask = new ImageGetTask((ImageView) convertView.findViewById(R.id.thumbnail));
                imageGetTask.execute(restaurant.getShopURL1());
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
