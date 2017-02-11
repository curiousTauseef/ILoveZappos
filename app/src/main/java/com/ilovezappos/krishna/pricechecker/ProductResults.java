package com.ilovezappos.krishna.pricechecker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class ProductResults extends AppCompatActivity {

    private static final String TAG = "ProductResultsActivity";

    String AUTH_KEY_ZAPPOS;
    String URL_PROTOCOL_ZAPPOS;
    String URL_AUTHORITY_ZAPPOS;
    String URL_PATH;
    String URL_QUERY_PARAMETER_TERM;
    String URL_QUERY_PARAMETER_KEY;
    String URL_QUERY_PARAMETER_LIMIT;
    String URL_QUERY_PARAMETER_LIMIT_VALUE;
    RecyclerView recyclerView;
    RecyclerViewAdapter adapter;
    ArrayList<Product> products = new ArrayList<Product>();
    ProgressBar progressBar;
    boolean foundResults = true;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_results);
        Log.v(TAG,"Product Results Activity is triggered");
        try {
            ActivityInfo ai = getPackageManager()
                    .getActivityInfo(this.getComponentName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            AUTH_KEY_ZAPPOS = bundle.getString("AUTH_KEY_ZAPPOS");
            URL_PROTOCOL_ZAPPOS = bundle.getString("URL_PROTOCOL_ZAPPOS");
            URL_AUTHORITY_ZAPPOS = bundle.getString("URL_AUTHORITY_ZAPPOS");
            URL_PATH = bundle.getString("URL_PATH");
            URL_QUERY_PARAMETER_TERM = bundle.getString("URL_QUERY_PARAMETER_TERM");
            URL_QUERY_PARAMETER_KEY = bundle.getString("URL_QUERY_PARAMETER_KEY");
            URL_QUERY_PARAMETER_LIMIT = bundle.getString("URL_QUERY_PARAMETER_LIMIT");
            URL_QUERY_PARAMETER_LIMIT_VALUE = bundle.getString("URL_QUERY_PARAMETER_LIMIT_VALUE");
            Log.v(TAG,"Fetching config values from manifest");
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            e.getLocalizedMessage();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        alertDialog = new AlertDialog.Builder(ProductResults.this).create();
        Button newButton = new Button(this);
        newButton.setText(getResources().getString(R.string.alert_ok_button_product_results_activity));
        newButton.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertDialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        alertDialog.setView(newButton);
        alertDialog.setTitle(getResources().getString(R.string.alert_title_product_results_activity));
        alertDialog.setMessage(getResources().getString(R.string.no_products_alert_product_results_activity));
        try {
            Log.v(TAG,"Checking for active internet connection");
            checkConnection();
        } catch (MalformedURLException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void checkConnection() throws MalformedURLException, ExecutionException, InterruptedException {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String searchTerm = extras.getString("SEARCH_TERM");
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Uri zapposUri = new Uri.Builder().scheme(URL_PROTOCOL_ZAPPOS).authority(URL_AUTHORITY_ZAPPOS).path(URL_PATH)
                    .appendQueryParameter(URL_QUERY_PARAMETER_TERM, searchTerm)
                    .appendQueryParameter(URL_QUERY_PARAMETER_KEY, AUTH_KEY_ZAPPOS)
                    .appendQueryParameter(URL_QUERY_PARAMETER_LIMIT, URL_QUERY_PARAMETER_LIMIT_VALUE)
                    .build();
            Log.v(TAG,"Making rest api call");
            new RestCallActivity().execute(zapposUri.toString());
        } else {
            Toast.makeText(getApplicationContext(), getResources().getString(
                    R.string.no_internet_toast), Toast.LENGTH_LONG).show();
            Intent intent2 = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent2);
        }
    }


    private class RestCallActivity extends AsyncTask<String, Void, JSONArray> {

        JSONObject jObj;
        JSONArray jArray;

        @Override
        protected void onPreExecute() {
            progressBar.setMax(10);
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            Log.v(TAG,"Querying zappos for the search term");
            try {
                jObj = zapposCall(params[0]);
                jArray = jObj.getJSONArray("results");
                if (jArray.length() == 0) {
                    foundResults = false;
                }
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jTempObj = jArray.getJSONObject(i);
                    String productName = jTempObj.getString("productName");
                    String brandName = jTempObj.getString("brandName");
                    String origPrice = jTempObj.getString("originalPrice");
                    String finalPrice = jTempObj.getString("price");
                    String discount = jTempObj.getString("percentOff");
                    String productId = jTempObj.getString("productId");
                    String imageUrl = jTempObj.getString("thumbnailImageUrl");
                    URL url = new URL(jTempObj.getString("thumbnailImageUrl"));
                    String styleId = jTempObj.getString("styleId");
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    String productUrl = jTempObj.getString("productUrl");
                    Product p = new Product(productName, brandName, origPrice, finalPrice, discount, bmp, productUrl, imageUrl, styleId, productId);
                    products.add(p);
                    publishProgress();
                }
                Log.v(TAG,"Added products to the arraylist");
            } catch (IOException | JSONException ex) {
                ex.getLocalizedMessage();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
            return jArray;
        }

        @Override
        protected void onPostExecute(JSONArray jArray) {
            if (!foundResults) {
                alertDialog.show();
            } else {
                progressBar.setVisibility(View.GONE);
                adapter = new RecyclerViewAdapter(ProductResults.this, products);
                recyclerView.setAdapter(adapter);
            }
        }


        private JSONObject zapposCall(String zUrl) throws IOException, JSONException {
            InputStream is = null;
            JSONObject jObj = null;
            Log.v(TAG,"Making the rest call to zappos");
            try {
                URL url = new URL(zUrl);
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.connect();
                int response = conn.getResponseCode();
                if (response != 200) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.rest_call_failed), Toast.LENGTH_LONG).show();
                } else {
                    is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder responseBuilder = new StringBuilder(2048);
                    String resStr;
                    while ((resStr = br.readLine()) != null) {
                        responseBuilder.append(resStr);
                    }
                    jObj = new JSONObject(responseBuilder.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                e.getLocalizedMessage();
                Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent3);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            return jObj;
        }
    }

}


