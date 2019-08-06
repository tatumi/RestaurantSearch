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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//ぐるなびAPIとやり取りするクラス
public class GurunaviAPI extends AsyncTask<HashMap<String,String>,Void,String>{

    //タイムアウトとか
    final int CONNECTION_TIMEOUT = 3000;
    final int READ_TIMEOUT = 3000;

    //リスナーを保存しておく変数
    private ResponseListener mListener;
    //インタフェース
    //APIの返事を受け取ったら呼ばれる
    public interface ResponseListener{
       void onResponseDataReceived(String responseData);
    }



    //コンストラクタ
    GurunaviAPI(ResponseListener listener){
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

    //GPSで検索
    public void search(HashMap map){
        this.execute(map);
    }

    //ぐるなびAPIをたたく
    @SafeVarargs
    @Override
    protected final String doInBackground(HashMap<String, String>... param) {
        String result = "";
        URL url;
        HttpURLConnection connection = null;

        try {
            //クエリの設定
            Uri.Builder builder = new Uri.Builder();


            String urlstr = param[0].get("url");
            param[0].remove("url");

            for(Map.Entry<String,String> entry : param[0].entrySet()){
                builder.appendQueryParameter(entry.getKey(),entry.getValue());
            }


            //URLにクエリを追加
            url = new URL(urlstr + builder.toString());

            //コネクションの設定
            connection = (HttpURLConnection) url.openConnection();

            //タイムアウト
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            //必要なさそうなのでコメントアウト
         //   connection.addRequestProperty("User-Agent","Android");
         //   connection.addRequestProperty("Accept-Language", Locale.getDefault().toString());

         //   connection.setDoOutput(false);
         //   connection.setDoOutput(true);

            connection.setRequestMethod("GET");
            connection.connect();   //リクエスト送信

            //ステータスコード取得
            int statusCode = connection.getResponseCode();
            if(statusCode == HttpURLConnection.HTTP_OK){    //接続成功していれば

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

                StringBuilder res = new StringBuilder();
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

