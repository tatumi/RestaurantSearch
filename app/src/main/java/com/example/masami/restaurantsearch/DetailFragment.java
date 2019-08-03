package com.example.masami.restaurantsearch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

public class DetailFragment extends Fragment{

    private JSONObject mRestrant;


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
                ((TextView) view.findViewById(R.id.addressTextView)).setText(mRestrant.getString("address").replaceAll(" ","\n"));
                ((TextView) view.findViewById(R.id.phoneTextView)).setText(mRestrant.getString("tel"));

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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
