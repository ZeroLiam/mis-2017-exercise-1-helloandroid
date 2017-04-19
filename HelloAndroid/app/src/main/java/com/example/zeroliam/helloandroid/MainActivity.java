package com.example.zeroliam.helloandroid;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    //Layout elements here
    Button btn;
    EditText geturl;
    WebView displayurl;
    TextView showResponse;
    String getTheResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get the elements from the XML
        btn = (Button) findViewById(R.id.reqBtn);
        geturl = (EditText) findViewById(R.id.reqURL);
        displayurl = (WebView) findViewById(R.id.displayHTML);
        showResponse = (TextView) findViewById(R.id.showResponseCode);


        //Setting the click listener
        btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                //Get the url from the TextView, pass the displayurl WebView, and the showResponse TextView for the server responses
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
        //Elements for the Toasts
        Context context = getApplicationContext();
        CharSequence text ="";
        int duration = Toast.LENGTH_LONG;
        //An int to color code the response from the server
        final int colornum;

        //Before we start bothering, let's see if the device is connected, and if not then return the null Stream
        //(source: Android API https://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        //If it's not connected return the empty Stream anyway
        if(!isConnected){
            Log.e("Connection: ", "NOT CONNECTED!");
            MainActivity.super.runOnUiThread(new workingToast(context, "NOT CONNECTED! Check your WiFi or Data Connection", duration));
            return theStream;
        }

        //If it's connected, then let's get to work! :D
        //Setting the URL & HTTPS requests
        try{
            Log.e("HelloAndroid", "ON THE TRY SIDE");
            //Get the url
            URL theurl = new URL (urls);
            Log.i("THE URL IS: ", theurl.toString());
            //open an url connection
            URLConnection urlcon = theurl.openConnection();
            //pass the url connection to a httpurl connection
            HttpURLConnection httpcon = (HttpURLConnection) urlcon;
            //Check if our permission is ok
            Log.i("Permission? ", httpcon.getPermission().toString());

            //connect
            httpcon.connect();

            //get response message
            getTheResponse = "Server response: " + httpcon.getResponseCode() + " " + httpcon.getResponseMessage();

            //Change the color of the TextView depending on the response
            if(httpcon.getResponseCode()<200)
            {
                colornum=0xFF65F442;
            }
            else if(httpcon.getResponseCode()>=200 && httpcon.getResponseCode()<300)
            {
                colornum=0xFF0E0B72;
            }
            else if(httpcon.getResponseCode()>=300 && httpcon.getResponseCode()<400)
            {
                colornum=0xFFC741F4;
            }
            else if(httpcon.getResponseCode()>=400 && httpcon.getResponseCode()<500)
            {
                colornum=0xFFB70929;
            }
            else if(httpcon.getResponseCode()>=500 && httpcon.getResponseCode()<600)
            {
                colornum=0xFF681323;
            }
            else
            {
                colornum=0xFF899185;
            }

            //Pass the result to the thread for the UI on the Main activity
            //instead of trying to pass it to the other thread.
            //(Idea from the second answer from this stackOverflow thread http://stackoverflow.com/questions/5185015/updating-android-ui-using-threads)
            //This solution is implemented throughout the file for other uses, including Toast
            MainActivity.super.runOnUiThread(new workingTextView(showResponse, colornum));

            //get our data
            theStream = httpcon.getInputStream();

        }catch (MalformedURLException mal){
            //In case of a wrong URL input
            Log.e("HelloAndroid", "MALFORMED URL EXCEPTION");
            Log.e("MalformedURLException: ", mal.toString());

            text = "That URL is not good, try again :)";
            MainActivity.super.runOnUiThread(new workingToast(context, text, duration));

        }catch (IOException ioe){
            //In case our Stream is null (due to connectivity issues, bad URL, etc
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
    public void displayURL(String theurl, final WebView displayurl, final TextView showResponse){
        //Our url from the top TextView in our layout
        final String finalurl = theurl;
        //Variables for the Toast
        final Context context = getApplicationContext();
        final int duration = Toast.LENGTH_LONG;

        //HEY!!! We found an error, then I (Lis) debugged it -- Later I read that an AsyncTask could've done it easier but here we are #oops
        //To avoid the fatal error network on main thread, we need to make a new thread
        //(source: ORACLE API - Defining and starting a thread - https://docs.oracle.com/javase/tutorial/essential/concurrency/runthread.html)
        //which basically will open a new thread instead of making a new process or interfering with
        //the main thread, and will let the connection to run in the background while we modify our
        //visual or in early cases, do a Log.e and see what's happening.
        //This only happens in Honeycomb or higher versions (source: ANDROID API - https://developer.android.com/reference/android/os/NetworkOnMainThreadException.html#NetworkOnMainThreadException );

        (new Thread(){
            public void run() {
                //Initialize the local InputStream and the Scanner to read the lines from the stream
                InputStream geturl = null;
                Scanner linereader;
                //Have a String to parse the HTML data
                String receiveHTML = "";

                //Call the input stream from the URL
                //(it needs to be called a final variable from within this inner class (IDE showed error)
                geturl = connection(finalurl);

                //If our stream is fine, then let's get our HTML and more! :D
                if (geturl != null) {
                    //Display the url site
                    try {
                        //Read the lines from the input
                        linereader = new Scanner(geturl);
                        while (linereader.hasNext()) {
                            receiveHTML += linereader.nextLine();
                        }

                        //Pass the result to the thread for the UI on the Main activity
                        //instead of trying to pass it to the other thread:
                        //For the WebView
                        MainActivity.super.runOnUiThread(new workingWebView(displayurl, receiveHTML));

                        //For the response TextView
                        //Second parameter is zero, meaning that the default text color is black (see the Runnable definition I (Lis) made for workingTextView)
                        MainActivity.super.runOnUiThread(new workingTextView(showResponse, 0));

                    } catch (Exception evt) {
                        //Something wrong? Could be the Stream, just log it and send a Toast
                        Log.e("Stream not good! ", evt.toString());
                        CharSequence text = "The source (Stream) is not good. Check your url or connectivity and please try again :)";

                        MainActivity.super.runOnUiThread(new workingToast(context, text, duration));
                    }
                }
            }
        }).start();//Start our new Thread! :D

    }

    //RUNNABLE IS A INTERFACE!! YAAY! :'D
    //We can make our own version of Runnable and then call it whenever we need it, for
    //whatever component we want to update in the UI.
    //source: Idea from ANDROID API - https://developer.android.com/reference/java/lang/Runnable.html

    /**
     * Class name: workingTextView
     * Modifier:    public
     * Purpose:     To update a TextView on the UI from a change outside the main thread.
     * Constructor: With parameters - TextView source target, int
     */
    public class workingTextView implements Runnable {
        //Make global vars for this class
        TextView txtView;
        int colornum;

        //Make the constructor
        workingTextView(TextView newTxtView, int newColornum){
            txtView = newTxtView;
            //colornum is optional
            colornum = newColornum != 0 ? newColornum : 0xFF000000;
        }

        @Override
        public void run() {
            if(getTheResponse != null){
                txtView.setTextColor(colornum);
                txtView.setText(getTheResponse);
            }else{
                txtView.setText("No response. Try again with another URL.");
            }
        }
    }

    /**
     * Class name: workingWebView
     * Modifier:    public
     * Purpose:     To update a WebView on the UI from a HTML string.
     * Constructor: With parameters - WebView target source
     */
    public class workingWebView implements Runnable {
        //Make global vars for this class
        WebView webView;
        String responseStr;

        //Make the constructor
        workingWebView(WebView newWebView, String newResponseStr){
            webView = newWebView;
            responseStr = newResponseStr;
        }

        @Override
        public void run() {
            webView.loadDataWithBaseURL(null, responseStr,"text/html", "UTF-8", null);
        }
    }

    /**
     * Class name: workingToast
     * Modifier:    public
     * Purpose:     To make a Toast on the UI from an error or change outside the main thread.
     * Constructor: With parameters - Context, CharSequence, int
     */
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
            //Make the Toast with the values received as parameters
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

}
