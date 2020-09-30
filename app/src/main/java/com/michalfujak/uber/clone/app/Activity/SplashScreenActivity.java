package com.michalfujak.uber.clone.app.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michalfujak.uber.clone.app.R;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

public class SplashScreenActivity extends AppCompatActivity {

    // static variable
    private final static int LOGIN_REQUEST_CODE = 7171;
    // variable
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseLisener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();
        //
        // SplashScreenActivityStartTimer();
    }

    /**
     * fun:     SplashScreenActivityStartTimer
     * param:   null
     */
    private void SplashScreenActivityStartTimer()
    {
        Completable.timer(5, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // start Thread.start
                    Toast.makeText(SplashScreenActivity.this, "Splash Screen done!", Toast.LENGTH_LONG).show();
                });
    }

    /**
     * function: init
     * return null
     */
    private void init()
    {
        // Call firebase from Phone and Email
        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        //
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseLisener = myFirebaseAuth -> {
            FirebaseUser firebaseUser = myFirebaseAuth.getCurrentUser();
            if(firebaseUser != null)
            {
                SplashScreenActivityStartTimer();
            }
            else
            {
                showLoginLayout();
            }
        };
    }

    /**
     * function: showLoginLayout
     */
    private void showLoginLayout()
    {
        // continue
    }
}