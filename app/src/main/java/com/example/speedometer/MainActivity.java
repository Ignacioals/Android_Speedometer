package com.example.speedometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.Bundle;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;

import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import android.view.View;

import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;


//import com.google.android.gms.vision.CameraSource;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    LocationService myService;
    static boolean status;
    static boolean timerstarted = false;
    LocationManager locationManager;
    static TextView speed, movementAlert, seatbelt, passanger;
    Button start, pause, stop;
    static ImageView image, onMovimiento, onCinturon, onVelocidad, onOjos, onPasajero, report;
    static ProgressDialog locate;
    static int p = 0;
    static boolean speedLimit = false;
    SensorManager sensorMan;
    Sensor accelerometer;
    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;
    static boolean movementStatus = false;
    static AlertDialog alert11;
    static boolean eyeDetected = false;

    static Report reportClass = new Report();

//    CameraSource cameraSource;


    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            status = false;
        }
    };
    private boolean cameraStatus = false;

    void bindService() {
        if (status == true)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
    }

    void startCameraActivity() {
        if (cameraStatus)
            return;
        try {
            /*Intent backCameraIntent = new Intent(getApplicationContext(), BackCamService.class);
            startService(backCameraIntent);*/

            Intent intent = new Intent(getApplicationContext(), CamService.class);
            startService(intent);
        } catch (Exception e) {
            // This will catch any exception, because they are all descended from Exception
            System.out.println("Error " + e.getMessage());
        }
//        bindService(i, sc, BIND_AUTO_CREATE);
        cameraStatus = true;
    }

    void unbindService() {
        if (status == false)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        unbindService(sc);
        status = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorMan.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_UI);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMan.unregisterListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (status == true)
            unbindService();
    }

    @Override
    public void onBackPressed() {
        if (status == false)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            ;
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            if (mAccel > 6) {
                new CountDownTimer(5000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        onMovimiento.setVisibility(View.VISIBLE);
                        movementStatus = true;
                    }

                    public void onFinish() {
                        onMovimiento.setVisibility(View.INVISIBLE);
                    }
                }.start();


            }
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // required method
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        }
        else {
//            createCameraSource();
        }


        sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        speed = (TextView) findViewById(R.id.speedtext);
        movementAlert = (TextView) findViewById(R.id.movementtext);
        seatbelt = (TextView) findViewById(R.id.seatbelttext);
        passanger = (TextView) findViewById(R.id.seatbeltpasaj);

        start = (Button) findViewById(R.id.start);
        pause = (Button) findViewById(R.id.pause);
        stop = (Button) findViewById(R.id.stop);

        image = (ImageView) findViewById(R.id.image);
        onCinturon = (ImageView) findViewById(R.id.botonOncinturon);
        onVelocidad = (ImageView) findViewById(R.id.botonOnvelocidad);
        onOjos = (ImageView) findViewById(R.id.botonOnojos);
        onMovimiento = (ImageView) findViewById(R.id.botonOnmovimiento);
        onPasajero = (ImageView) findViewById(R.id.botonOnpasaj);
        report = (ImageView) findViewById(R.id.report);

        report.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), ReportActivity.class);
                startActivityForResult(myIntent, 0);
            }

        });



        speedAlert();
        termsConditions();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //The method below checks if Location is enabled on device or not. If not, then an alert dialog box appears with option
                //to enable gps.
                checkGps();
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    return;
                }


                if (status == false) {
                    //Here, the Location Service gets bound and the GPS Speedometer gets Active.
                    bindService();


                }
                locate = new ProgressDialog(MainActivity.this);
                locate.setIndeterminate(true);
                locate.setCancelable(false);
                locate.setMessage("Obteniendo Localizacion...");
                locate.show();
                start.setVisibility(View.GONE);
                pause.setVisibility(View.VISIBLE);
                pause.setText("Pause");
                stop.setVisibility(View.VISIBLE);

                if (!cameraStatus) {
                    startCameraActivity();
                }


            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pause.getText().toString().equalsIgnoreCase("pause")) {
                    pause.setText("Resume");
                    p = 1;

                } else if (pause.getText().toString().equalsIgnoreCase("Resume")) {
                    checkGps();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        //Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pause.setText("Pause");
                    p = 0;

                }
            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == true)
                    unbindService();
                start.setVisibility(View.VISIBLE);
                pause.setText("Pause");
                pause.setVisibility(View.GONE);
                stop.setVisibility(View.GONE);
                p = 0;
                speed.setText("....");
                onVelocidad.setVisibility(View.INVISIBLE);

            }
        });
    }

    static void startTimer(){
        if(timerstarted == false){
            new CountDownTimer(30000, 1000) {
                public void onTick(long millisUntilFinished) {
                    if(timerstarted == false) {
                        String date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
                        MainActivity.reportClass.addReport(date, MainActivity.speed.getText().toString());
                    }
                    timerstarted = true;
                }

                public void onFinish() {
                    timerstarted = false;
                }
            }.start();
        }
    }

    private void speedAlert(){
        AlertDialog.Builder builder1;
        builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Vas a mas de 20km/hr, estas conduciendo");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alert11 = builder1.create();


    }

    private void termsConditions(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(Html.fromHtml("Antes de usar la app debe leer los <a href=\"https://safedrive.htmlsave.net/\">Terminos y Conidiciones</a>"));
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "He leido y acepto",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
        // Make the textview clickable.
        ((TextView)alert11.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }


    //This method leads you to the alert dialog box.
    void checkGps() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


            showGPSDisabledAlertToUser();
        }
    }

    //This method configures the Alert Dialog box.
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS to use application")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

}

