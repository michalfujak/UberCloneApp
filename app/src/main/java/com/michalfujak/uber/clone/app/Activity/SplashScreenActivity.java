package com.michalfujak.uber.clone.app.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michalfujak.uber.clone.app.R;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    @BindView(R.id.progress_bar_screen_loading)
    ProgressBar progressBar;

    @Override
    protected void onStart() {
        super.onStart();
        SplashScreenActivityStartTimer();
    }

    @Override
    protected void onStop() {
        if(firebaseAuth != null && firebaseLisener != null)
        {
            firebaseAuth.removeAuthStateListener(firebaseLisener);
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_loading);

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
        //
        progressBar.setVisibility(View.VISIBLE);

        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // start Thread.start
                    // After show splash screen, ask login if not login
                    firebaseAuth.addAuthStateListener(firebaseLisener);
                });
    }

    /**
     * function: init
     * return void
     */
    private void init()
    {
        // ButterKnife starting
        ButterKnife.bind(this);

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
                checkUserFromFirebase();
                // Toast.makeText(this, "Welcoome: " + firebaseUser.getUid() + " ", Toast.LENGTH_LONG).show();
            }
            else
            {
                showLoginLayout();
            }
        };
    }

    /**
     * function: showLoginLayout
     * return void
     */
    private void showLoginLayout()
    {
        // AUTH METHOD FOR GOOGLE
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_login_in)
                .setPhoneButtonId(R.id.firebase_button_auth_phone)
                .setGoogleButtonId(R.id.firebase_button_auth_email)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setTheme(R.style.LoginTheme)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                .build(), LOGIN_REQUEST_CODE);
    }

    /**
     * function: checkUserFromFirebase
     * return void
     */
    private void checkUserFromFirebase()
    {
        // firebase method
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //
        IdpResponse response = IdpResponse.fromResultIntent(data);
        if(requestCode == LOGIN_REQUEST_CODE)
        {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }
        else
        {
            Toast.makeText(this, " [ ERROR ]: " + response.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}























