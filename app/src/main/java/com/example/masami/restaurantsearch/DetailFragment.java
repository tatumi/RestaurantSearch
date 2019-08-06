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

public class DetailFragment extends Fragment{

    private JSONObject mRestrant;
    private String mBrowseURI;
    private String mPhoneNumber;
    private String mDestination;
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

                ((TextView) view.findViewById(R.id.nameTextView)).setText(mRestrant.getString("name"));
                ((TextView) view.findViewById(R.id.prTextView)).setText(mRestrant.getJSONObject("pr").getString("pr_short"));
                ((TextView) view.findViewById(R.id.categoryTextView)).setText(mRestrant.getString("category"));

                mDestination = mRestrant.getString("address");
                ((TextView) view.findViewById(R.id.addressTextView)).setText(mDestination.replaceAll(" ","\n"));


                mPhoneNumber = mRestrant.getString("tel").replaceAll("-","");
                ((TextView) view.findViewById(R.id.phoneTextView)).setText(mRestrant.getString("tel"));

                mBrowseURI = mRestrant.getString("url_mobile");
                ((TextView) view.findViewById(R.id.URLTextView)).setText(mBrowseURI);

                if (mRestrant.getJSONObject("image_url").getString("shop_image1").equals("")) {//URLが空の場合
                    //NOIMAGE画像を設定
                    Bitmap shop = BitmapFactory.decodeResource(getResources(), R.drawable.noimage);
                    shop = Bitmap.createScaledBitmap(shop, 800, 600, false);
                    ((ImageView) view.findViewById(R.id.shopImage)).setImageBitmap(shop);
                } else {
                    //URLから画像を取得する
                    ImageGetTask imageGetTask = new ImageGetTask((ImageView) view.findViewById(R.id.shopImage));
                    imageGetTask.execute(mRestrant.getJSONObject("image_url").getString("shop_image1"));
                }

                (view.findViewById(R.id.addressTextView)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openMapApp(mDestination);
                    }
                });

                (view.findViewById(R.id.phoneImage)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPhoneApp(mPhoneNumber);
                    }
                });

                (view.findViewById(R.id.URLTextView)).setOnClickListener(new View.OnClickListener() {
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

        // String   mLatitude = ((MainActivity)getActivity()).getLatitude();
        // String   mLongitude = ((MainActivity)getActivity()).getLongitude();
        String lat = "35.657575";
        String lon = "139.702234";
        // 起点の緯度,経度, 目的地の緯度,経度
        String str = String.format(Locale.JAPAN,
                "http://maps.google.com/maps?saddr=%s,%s&daddr=%s",
                lat,lon,destination);

        intent.setData(Uri.parse(str));
        startActivity(intent);


    }
}
