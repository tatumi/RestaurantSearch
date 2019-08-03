package com.example.masami.restaurantsearch;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultFragment extends Fragment implements GurunaviAPI.ResponseListener{

    private String mRange;
    private String mLatitude;
    private String mLongitude;
    private JSONObject mJsonObject;
    private ListView mListView;
    private View mFooter;
    private int mPageCount=1;
    private RestaurantAdapter mAdapter;
    private ResultFragment mResultFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);


        if(savedInstanceState==null) {
            mResultFragment = this;
            Bundle bundle = getArguments();
            if(bundle != null) {

                mRange = bundle.getString("Range");
                mPageCount = bundle.getInt("PageCount");
            }
            if(mPageCount==0)mPageCount=1;
            mLatitude = "35.657575";
            mLongitude = "139.702234";
            //    mLatitude = ((MainActivity)getActivity()).getLatitude();
            //    mLongitude = ((MainActivity)getActivity()).getLongitude();

            searchGo(mLatitude, mLongitude, mRange, Integer.toString(mPageCount));

        }
        return  inflater.inflate(R.layout.result_fragment,container,false);

    }

    //GPSを使った検索を開始
    public void searchGo(String latitude, String longitude, String range,String page){
        GurunaviAPI gurunaviAPI = new GurunaviAPI(this);
        gurunaviAPI.searchGPS(latitude, longitude, range, page);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = view.findViewById(R.id.restaurantList);
        //アダプターをリストビューにセット
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(getView().findViewById(R.id.emptyView));     //読み込み画面登録
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
                    detailBundle.putString("rest",mJsonObject.getJSONArray("rest").getJSONObject(position).toString());
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

    //リストビューのフッターを返す
    private View getFooter() {
        if (mFooter == null) {
            mFooter = getLayoutInflater().inflate(R.layout.listview_footer, null);
            mFooter.findViewById(R.id.nextButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPageCount++;
                    searchGo(mLatitude,mLongitude,mRange,Integer.toString(mPageCount));
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
            //ヒットした店数
            int pageNum = mJsonObject.getInt("hit_per_page");

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


            //自作アダプターRestaurantAdapterのインスタンス作成
        mAdapter = new RestaurantAdapter(this.getContext(), 0,restaurants );
        mListView.setAdapter(mAdapter);


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
