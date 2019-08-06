package com.example.masami.restaurantsearch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageGetTask extends AsyncTask<String,Void,Bitmap[]> {

    OnImageResponseListener mListener;

    interface OnImageResponseListener{
        void onImageDataReceived(Bitmap[] bitmap);
    }

    ImageGetTask(OnImageResponseListener listener){
        super();
        mListener = listener;
    }

    @Override
    protected Bitmap[] doInBackground(String... urls) {


        Bitmap[] result = new Bitmap[urls.length];
        try {
            for(int i=0;i<urls.length;i++) {
                if(urls[i].equals("")){
                    result[i] = null;
                    continue;
                }
                URL imageUrl = new URL(urls[i]);
                InputStream imageIs = imageUrl.openStream();
                result[i] = Bitmap.createScaledBitmap(
                        BitmapFactory.decodeStream(imageIs),
                        800,600,false);
            }
            return result;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap[] bitmap) {
        super.onPostExecute(bitmap);
        mListener.onImageDataReceived(bitmap);
    }
}
