package id.sch.moodlequizsmk.browsermoodleapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.Result;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Delayed;

import id.sch.smkn1banjarbaru.browsermoodleapp.R;

public class ScannerActivity extends AppCompatActivity {

    //public static MainActivity instance = null;
    private SharedPreferences.Editor sfEditor;
    private SharedPreferences sharedPreferences;

    private CodeScanner mCodeScanner;
    private FloatingActionButton btnCameraSwitch;
    private int hadap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scanner_activity);
        if (ContextCompat.checkSelfPermission(ScannerActivity.this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(ScannerActivity.this, new String[] {Manifest.permission.CAMERA}, 123);
        } else {

            startScanning();
            if (android.os.Build.VERSION.SDK_INT > 9)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }

            btnCameraSwitch = findViewById(R.id.fab_switch_camera);
            btnCameraSwitch.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (hadap == CodeScanner.CAMERA_BACK){
                        hadap = CodeScanner.CAMERA_FRONT;
                    }else{
                        hadap = CodeScanner.CAMERA_BACK;
                    }
                    mCodeScanner.setCamera(hadap);
                }
            });
        }
    }

    private void startScanning() {
        CodeScannerView scannerView = findViewById(R.id.previewView);
        mCodeScanner = new CodeScanner(this, scannerView);
        hadap = CodeScanner.CAMERA_BACK;
        mCodeScanner.setCamera(hadap);
        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Integer hasil = urlCheck(result.getText());
                        if (hasil==200){
                            mCodeScanner.stopPreview();
                            mCodeScanner.releaseResources();
                            checkOverlayPermission();
                            startService(result.getText());
                        }else {
                            Toast.makeText(ScannerActivity.this, "Bukan QR yang tepat, silahkan scan ulang", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                startScanning();
            } else {
                Toast.makeText(this, "Aplikasi tidak bisa jalan karena akses kamera ditolak", Toast.LENGTH_LONG).show();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 2s
                        System.exit(0);
                    }
                }, 2000);
            }
        }
    }

    protected Integer urlCheck(String url){
        Integer code=0;
        try {
            URL u = new URL ( url);
            HttpURLConnection huc =  (HttpURLConnection) u.openConnection();
            huc.setRequestMethod("GET");
            HttpURLConnection.setFollowRedirects(true);
            huc.connect();
            code = huc.getResponseCode();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code;
    }


    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    // method for starting the service
    public void startService(String url){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check if the user has already granted
            // the Draw over other apps permission
            if(Settings.canDrawOverlays(this)) {
                // start the service based on the android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent serviceIntent = new Intent(this,ForegroundService.class);
                    serviceIntent.putExtra("url", url);
                    startForegroundService(serviceIntent);
                } else {
                    Intent serviceIntent = new Intent(this,ForegroundService.class);
                    serviceIntent.putExtra("url", url);
                    startService(serviceIntent);
                }
            }
        }else{
            Intent serviceIntent = new Intent(this,ForegroundService.class);
            serviceIntent.putExtra("url", url);
            startService(serviceIntent);
        }
    }

    public void offService(){
        stopService(new Intent(this, ForegroundService.class));
    }

    // method to ask user to grant the Overlay permission
    public void checkOverlayPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                // send user to the device settings
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        }
    }

    // check for permission again when user grants it from
    // the device settings, and start the service
    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
        //startService();
    }
}