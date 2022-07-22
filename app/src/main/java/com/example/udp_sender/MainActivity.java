package com.example.udp_sender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTextViewReplyFromServer;
    private TextView  mEditTextSendMessage;
    private int port;
    private EditText ipaddress,portt;
    String ip;
    public static final Integer RecordAudioRequestCode = 1;
    private ImageView iv_mic;
    Switch sw1,sw2,sw3;
    Boolean connectflag;

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;
    @SuppressLint("ClickableViewAccessibility")


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
         sw1 = (Switch) findViewById(R.id.switch1);
        sw2 = (Switch) findViewById(R.id.switch2);
        sw3 = (Switch) findViewById(R.id.switch3);
        //sw.setChecked(false);
        sw1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage("1on");
                } else {
                    sendMessage("1off");
                }
            }
        });
        sw2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage("2on");
                } else {
                    sendMessage("2off");
                }
            }
        });
        sw3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sendMessage("3on");
                } else {
                    sendMessage("3off");
                }
            }
        });
        iv_mic = findViewById(R.id.iv_mic);

        Button buttonconnect = (Button) findViewById(R.id.btn_connect);

        mEditTextSendMessage = findViewById(R.id.edt_send_message);
         ipaddress=findViewById(R.id.ipadress);
        portt=findViewById(R.id.port);


        buttonconnect.setOnClickListener( this);

        iv_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent
                        = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                        Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                }
                catch (Exception e) {
                    Toast
                            .makeText(MainActivity.this, " " + e.getMessage(),
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);
                mEditTextSendMessage.setText(
                        Objects.requireNonNull(result).get(0));
                String msg= String.valueOf(mEditTextSendMessage.getText());
                //Toast.makeText(this, "msg :"+msg, Toast.LENGTH_SHORT).show();
                if(msg.contains("turn on switch 1") || msg.contains("turn on switch one") ){
                    sw1.setChecked(true);
                }
               else if(msg.contains("turn off switch 1") || msg.contains("turn off switch one") ){
                    sw1.setChecked(false);
                }
               else if (msg.contains("turn on switch 2") || msg.contains("turn on switch to") ){
                    sw2.setChecked(true);
                }
                else if(msg.contains("turn off switch 2") || msg.contains("turn off switch to") ){
                    sw2.setChecked(false);
                }
                else if (msg.contains("turn on switch 3") || msg.contains("turn on switch three") ){
                    sw3.setChecked(true);
                }
                else if(msg.contains("turn off switch 3") || msg.contains("turn off switch three") ){
                    sw3.setChecked(false);
                }

            }
        }
    }
    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.btn_connect:
                 ip= String.valueOf(ipaddress.getText());
                String a=String.valueOf(portt.getText());
                if (a.length()>1) {
                    port = Integer.parseInt(a);
                    sendMessage("Connected");
connectflag=true;
                }
                break;
        }
    }

    private void sendMessage(final String message) {

        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {

            String stringData;

            @Override
            public void run() {

                DatagramSocket ds = null;
                try {
                    ds = new DatagramSocket();
                    // IP Address below is the IP address of that Device where server socket is opened.
                    InetAddress serverAddr = InetAddress.getByName(ip);
                    DatagramPacket dp;
                    dp = new DatagramPacket(message.getBytes(), message.length(), serverAddr, port);
                    ds.send(dp);

                    byte[] lMsg = new byte[1000];
                    dp = new DatagramPacket(lMsg, lMsg.length);
                    ds.receive(dp);
                    stringData = new String(lMsg, 0, dp.getLength());

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                       // String s = mTextViewReplyFromServer.getText().toString();
                        //  Toast.makeText(MainActivity.this, "R :"+stringData, Toast.LENGTH_SHORT).show();

                            if (stringData.trim().length() != 0 && connectflag==true) {
                                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                               // mTextViewReplyFromServer.setText(s + "\nFrom Server : " + stringData);
                                connectflag=false;
                            }


                    }
                });
            }
        });

        thread.start();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}