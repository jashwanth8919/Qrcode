package com.example.resoluteai;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.encoder.QRCode;

import java.security.Permissions;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class MainActivity2 extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static final int Request_camera=1;
    private static int cam = Camera.CameraInfo.CAMERA_FACING_BACK;
    private FirebaseFirestore data;
    String rawresult;

    ZXingScannerView scannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         scannerView = new ZXingScannerView(this);

        setContentView(scannerView);
        int current= Build.VERSION.SDK_INT;
        if(current>= Build.VERSION_CODES.M)
        {
             if(checkpermission()){
                 Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
             }
             else {
                 request();
             }

        }


    }
    private boolean checkpermission(){
        return(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED);
    }
    private void request() {
       ActivityCompat.requestPermissions(MainActivity2.this,new String[]{Manifest.permission.CAMERA},Request_camera);
    }
    public void oncameraresult(int requestcode,String Permissions[] , int[]result)
    {
        switch ((requestcode)) {
            case Request_camera:
                if (result.length > 0) {
                    boolean cameraAccept = result[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccept) {
                        Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show();
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            oncancel("you should give permission",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(new String[]{Manifest.permission.CAMERA}, Request_camera);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }
        }
        @Override
        public void onResume(){
        super.onResume();
        int currentapiver=Build.VERSION.SDK_INT;
        if(currentapiver>=Build.VERSION_CODES.M){
            if(checkpermission())
            {
                if(scannerView ==null){
                    scannerView=new ZXingScannerView(this);
                    setContentView(scannerView);
                }
            }
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }

        }
        @Override
        public void onDestroy(){
        super.onDestroy();
        scannerView.stopCamera();;
        scannerView=null;
        }

        private void oncancel(String message,DialogInterface.OnClickListener oklistener)
        {
            new AlertDialog.Builder(MainActivity2.this)
                    .setMessage(message)
                    .setPositiveButton("ok",oklistener)
                    .setNegativeButton("cancel",null)
                    .create()
                    .show();
        }



    @Override
    public void handleResult(Result result) {
        final String rawresult = result.getText();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("scan result");
        builder.setPositiveButton("Store Data", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity2.this, "started", Toast.LENGTH_SHORT).show();
                data=FirebaseFirestore.getInstance();

                Map<String, Object> user=new HashMap<>();
                user.put("The Decoded data",rawresult);
                data.collection("user").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(MainActivity2.this, "It is stored", Toast.LENGTH_SHORT).show();


                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity2.this, "Scan again ", Toast.LENGTH_SHORT).show();
                                scannerView.resumeCameraPreview(MainActivity2.this);
                            }
                        });




            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scannerView.resumeCameraPreview(MainActivity2.this);
            }
        });
        builder.setMessage(rawresult);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }
}