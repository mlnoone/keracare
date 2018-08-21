package com.mcareapps.keracare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    WebView webView;
    ProgressBar progressBar;


    GeolocationPermissions.Callback mGeolocationCallback;
    String mGeolocationOrigin;

    int PERMISSION_REQUEST = 101;

    private final static String htmlOpen = "<html><head><meta charset='utf-8'/><meta name='viewport' content='width=device-width, initial-scale=1.0'/>";
    protected final static String mime = "text/html";
    protected final static String charset = "utf-8";
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webView);


        progressBar = findViewById(R.id.progressBar);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                MainActivity.this.setTitle(getString(R.string.app_name)+": Loading...");
                progressBar.setProgress(newProgress);

            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {

                mGeolocationOrigin  = null;
                mGeolocationCallback = null;

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    mGeolocationOrigin = origin;
                    mGeolocationCallback = callback;
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                        new AlertDialog.Builder(MainActivity.this).setMessage("Permission is required to access the device location")
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
                                    }
                                }).show();
                    } else {

                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
                    }
                 } else {
                    callback.invoke(origin, true, false);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {

                String msg = getString(R.string.io);
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();

                progressBar.setVisibility(View.GONE);
                MainActivity.this.setTitle(R.string.app_name);

                webView.loadDataWithBaseURL(null, htmlOpen+"</head><body><h3 style='color:red'>"+msg+"<br>"+description+"</h3></body></html>", mime, charset,
                        null);
                webView.clearHistory();

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if ( (url.contains("market") || url.contains("store") || url.contains("apk") || url.contains("map") || url.contains("tel:"))) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(intent);
                    clearProgress();
                    setTitle(view);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                clearProgress();
                setTitle(view);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);

        Intent myIntent = getIntent();
        if (Intent.ACTION_VIEW.equals(myIntent.getAction()) && myIntent.getData() != null) {
            Uri link = myIntent.getData();
            webView.loadUrl(link.toString());
        } else {
            webView.loadUrl("https://kera.care");
        }
    }

    private void clearProgress() {
        progressBar.setProgress(100);
        progressBar.setVisibility(View.GONE);
    }

    private void setTitle(WebView view) {
        String title = view.getTitle();
        String at = title.equalsIgnoreCase("Kera.Care") ? "Home" : title;
        MainActivity.this.setTitle(getString(R.string.app_name)+ ": " + at);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mGeolocationCallback != null) {
                    mGeolocationCallback.invoke(mGeolocationOrigin, true, true);
                }
            }else {
                if (mGeolocationCallback != null) {
                    mGeolocationCallback.invoke(mGeolocationOrigin, false, false);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())  {
            case R.id.info :
                AlertDialog.Builder b = new AlertDialog.Builder(this).setTitle(R.string.app_name)
                        .setMessage("Version: "+BuildConfig.VERSION_NAME+"\n\nMCare Apps, HODO Informatics & Compassionate Kerala")
                        .setPositiveButton(R.string.ok, null);
                AlertDialog d = b.create();
                d.show();
                return  true;
        }
        return  super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
