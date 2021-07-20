package com.example.sociallogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessTokenTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener {

    private TextView data,textView,tsfh,ttext;
    private ImageView profile,tsf;
    private LoginButton login;
    Animation topAnim,bottomAnim;



    SignInButton signInButton;
    private GoogleApiClient googleApiClient;
    private static final int SIGN_IN =1;
    CallbackManager callbackmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,
                gso).build();

        signInButton = findViewById(R.id.googlesignin);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, SIGN_IN);
            }
        });


        data=findViewById(R.id.data);
        profile=findViewById(R.id.profile);
        login=findViewById(R.id.login);
        textView=findViewById(R.id.name);
        tsfh=findViewById(R.id.tsfh);
        ttext=findViewById(R.id.ttext);
        tsf=findViewById(R.id.tsf);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim=AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        tsf.setAnimation(topAnim);
        tsfh.setAnimation(bottomAnim);
        ttext.setAnimation(topAnim);

        callbackmanager = CallbackManager.Factory.create();


        login.registerCallback(callbackmanager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                data.setText("User Id: " + loginResult.getAccessToken().getUserId());
                String imageUrl = "https://graph.facebook.com/" + loginResult.getAccessToken().getUserId() + "/picture?return_ssl_resources=1";
                signInButton.setVisibility(View.GONE);
                tsf.setVisibility(View.GONE);
                tsfh.setVisibility(View.GONE);
                ttext.setVisibility(View.GONE);
            }


            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackmanager.onActivityResult(requestCode,resultCode,data);

        if(requestCode == SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess()) {
                startActivity(new Intent(MainActivity.this, profileActivity.class));
                finish();
            }else{
                Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show();

            }
        }


        GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse graphResponse) {
                        Log.d("Demo",object.toString());

                        try {
                            String name = object.getString("name");
                            textView.setText(name);
                            String pic = object.getJSONObject("picture").getJSONObject("data").getString("url");
                            Picasso.get().load(pic).into(profile);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });

        Bundle bundle= new Bundle();
        bundle.putString("fields","gender,name,id,first_name,last_name,birthday,picture.width(150).height(150)");
        graphRequest.setParameters(bundle);
        graphRequest.executeAsync();
    }

    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if(currentAccessToken==null){
                LoginManager.getInstance().logOut();
                textView.setText("");
                profile.setImageResource(0);
                data.setText("");
                signInButton.setVisibility(View.VISIBLE);
                tsf.setVisibility(View.VISIBLE);
                tsfh.setVisibility(View.VISIBLE);
                ttext.setVisibility(View.VISIBLE);
            }
        }
    };

    protected void onDestroy(){
        super.onDestroy();
        accessTokenTracker.startTracking();
    }

    @Override
    public void onConnectionFailed(@NonNull @NotNull ConnectionResult connectionResult) {

    }
}