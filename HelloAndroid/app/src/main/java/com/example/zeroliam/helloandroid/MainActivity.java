package com.example.zeroliam.helloandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {
    //Layout elements here
    Button btn;
    EditText geturl;
    TextView displayurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the elements from the XML
        btn = (Button) findViewById(R.id.reqBtn);
        geturl = (EditText) findViewById(R.id.reqURL);
        displayurl = (TextView) findViewById(R.id.displayHTML);

        //Setting the click listener
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //Setting the URL & HTTPS requests
                String urltxt = geturl.getText().toString();
                Log.e("HelloAndroid", urltxt);
                try{
                    URL theurl = new URL (urltxt);
                    URLConnection urlcon = theurl.openConnection();
                    Object responseCode = 0;
                    urlcon.connect();
                    Log.e("HelloAndroid", "ON THE TRY SIDE");
                    responseCode = urlcon.getRequestProperties();

                    Log.e("HelloAndroid", responseCode.toString());
//                    System.out.println(responseCode.toString());

                }catch (IOException ioe){
                    Log.e("HelloAndroid", "CALLING STACK TRACE");
                    ioe.printStackTrace();
                }
            }
        });


    }
}
