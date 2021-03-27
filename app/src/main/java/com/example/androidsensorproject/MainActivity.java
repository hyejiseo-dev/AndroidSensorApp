package com.example.androidsensorproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity implements SensorEventListener{


    TextView tv_magn, tv_gyro, tv_acce, tv_gps;
    ToggleButton btn_magn, btn_gyro, btn_acce, btn_gps;

    TextView tv_count;

    //acce
    SensorManager a_sensorManager;
    Sensor acceSensor, gyroSensor, magnSensor;

    double pitch, roll, yaw;

    double timestamp, dt;

    double RAD2DGR = 180 / Math.PI;
    static final float NS2S = 1.0f/1000000000.0f;

    long lastTime;

    float speed;
    float lastX, lastY, lastZ;

    int count = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_magn = (TextView) findViewById(R.id.tv_magn);
        tv_gyro = (TextView) findViewById(R.id.tv_gyro);
        tv_acce = (TextView) findViewById(R.id.tv_acce);
        tv_gps = (TextView) findViewById(R.id.tv_gps);

        btn_magn = (ToggleButton) findViewById(R.id.btn_magn);
        btn_gyro = (ToggleButton) findViewById(R.id.btn_gyro);
        btn_acce = (ToggleButton) findViewById(R.id.btn_acce);
        btn_gps = (ToggleButton) findViewById(R.id.btn_gps);



        a_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //acce
        acceSensor = a_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        a_sensorManager.registerListener(this, acceSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //gyro
        gyroSensor = a_sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        a_sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //mag
        magnSensor = a_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        a_sensorManager.registerListener(this, magnSensor, SensorManager.SENSOR_DELAY_NORMAL);


        //GPS
        if ((Build.VERSION.SDK_INT >= 23) && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        final LocationManager g_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(btn_gps.isChecked()){
                        tv_gps.setText("수신중..");
                        // GPS 제공자의 정보가 바뀌면 콜백하도록 리스너 등록하기~!!!
                        g_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                                100, // 통지사이의 최소 시간간격 (miliSecond)
                                1, // 통지사이의 최소 변경거리 (m)
                                g_locationListener);
                        g_locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                                100, // 통지사이의 최소 시간간격 (miliSecond)
                                1, // 통지사이의 최소 변경거리 (m)
                                g_locationListener);
                    }else{
                        tv_gps.setText("GPS 위치");
                        g_locationManager.removeUpdates(g_locationListener);  //  미수신할때는 반드시 자원해체를 해주어야 한다.
                    }
                }catch(SecurityException ex){
                }
            }
        });
    }


    //GPS
    private final LocationListener g_locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자

            tv_gps.setText("위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude + "\n고도 : " + altitude + "\n정확도 : "  + accuracy);
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
    };

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                if(btn_acce.isChecked()) {
                    tv_acce.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
                    long currentTime = System.currentTimeMillis();
                    long gabOfTime = (currentTime - lastTime);
                    if (gabOfTime > 100) {
                        lastTime = currentTime;
                        speed = Math.abs(event.values[0] + event.values[1] + event.values[2] - lastX - lastY - lastZ) / gabOfTime * 10000;
                        if(speed > 800 ){
                            count ++;
                            tv_count.setText(count);
                        }
                        lastX = event.values[0];
                        lastY = event.values[1];
                        lastZ = event.values[2];
                    }
                }else{
                    tv_acce.setText("가속도 위치");
                }
                break;
            case Sensor.TYPE_GYROSCOPE:
                if(btn_gyro.isChecked()) {
                    double gyroX = event.values[0];
                    double gyroY = event.values[1];
                    double gyroZ = event.values[2];

                    dt = (event.timestamp - timestamp) * NS2S;
                    timestamp = event.timestamp;

                    if(dt - timestamp*NS2S !=0){
                        pitch = pitch + gyroY*dt;
                        roll = roll + gyroX*dt;
                        yaw = yaw + gyroZ*dt;
                    }
                    tv_gyro.setText("pitch: " + pitch + "\nroll: " + roll + "\nyaw: " + yaw);
                }else{
                    tv_gyro.setText("자이로 위치");
                }
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if(btn_magn.isChecked()) {
                    tv_magn.setText("magnX: " + event.values[0] + "\nmagnY: " + event.values[1] + "\nmagnZ: " + event.values[2]);
                }else{
                    tv_magn.setText("자기장 위치");
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
