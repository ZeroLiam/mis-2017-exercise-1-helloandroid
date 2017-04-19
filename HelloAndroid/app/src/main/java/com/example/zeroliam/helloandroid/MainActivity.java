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
        final int colornum;

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
            Log.i("THE URL IS: ", theurl.toString());
            //open an url connection
            URLConnection urlcon = theurl.openConnection();
            //pass the url connection to a httpurl connection
            HttpURLConnection httpcon = (HttpURLConnection) urlcon;
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
    public void displayURL(String theurl, final WebView displayurl, final TextView showResponse){
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

                //Display the url site
                try{
                    //Read the lines from the input
                    linereader = new Scanner(geturl);
                    while(linereader.hasNext()){
                        receiveHTML += linereader.nextLine();
                    }

                    //Pass the result to the thread for the UI on the Main activity
                    //instead of trying to pass it to the other thread.

                    //For the WebView
                    MainActivity.super.runOnUiThread(new workingWebView(displayurl, receiveHTML));

                    //For the response TextView
                    //Second parameter is zero, meaning that the default text color is black (see Runnable definition for workingTextView)
                    MainActivity.super.runOnUiThread(new workingTextView(showResponse, 0));

                }catch (Exception evt){
                    Log.e("Stream no bueno! ", evt.toString());
                    CharSequence text = "The source stream is not good, the host or URL might be wrong. Please try again :)";

                    MainActivity.super.runOnUiThread(new workingToast(context, text, duration));
                }
            }
        }).start();

    }

    //RUNNABLE IS A INTERFACE!! YAAY!
    //We can make our own version of Runnable and then call it whenever we need it, for
    //whatever component we want to update in the UI.
    //source: https://developer.android.com/reference/java/lang/Runnable.html

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
                showResponse.setText(getTheResponse);
            }else{
                showResponse.setText("No response. Try again with another URL.");
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
        String htmlStr;

        //Make the constructor
        workingWebView(WebView newWebView, String newHtmlStr){
            webView = newWebView;
            htmlStr = newHtmlStr;
        }

        @Override
        public void run() {
            webView.loadData(htmlStr, "text/html", null);
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
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

}
