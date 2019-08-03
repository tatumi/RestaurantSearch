package com.example.masami.restaurantsearch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageGetTask extends AsyncTask<String,Void,Bitmap> {
    ImageView mImageView;

    public ImageGetTask(ImageView imageview){
        mImageView = imageview;
    }

    @Override
    protected Bitmap doInBackground(String... urls) {



        try {
            URL imageUrl = new URL(urls[0]);
            InputStream imageIs;
            imageIs = imageUrl.openStream();

            return BitmapFactory.decodeStream(imageIs);



        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        bitmap  = Bitmap.createScaledBitmap(bitmap,800,600,false);
        mImageView.setImageBitmap(bitmap);
    }
}
