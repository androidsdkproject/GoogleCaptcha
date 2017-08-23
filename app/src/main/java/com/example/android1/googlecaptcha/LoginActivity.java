package com.example.android1.googlecaptcha;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

import static android.widget.Toast.LENGTH_LONG;


public class LoginActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String PUBLIC_KEY = "6LcKFCUUAAAAACcT-6KmjZXZwnik5H59VjcZz5wg";
    private static final String PRIVATE_KEY = "6LcKFCUUAAAAAC50YId9tzKcoAYQ8FOGlxGSn--Z";
    private String TAG = "TAG";
    static String response;
    private GoogleApiClient mGoogleApiClient;
    boolean captcha_status = false;
    Button login;
    String final_res = "";
    int flag = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);


        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);
        this.findViewById(R.id.reload).setOnClickListener(this);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(LoginActivity.this)
                .addOnConnectionFailedListener(LoginActivity.this)
                .setAccountName("kamalverma1207@gmail.com")
                .setGravityForPopups(4)
                .build();

        mGoogleApiClient.connect();


    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.login:

                if (!captcha_status) {
                    shownCaptcha();
                }
                if (captcha_status) {
                    Intent in = new Intent(getApplicationContext(), Welcome.class);
                    startActivity(in);

                }


                break;

            case R.id.reload:
                shownCaptcha();
                break;
        }
    }


    void shownCaptcha() {


        SafetyNet.SafetyNetApi.verifyWithRecaptcha(mGoogleApiClient, PUBLIC_KEY)
                .setResultCallback(new ResultCallback<SafetyNetApi.RecaptchaTokenResult>() {
                    @Override
                    public void onResult(SafetyNetApi.RecaptchaTokenResult result) {
                        Status status = result.getStatus();

                        if ((status != null) && status.isSuccess()) {


                            if (!result.getTokenResult().isEmpty()) {

                                Toast.makeText(getApplicationContext(), "success", LENGTH_LONG).show();
                                response = result.getTokenResult();
                                doGetData();
                            }


                        } else {

                            Log.e("MY_APP_TAG", "Error occurred " +
                                    "when communicating with the reCAPTCHA service.");


                        }
                    }
                });


    }


    private void doGetData() {



        final Thread thread = new Thread() {
            @Override
            public void run() {


                try {
                    String url = "https://www.google.com/recaptcha/api/siteverify";

                    String query = "secret=" + PRIVATE_KEY + "&response=" + response;

                    HttpClient httpClient = new DefaultHttpClient();

                    Log.e(TAG, "urlParameters--" + url + query);
                    HttpGet httpost = new HttpGet(url + "?" + query);

                    HttpResponse response;

                    response = httpClient.execute(httpost);
                    HttpEntity resEntity = response.getEntity();

                    if (resEntity != null) {
                        final String responseStr = EntityUtils.toString(resEntity).trim();
                        JSONObject json = new JSONObject(responseStr);
                        Boolean status = json.getBoolean("success");
                        if (status) {
                            flag = 1;
                            final_res = responseStr.toString();
                        } else {

                        }
                        Log.e(TAG, "responseStr---" + responseStr);


                    }
                } catch (final MalformedURLException e) {
                    showError("Error : MalformedURLException " + e);

                    e.printStackTrace();
                } catch (final IOException e) {
                    showError("Error : IOException " + e);
                    e.printStackTrace();
                } catch (Exception e) {
                    showError(e.toString());
                }

                Message msg = handler.obtainMessage();
                final Bundle b = new Bundle();
                msg.what = flag;
                handler.sendMessage(msg);
            }

        };
        thread.start();


    }

    protected Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                captcha_status = true;
                Log.e(TAG, final_res + "rhtfghff");
                Intent intent = new Intent(LoginActivity.this, Welcome.class);
                startActivity(intent);

            } else {

            }
        }
    };


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "connnected");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "connnected suspended");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connnected failed");

    }

    void showError(final String err) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(LoginActivity.this, err, LENGTH_LONG).show();
            }
        });
    }


}

