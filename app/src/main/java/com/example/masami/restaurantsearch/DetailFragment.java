package com.example.masami.restaurantsearch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

public class DetailFragment extends Fragment implements ImageGetTask.OnImageResponseListener{

    private JSONObject mRestrant;

    private String mBrowseURI;          //Webで店舗情報を見る際のURL
    private String mPhoneNumber;        //店舗の電話番号
    private String mDestination;        //店舗の住所


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.detail_fragment,container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle!=null) {
            try {

                mRestrant = new JSONObject(bundle.getString("rest"));
                //各店舗情報をJSONから抜き出してViewに設定
                //店舗名
                ((TextView) view.findViewById(R.id.nameTextView)).setText(mRestrant.getString("name"));
                //PR
                ((TextView) view.findViewById(R.id.prTextView)).setText(mRestrant.getJSONObject("pr").getString("pr_short"));
                //カテゴリ
                ((TextView) view.findViewById(R.id.categoryTextView)).setText(mRestrant.getString("category"));
                //大業態
                ((TextView) view.findViewById(R.id.category_lTextView)).setText(mRestrant.getJSONObject("code").getJSONArray("category_name_l").getString(0));

                //経路案内で使うため住所は別で保存
                mDestination = mRestrant.getString("address");
                //住所
                ((TextView) view.findViewById(R.id.addressTextView)).setText(mDestination.replaceAll(" ","\n"));

                //電話番号も後で使うため保存
                mPhoneNumber = mRestrant.getString("tel").replaceAll("-","");
                //電話番号
                ((TextView) view.findViewById(R.id.phoneTextView)).setText(mRestrant.getString("tel"));

                //店舗URL保存
                mBrowseURI = mRestrant.getString("url_mobile");

                //営業時間
                ((TextView) view.findViewById(R.id.openTimeTextView)).setText(mRestrant.getString("opentime"));

                //休業日
                ((TextView) view.findViewById(R.id.holidayTextView)).setText(mRestrant.getString("holiday"));

                if (mRestrant.getJSONObject("image_url").getString("shop_image1").equals("")) {//URLが空の場合
                    //NOIMAGE画像を設定
                    Bitmap shop = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
                    shop = Bitmap.createScaledBitmap(shop, 800, 600, false);
                    ((ImageView) view.findViewById(R.id.shopImage)).setImageBitmap(shop);
                } else {
                    //URLから画像を取得する
                    ImageGetTask imageGetTask = new ImageGetTask(this);
                    imageGetTask.execute(mRestrant.getJSONObject("image_url").getString("shop_image1"));
                }

                //各ボタンの挙動設定
                (view.findViewById(R.id.NavigationButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openMapApp(mDestination);
                    }
                });

                (view.findViewById(R.id.CallButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPhoneApp(mPhoneNumber);
                    }
                });

                (view.findViewById(R.id.GoBrowseButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openBrowser(mBrowseURI);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    void openBrowser(String urlstr){
        Uri uri = Uri.parse(urlstr);
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }

    void openPhoneApp(String number){
        Uri uri = Uri.parse("tel:"+number);
        Intent intent = new Intent(Intent.ACTION_DIAL,uri);
        startActivity(intent);
    }

    void openMapApp(String destination){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity");

        String   latitude = ((MainActivity)getActivity()).getLatitude();
        String   longitude = ((MainActivity)getActivity()).getLongitude();
        // 起点の緯度,経度, 目的地の緯度,経度
        String str = String.format(Locale.JAPAN,
                "http://maps.google.com/maps?saddr=%s,%s&daddr=%s",
                latitude,longitude,destination);

        intent.setData(Uri.parse(str));
        startActivity(intent);


    }

    @Override
    public void onImageDataReceived(Bitmap[] bitmap) {
        ((ImageView) getView().findViewById(R.id.shopImage)).setImageBitmap(bitmap[0]);
    }
}
