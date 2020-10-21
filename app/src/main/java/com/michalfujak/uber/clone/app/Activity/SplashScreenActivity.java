package com.michalfujak.uber.clone.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.michalfujak.uber.clone.app.Modul.Common;
import com.michalfujak.uber.clone.app.Modul.DriverInfoModel;
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

    // Firebase - reference
    FirebaseDatabase firebaseDatabase;
    DatabaseReference driverInfoReference;

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

        // firebase.db
        firebaseDatabase = FirebaseDatabase.getInstance();
        driverInfoReference = firebaseDatabase.getReference(Common.DRIVER_INFO_REFERENCE);

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
        driverInfoReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //
                        if(dataSnapshot.exists())
                        {
                            // user is already
                            Toast.makeText(SplashScreenActivity.this, "User is already register!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(SplashScreenActivity.this, " " + databaseError.getMessage() + " ", Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * function: showRegisterLayout
     * return: void
     * param: null
     */
    private void showRegisterLayout()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alertDialogRegister);
        // View.inflate
        View inflateView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        // TextFieldEditText object
        TextInputEditText registerFirstName = (TextInputEditText)inflateView.findViewById(R.id.register_text_input_first_name);
        TextInputEditText registerLastName = (TextInputEditText)inflateView.findViewById(R.id.register_text_input_last_name);
        TextInputEditText registerPhoneNumber = (TextInputEditText)inflateView.findViewById(R.id.register_text_input_phone_number);

        // Button
        Button continueAction = (Button)inflateView.findViewById(R.id.register_account_create_continue);
        // Set data for Firebase
        if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
        {
            // SetView
            builder.setView(inflateView);
            AlertDialog dialog = builder.create();
            dialog.show();

            continueAction.setOnClickListener(view -> {
                // check first name, is empty...
                if(TextUtils.isEmpty(registerFirstName.getText().toString()))
                {
                    Toast.makeText(this, getString(R.string.register_alert_toast_empty_first_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                // check last name, is empty
                else if(TextUtils.isEmpty(registerLastName.getText().toString()))
                {
                    Toast.makeText(this, getString(R.string.register_alert_toast_empty_last_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                // check phone number, is empty
                else if(TextUtils.isEmpty(registerPhoneNumber.getText().toString()))
                {
                    Toast.makeText(this, getString(R.string.register_alert_toast_empty_phone_number), Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    // done!
                    DriverInfoModel drivermodel = new DriverInfoModel();
                    drivermodel.setFirstname(registerFirstName.getText().toString());
                    drivermodel.setLastName(registerLastName.getText().toString());
                    drivermodel.setPhoneNumber(registerPhoneNumber.getText().toString());
                    drivermodel.setRating(0.0);

                    // Call Firebase
                    driverInfoReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(drivermodel)
                            .addOnFailureListener(e -> {
                                        dialog.dismiss();
                                        Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, getString(R.string.register_activity_message_done), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            });
                }
            });
        }
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























