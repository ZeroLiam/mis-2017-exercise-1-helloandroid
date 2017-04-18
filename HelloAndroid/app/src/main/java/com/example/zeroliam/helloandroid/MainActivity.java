package com.example.zeroliam.helloandroid;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.*;
import java.net.*;
import java.security.Permission;
import java.util.Scanner;
import android.Manifest.permission;

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
                //Call the site and display it
                displayURL(geturl.getText().toString(), displayurl);
            }
        });
    }


    /**
     * Method name: connection
     * Modifier:    private
     * Purpose:     To connect to a URL, make a connection, grab the object and pass it
     *              to a InputStream that will be returned, ready to be used later.
     * Parameters:  String
     * Returns:     InputStream
     */
    private InputStream connection (String urls){
        //Initialize the stream that will return the html object
        InputStream theStream = null;

        //Setting the URL & HTTPS requests
        try{
            Log.e("HelloAndroid", "ON THE TRY SIDE");
            //Get the url
            URL theurl = new URL (urls);
            Log.e("THE URL IS: ", theurl.toString());
            //open an url connection
            URLConnection urlcon = theurl.openConnection();
            //pass the url connection to a httpurl connection
            HttpURLConnection httpcon = (HttpURLConnection) urlcon;
            Log.e("Permission? ", httpcon.getPermission().toString());

            //connect
            httpcon.connect();


            //get our data
            theStream = httpcon.getInputStream();

        }catch (MalformedURLException mal){
            Log.e("HelloAndroid", "MALFORMED URL EXCEPTION");
            Log.e("MalformedURLException: ", mal.toString());
        }catch (IOException ioe){
            Log.e("HelloAndroid", "IO EXCEPTION");
            Log.e("IOException stack: ", ioe.toString());
        }

        return theStream;
    }


    /**
     * Method name: displayURL
     * Modifier:    public
     * Purpose:     To to get any URL as an InputStream and display the object returned as
     *              an HTML in a TextView.
     * Parameters:  String, TextView source target
     * Returns:     void
     */
    public void displayURL(String theurl, final TextView displayurl){
        final String finalurl = theurl;


        //To avoid the fatal error network on main thread, we need to make a new thread
        //(source: Defining and starting a thread - https://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html)
        //which basically will open a new thread instead of making a new process or interfering with
        //the main thread, and will let the connection to run in the background while we modify our
        //visual or in early cases, do a Log.e and see what's happening.
        //This only happens in Honeycomb or higher versions (source: https://developer.android.com/reference/android/os/NetworkOnMainThreadException.html#NetworkOnMainThreadException );

        (new Thread(){
            public void run(){
                //Initialize the local InputStream and the Scanner to read the lines from the stream
                InputStream geturl = null;
                Scanner linereader;
                //Have a String to parse the HTML data
                String receiveHTML = "";

                //Call the input stream from the URL
                //(it needs to be called a final variable from within this inner class (IDE showed error)
                geturl = connection(finalurl);
//

                //Display the url site
                try{
                    //Read the lines from the input
                    linereader = new Scanner(geturl);
                    while(linereader.hasNext()){
                        receiveHTML += linereader.nextLine();
                    }

//                    Log.e("READING: ", receiveHTML);
                    displayurl.setText(Html.fromHtml(receiveHTML));

                }catch (Exception evt){
                    evt.printStackTrace();
                }
            }
        }).start();

    }

}
