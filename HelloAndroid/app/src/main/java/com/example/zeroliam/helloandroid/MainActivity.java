package com.example.zeroliam.helloandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {
    //Layout elements here
    Button btn;
    TextView geturl;
    TextView displayurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the elements from the XML
        btn = (Button) findViewById(R.id.reqBtn);
        geturl = (TextView) findViewById(R.id.reqURL);
        displayurl = (TextView) findViewById(R.id.displayHTML);

        //Setting the click listener
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //Setting the URL & HTTPS requests

                try{
                    URL myurl = new URL("hsjafg");
                    URLConnection connection = myurl.openConnection();

                }catch(IOException e){
                    e.printStackTrace();
                    System.out.println("EXCEPTIONNN!!!");

                }

            }
        });


    }
}
