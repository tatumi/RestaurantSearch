package com.example.masami.restaurantsearch;


import android.net.Uri;
import android.os.AsyncTask;
import android.renderscript.ScriptGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

//ぐるなびAPIとやり取りするクラス
public class GurunaviAPI extends AsyncTask<String,Void,String>{
    final byte USE_GPS = 1<<0;
    final byte USE_SORT = 1<<1;

    final int CONNECTION_TIMEOUT = 3000;
    final int READ_TIMEOUT = 3000;
    private String time ;
    final private String KEY = "2e585a9e680e1e6bf8d3857abdb3c0c1";
    final private String urlstr = "https://api.gnavi.co.jp/RestSearchAPI/v3/";
   public interface ResponseListener{
       void onResponseDataReceived(String responseData);
   }

   private ResponseListener mListener;

   public  GurunaviAPI(ResponseListener listener){
       super();
       if(listener == null){
           listener = new ResponseListener() {
               @Override
               public void onResponseDataReceived(String responseData) {

               }
           };
       }
       mListener = listener;
   }

   public void searchGPS(String lati, String longi, String range){
        this.execute(lati,longi,range);
   }

    @Override
    protected String doInBackground(String... param) {
       String result = "";
       URL url;
        HttpURLConnection connection = null;
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.appendQueryParameter("keyid",KEY);
            builder.appendQueryParameter("latitude",param[0]);
            builder.appendQueryParameter("longitude",param[1]);
            builder.appendQueryParameter("range",param[2]);

            url = new URL(urlstr + builder.toString());

            connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
         //   connection.addRequestProperty("User-Agent","Android");
         //   connection.addRequestProperty("Accept-Language", Locale.getDefault().toString());
            connection.setRequestMethod("GET");
         //   connection.setDoOutput(false);
         //   connection.setDoOutput(true);
            URL tmp = connection.getURL();
            connection.connect();


            int statusCode = connection.getResponseCode();
            if(statusCode == HttpURLConnection.HTTP_OK){    //接続できたか確認

                //読み取る際の文字コードを取得
                String encoding = connection.getContentEncoding();
                if(encoding == null){
                    //指定がなければUTF-8
                    encoding = "UTF-8";
                }

                //読み出しの準備(Buffered Reader)
                final InputStream in = connection.getInputStream();
                final InputStreamReader inReader = new InputStreamReader(in,encoding);
                final BufferedReader bufferedReader = new BufferedReader(inReader);

                StringBuffer res = new StringBuffer();
                //読み出しに使う変数
                String line;

                while((line = bufferedReader.readLine()) != null){
                    //読み出すものが存在する間，resultに書き込み続ける
                    res.append(line);
                }

                result = res.toString();

                //readerのclose
                bufferedReader.close();
                inReader.close();
                in.close();


            }

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(connection != null){
                //最後にコネクション切断
                connection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String response) {
       //処理終了したらコールバック
        mListener.onResponseDataReceived(response);
    }
}

