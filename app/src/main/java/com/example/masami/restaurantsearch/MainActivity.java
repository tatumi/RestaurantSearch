package com.example.masami.restaurantsearch;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements LocationListener{

    private String mLatitude;
    private String mLongitude;
    private LocationManager mLocationManager;
    private String mBestProvider;
    private static final int REQUEST_LOCATION_PERMISSION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){//初回起動 or 前回の記録なし

            //フラグメントをアクティビティに貼り付け
            FragmentManager fragmentManager = getSupportFragmentManager();
            SearchFragment searchFragment = new SearchFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.container,searchFragment)
                    .commit();

            fragmentManager.executePendingTransactions(); // FragmentのTransaction処理の完了同期待ち
        }

        //GPSを使う準備
        initLocationManager();

        //測位開始
        startLocation();


    }

    @Override
    public void onStop() {
        super.onStop();

        //測位停止
        stopLocation();
    }

    //GPSの権限チェック
    private void checkPermission(){
        //権限の許可を受けているか
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //許可がなければ許可を取得する
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }


    //測位開始する
    private void startLocation() {
        checkPermission();
        mLocationManager.requestLocationUpdates(mBestProvider, 60000,3,this);
    }

    //測位停止する
    private void stopLocation(){
        mLocationManager.removeUpdates(this);
    }

    //LocationManagerを使う準備をする
    private void initLocationManager() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);           //正確な位置情報をつかう
        criteria.setPowerRequirement(Criteria.POWER_LOW);       //電力消費控えめ
        criteria.setSpeedRequired(false);                       //速さ情報．使わない
        criteria.setAltitudeRequired(false);                    //高さ情報．使わない
        criteria.setBearingRequired(false);                     //向き情報．使わない
        criteria.setCostAllowed(true);                          //ネットを使って精度高める
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        //最良のプロバイダーを選択
        mBestProvider = mLocationManager.getBestProvider(criteria, true);
    }

    //現在地が更新された
    @Override
    public void onLocationChanged(Location location) {
        //メンバに保存
        mLatitude = Double.toString(location.getLatitude());
        mLongitude = Double.toString(location.getLongitude());

        Log.d("DEBUG", "called onLocationChanged");
        Log.d("DEBUG", "lat : " + location.getLatitude());
        Log.d("DEBUG", "lon : " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

}
