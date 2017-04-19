package com.example.zeroliam.helloandroid;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //Layout elements here
    Button btn;
    EditText geturl;
    TextView displayurl, showResponse;
    String getTheResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the elements from the XML
        btn = (Button) findViewById(R.id.reqBtn);
        geturl = (EditText) findViewById(R.id.reqURL);
        displayurl = (TextView) findViewById(R.id.displayHTML);
        showResponse = (TextView) findViewById(R.id.showResponseCode);


        //Setting the click listener
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //Call the site and display it
                displayURL(geturl.getText().toString(), displayurl, showResponse);
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
        Context context = getApplicationContext();
        CharSequence text ="";
        int duration = Toast.LENGTH_LONG;

//        //Before we start bothering, let's see if the device is connected
//        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
//
//        if(!isConnected){
//            return theStream;
//        }

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

            //get response message
            getTheResponse = "Server response: " + httpcon.getResponseCode() + " " + httpcon.getResponseMessage();

            //get our data
            theStream = httpcon.getInputStream();

        }catch (MalformedURLException mal){
            Log.e("HelloAndroid", "MALFORMED URL EXCEPTION");
            Log.e("MalformedURLException: ", mal.toString());

            text = "That URL is not good, try again :)";
            MainActivity.super.runOnUiThread(new workingToast(context, text, duration));

        }catch (IOException ioe){
            Log.e("HelloAndroid", "IO EXCEPTION");
            Log.e("IOException stack: ", ioe.toString());

            text = "Hey!! That host seems wrong, try again :)";
            MainActivity.super.runOnUiThread(new workingToast(context, text, duration));
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
    public void displayURL(String theurl, final TextView displayurl, final TextView showResponse){
        final String finalurl = theurl;
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_LONG;


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

                    //Pass the result to the thread for the UI on the Main activity
                    //instead of trying to pass it to the other thread.
                    MainActivity.super.runOnUiThread(new workingTextView(showResponse));

                }catch (Exception evt){
                   Log.e("Stream no bueno! ", evt.toString());
                    CharSequence text = "The source stream is not good, the host or URL might be wrong. Please try again :)";

                    MainActivity.super.runOnUiThread(new workingToast(context, text, duration));
                }
            }
        }).start();

    }

    //RUNNABLE IS A INTERFACE!! YAAY!
    //source: https://developer.android.com/reference/java/lang/Runnable.html

    public class workingTextView implements Runnable {
        //Make global vars for this class
        TextView txtView;

        //Make the constructor
        workingTextView(TextView newTxtView){
            txtView = newTxtView;
        }

        @Override
        public void run() {
            if(getTheResponse != null){
                showResponse.setText(getTheResponse);
            }else{
                showResponse.setText("No response. Try again with another URL.");
            }
        }
    }

    public class workingToast implements Runnable {
        //Make the global vars for this class
        Context context;
        CharSequence text;
        int duration;

        //Make the constructor
        workingToast(Context newContext, CharSequence newText, int newDuration){
            context = newContext;
            text = newText;
            duration = newDuration;
        }

        @Override
        public void run() {
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

}
