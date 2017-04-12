package com.example.zeroliam.helloandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

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

        btn.setOnClickListener(new View.OnClickListener() {
            try{
                URL theUrl = new URL(geturl.getText());
            }catch(MalformedURLException exc){

            }

        });
    }
}
