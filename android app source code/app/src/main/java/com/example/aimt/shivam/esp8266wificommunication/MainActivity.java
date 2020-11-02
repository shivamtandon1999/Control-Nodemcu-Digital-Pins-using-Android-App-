package com.example.aimt.shivam.esp8266wificommunication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String ipAddress, ssid;
    TextView info;
    Button S1_on, S1_off, S2_on, S2_off, S3_on, S3_off, S4_on, S4_off, S5_on, S5_off, S6_on, S6_off, S7_on, S7_off, S8_on, S8_off;
    int sw_val;
    String check_status = "";
    final Context context = this;
    EditText sw_1, sw_2, sw_3, sw_4, sw_5, sw_6, sw_7, sw_8;
    boolean is_esp_conn;

    private SharedPreferences savedFields;

    protected static final String TAG = "LocationOnOff";


    private GoogleApiClient googleApiClient;
    final static int REQUEST_LOCATION = 199;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipAddress = Wifi_Info(); // we get SSID info by same function and is stored in global variable named "ssid"
        info = findViewById(R.id.info);
        info.setText("SSID: " + ssid);
        savedFields = getSharedPreferences("info", MODE_PRIVATE); //using save preferences for storing switch name if user changes it

        //initializing all "buttons"

        S1_on = findViewById(R.id.SWITCH_1_ON);
        S2_on = findViewById(R.id.SWITCH_2_ON);
        S3_on = findViewById(R.id.SWITCH_3_ON);
        S4_on = findViewById(R.id.SWITCH_4_ON);
        S5_on = findViewById(R.id.SWITCH_5_ON);
        S6_on = findViewById(R.id.SWITCH_6_ON);
        S7_on = findViewById(R.id.SWITCH_7_ON);
        S8_on = findViewById(R.id.SWITCH_8_ON);
        S1_off = findViewById(R.id.SWITCH_1_OFF);
        S2_off = findViewById(R.id.SWITCH_2_OFF);
        S3_off = findViewById(R.id.SWITCH_3_OFF);
        S4_off = findViewById(R.id.SWITCH_4_OFF);
        S5_off = findViewById(R.id.SWITCH_5_OFF);
        S6_off = findViewById(R.id.SWITCH_6_OFF);
        S7_off = findViewById(R.id.SWITCH_7_OFF);
        S8_off = findViewById(R.id.SWITCH_8_OFF);

        //setting up default text for every "switch"

        sw_1 = findViewById(R.id.sw1);
        sw_1.setText(savedFields.getString("1", "SWITCH 1"));
        sw_2 = findViewById(R.id.sw2);
        sw_2.setText(savedFields.getString("2", "SWITCH 2"));
        sw_3 = findViewById(R.id.sw3);
        sw_3.setText(savedFields.getString("3", "SWITCH 3"));
        sw_4 = findViewById(R.id.sw4);
        sw_4.setText(savedFields.getString("4", "SWITCH 4"));
        sw_5 = findViewById(R.id.sw5);
        sw_5.setText(savedFields.getString("5", "SWITCH 5"));
        sw_6 = findViewById(R.id.sw6);
        sw_6.setText(savedFields.getString("6", "SWITCH 6"));
        sw_7 = findViewById(R.id.sw7);
        sw_7.setText(savedFields.getString("7", "SWITCH 7"));
        sw_8 = findViewById(R.id.sw8);
        sw_8.setText(savedFields.getString("8", "SWITCH 8"));

        setSwitchStatus();


        // Todo Location Already on  ... start
        final LocationManager manager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);

        // Todo Location Already on  ... end

        if (!hasGPSDevice(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "Gps not Supported", Toast.LENGTH_SHORT).show();
        }

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasGPSDevice(MainActivity.this)) {
            Log.e("TAG", "Gps already enabled");
            Toast.makeText(MainActivity.this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            enableLoc();
        }


        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);         //requesting permissions from user using run-time dialog box


    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;
        final List<String> providers = mgr.getAllProviders();
        if (providers == null)
            return false;
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }

        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("onActivityResult()", Integer.toString(resultCode));

        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode)
        {
            case REQUEST_LOCATION:
                switch (resultCode)
                {
                    case Activity.RESULT_OK:
                    {
                        // All required changes were successfully made
                        Wifi_Info();
                        info.setText("SSID: " + ssid);
                        Toast.makeText(MainActivity.this, ssid, Toast.LENGTH_LONG).show();
                        break;
                    }
                    case Activity.RESULT_CANCELED:
                    {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(MainActivity.this, "Location not enabled, user cancelled.", Toast.LENGTH_LONG).show();
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],@NonNull int[] grantResults) //extending permission request function
    {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Oops !!!");
                    builder.setMessage("One or more permission(s) required by the app was denied.\nGrant the required permission to continue.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MainActivity.this.finish();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


            //editing switch name//
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.edtb1)
            editButtonText(sw_1,"1");
        if (item.getItemId() == R.id.edtb2)
            editButtonText(sw_2,"2");
        if (item.getItemId() == R.id.edtb3)
            editButtonText(sw_3,"3");
        if (item.getItemId() == R.id.edtb4)
            editButtonText(sw_4,"4");
        if (item.getItemId() == R.id.edtb5)
            editButtonText(sw_5,"5");
        if (item.getItemId() == R.id.edtb6)
            editButtonText(sw_6,"6");
        if (item.getItemId() == R.id.edtb7)
            editButtonText(sw_7,"7");
        if (item.getItemId() == R.id.edtb8)
            editButtonText(sw_8,"8");
        return super.onOptionsItemSelected(item);
    }

    void changeSwitchState(String url)
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VOLLY ERROR",error.toString());
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }



    // On button click these methods executes
    public void Switch_1_click(View view) {

        String SwitchStatus;
        sw_val = 1;
        if(true)
        {
            if (!is_esp_conn) // calling function to check connectivity to ESP on every button press
                esp_not_conn();



            if (ipAddress.equals(""))
                Toast.makeText(MainActivity.this, "Invalid IP detected\nKindly check connection...", Toast.LENGTH_LONG).show();
        }
         {
            if (view == S1_on) {
                SwitchStatus = "1";
                S1_on.setBackgroundColor(Color.GREEN);
                S1_off.setBackgroundColor(Color.LTGRAY);
            } else {
                SwitchStatus = "0";
                S1_off.setBackgroundColor(Color.RED);
                S1_on.setBackgroundColor(Color.LTGRAY);
            }

            //Connect to default port number. Ex: http://IpAddress:80
             String url ="http://" + ipAddress + ":" + "80" + "/sw" + sw_val + "/" +SwitchStatus;
             changeSwitchState(url);


        }
    }

    public void Switch_2_click(View view) {
        String SwitchStatus;
        sw_val = 2;
        if(true)
        {
            if (!is_esp_conn) // calling function to check connectivity to ESP on every button press
                esp_not_conn();



            if (ipAddress.equals(""))
                Toast.makeText(MainActivity.this, "Invalid IP detected\nKindly check connection...", Toast.LENGTH_LONG).show();
        }
         {
            if (view == S2_on) {
                SwitchStatus = "1";
                S2_on.setBackgroundColor(Color.GREEN);
                S2_off.setBackgroundColor(Color.LTGRAY);
            } else {
                SwitchStatus = "0";
                S2_off.setBackgroundColor(Color.RED);
                S2_on.setBackgroundColor(Color.LTGRAY);
            }

             String url ="http://" + ipAddress + ":" + "80" + "/sw" + sw_val + "/" +SwitchStatus;
             changeSwitchState(url);
        }
    }

    public void Switch_3_click(View view) {
        String SwitchStatus;
        sw_val = 3;
        if(true)
        {
            if (!is_esp_conn) // calling function to check connectivity to ESP on every button press
                esp_not_conn();



            if (ipAddress.equals(""))
                Toast.makeText(MainActivity.this, "Invalid IP detected\nKindly check connection...", Toast.LENGTH_LONG).show();
        }
         {
            if (view == S3_on) {
                SwitchStatus = "1";
                S3_on.setBackgroundColor(Color.GREEN);
                S3_off.setBackgroundColor(Color.LTGRAY);
            } else {
                SwitchStatus = "0";
                S3_off.setBackgroundColor(Color.RED);
                S3_on.setBackgroundColor(Color.LTGRAY);
            }

             String url ="http://" + ipAddress + ":" + "80" + "/sw" + sw_val + "/" +SwitchStatus;
             changeSwitchState(url);
        }
    }

    public void Switch_4_click(View view) {
        String SwitchStatus;
        sw_val = 4;
        if(true)
        {
            if (!is_esp_conn) // calling function to check connectivity to ESP on every button press
                esp_not_conn();



            if (ipAddress.equals(""))
                Toast.makeText(MainActivity.this, "Invalid IP detected\nKindly check connection...", Toast.LENGTH_LONG).show();
        }
         {
            if (view == S4_on) {
                SwitchStatus = "1";
                S4_on.setBackgroundColor(Color.GREEN);
                S4_off.setBackgroundColor(Color.LTGRAY);
            } else {
                SwitchStatus = "0";
                S4_off.setBackgroundColor(Color.RED);
                S4_on.setBackgroundColor(Color.LTGRAY);
            }

            //Connect to default port number. Ex: http://IpAddress:80
            //String serverAdress = ipAddress + ":" + "80";
             String url ="http://" + ipAddress + ":" + "80" + "/sw" + sw_val + "/" +SwitchStatus;
             changeSwitchState(url);
        }
    }

    public void Switch_5_click(View view) {
        String SwitchStatus;
        sw_val = 5;
        if(true)
        {
            if (!is_esp_conn) // calling function to check connectivity to ESP on every button press
                esp_not_conn();



            if (ipAddress.equals(""))
                Toast.makeText(MainActivity.this, "Invalid IP detected\nKindly check connection...", Toast.LENGTH_LONG).show();
        }
         {
            if (view == S5_on) {
                SwitchStatus = "1";
                S5_on.setBackgroundColor(Color.GREEN);
                S5_off.setBackgroundColor(Color.LTGRAY);
            } else {
                SwitchStatus = "0";
                S5_off.setBackgroundColor(Color.RED);
                S5_on.setBackgroundColor(Color.LTGRAY);
            }

            //Connect to default port number. Ex: http://IpAddress:80
            //String serverAdress = ipAddress + ":" + "80";
             String url ="http://" + ipAddress + ":" + "80" + "/sw" + sw_val + "/" +SwitchStatus;
             changeSwitchState(url);
        }
    }

    public void Switch_6_click(View view) {
        String SwitchStatus;
        sw_val = 6;
        if(true)
        {
            if (!is_esp_conn) // calling function to check connectivity to ESP on every button press
                esp_not_conn();



            if (ipAddress.equals(""))
                Toast.makeText(MainActivity.this, "Invalid IP detected\nKindly check connection...", Toast.LENGTH_LONG).show();
        }
         {
            if (view == S6_on) {
                SwitchStatus = "1";
                S6_on.setBackgroundColor(Color.GREEN);
                S6_off.setBackgroundColor(Color.LTGRAY);
            } else {
                SwitchStatus = "0";
                S6_off.setBackgroundColor(Color.RED);
                S6_on.setBackgroundColor(Color.LTGRAY);
            }

            //Connect to default port number. Ex: http://IpAddress:80
             String url ="http://" + ipAddress + ":" + "80" + "/sw" + sw_val + "/" +SwitchStatus;
             changeSwitchState(url);
        }
    }

    public void Switch_7_click(View view) {
        String SwitchStatus;
        sw_val = 7;
        if(true)
        {
            if (!is_esp_conn) // calling function to check connectivity to ESP on every button press
                esp_not_conn();



            if (ipAddress.equals(""))
                Toast.makeText(MainActivity.this, "Invalid IP detected\nKindly check connection...", Toast.LENGTH_LONG).show();
        }
         {
            if (view == S7_on) {
                SwitchStatus = "1";
                S7_on.setBackgroundColor(Color.GREEN);
                S7_off.setBackgroundColor(Color.LTGRAY);
            } else {
                SwitchStatus = "0";
                S7_off.setBackgroundColor(Color.RED);
                S7_on.setBackgroundColor(Color.LTGRAY);
            }

            //Connect to default port number. Ex: http://IpAddress:80
             String url ="http://" + ipAddress + ":" + "80" + "/sw" + sw_val + "/" +SwitchStatus;
             changeSwitchState(url);
        }
    }

    public void Switch_8_click(View view) {
        String SwitchStatus;
        sw_val = 8;
        if(true)
        {
            if (!is_esp_conn) // calling function to check connectivity to ESP on every button press
                esp_not_conn();

            if (ipAddress.equals(""))
                Toast.makeText(MainActivity.this, "Invalid IP detected\nKindly check connection...", Toast.LENGTH_LONG).show();
        }
         {
            if (view == S8_on) {
                SwitchStatus = "1";
                S8_on.setBackgroundColor(Color.GREEN);
                S8_off.setBackgroundColor(Color.LTGRAY);
            } else {
                SwitchStatus = "0";
                S8_off.setBackgroundColor(Color.RED);
                S8_on.setBackgroundColor(Color.LTGRAY);
            }

            //Connect to default port number. Ex: http://IpAddress:80
             String url ="http://" + ipAddress + ":" + "80" + "/sw" + sw_val + "/" +SwitchStatus;
             changeSwitchState(url);
        }
    }


    public String Wifi_Info() {

        WifiManager wifiManager = (WifiManager) super.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiinfo = wifiManager.getConnectionInfo();
        ssid = wifiinfo.getSSID();
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        int ip = dhcp.gateway;
        String my_address = formatIP(ip);

        return my_address;
    }

    // calculating IP in right format
    @SuppressLint("DefaultLocale")
    private String formatIP(int ip) {
        return String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff)
        );
    }



    public void esp_not_conn() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Connection !!!");
        builder.setMessage("Lost Connection.\nCannot Communicate with the device.");
        builder.setCancelable(false);
        builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                MainActivity.this.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void editButtonText(final EditText edit_btn_val, final String sw_no) //this button changes switch name
    {

        final SharedPreferences.Editor preferencesEditor = savedFields.edit();

        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                edit_btn_val.clearComposingText();
                                edit_btn_val.setText(userInput.getText());
                                preferencesEditor.putString(sw_no,edit_btn_val.getText().toString());
                                preferencesEditor.commit();

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    void setSwitchStatus()
    {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://192.168.4.1:80/status";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.length() !=8)
                            is_esp_conn = false;
                        else
                            is_esp_conn = true;

                        if (response.charAt(0) == '1') {
                            S1_on.setBackgroundColor(Color.GREEN);
                            S1_off.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(0) == '0') {
                            S1_off.setBackgroundColor(Color.RED);
                            S1_on.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(1) == '1') {
                            S2_on.setBackgroundColor(Color.GREEN);
                            S2_off.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(1) == '0') {
                            S2_off.setBackgroundColor(Color.RED);
                            S2_on.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(2) == '1') {
                            S3_on.setBackgroundColor(Color.GREEN);
                            S3_off.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(2) == '0') {
                            S3_off.setBackgroundColor(Color.RED);
                            S3_on.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(3) == '1') {
                            S4_on.setBackgroundColor(Color.GREEN);
                            S4_off.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(3) == '0') {
                            S4_off.setBackgroundColor(Color.RED);
                            S4_on.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(4) == '1') {
                            S5_on.setBackgroundColor(Color.GREEN);
                            S5_off.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(4) == '0') {
                            S5_off.setBackgroundColor(Color.RED);
                            S5_on.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(5) == '1') {
                            S6_on.setBackgroundColor(Color.GREEN);
                            S6_off.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(5) == '0') {
                            S6_off.setBackgroundColor(Color.RED);
                            S6_on.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(6) == '1') {
                            S7_on.setBackgroundColor(Color.GREEN);
                            S7_off.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(6) == '0') {
                            S7_off.setBackgroundColor(Color.RED);
                            S7_on.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(7) == '1') {
                            S8_on.setBackgroundColor(Color.GREEN);
                            S8_off.setBackgroundColor(Color.LTGRAY);
                        }
                        if (response.charAt(7) == '0') {
                            S8_off.setBackgroundColor(Color.RED);
                            S8_on.setBackgroundColor(Color.LTGRAY);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("VOLLY ERROR",error.toString());
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}
