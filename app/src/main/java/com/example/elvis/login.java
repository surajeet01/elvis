package com.example.elvis;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class login extends AppCompatActivity {

    private View loginLogoView;
    com.google.android.material.textfield.TextInputEditText mobileNoText;

    private LoginButton fbLoginBtn;
    private CallbackManager callbackManager;
    private FirebaseAuth firebaseAuth;

    final String FB_LOGIN_TAG = "FacebookAuth";

    public void loginVerifyClick(View view) {
        String mobile = mobileNoText.getText().toString();

        if(mobile.length() != 10) {
            Toast.makeText(login.this, "Invalid Mobile No!", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(login.this, otp.class);
        intent.putExtra("mobileNo", mobile);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(login.this, loginLogoView, ViewCompat.getTransitionName(loginLogoView));
        startActivity(intent, optionsCompat.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.parseColor("#1f1f1d"));
        getWindow().setEnterTransition(null);
        setContentView(R.layout.activity_login);

        mobileNoText = (com.google.android.material.textfield.TextInputEditText)findViewById(R.id.loginMobile);
        loginLogoView = findViewById(R.id.loginLogo);
        fbLoginBtn = findViewById(R.id.loginFacebookBtn);
        fbLoginBtn.setPermissions(Arrays.asList("email","user_likes","user_gender","user_birthday"));

        firebaseAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        fbLoginBtn.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(FB_LOGIN_TAG, "Login Successful");
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(FB_LOGIN_TAG, "Login Cancelled");
                Toast.makeText(login.this, "FB Sign-in Cancelled! Try again", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(FB_LOGIN_TAG, "Login Failed");
                Toast.makeText(login.this, "FB Sign-in Failed!" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Log.d(FB_LOGIN_TAG, "Firebase Facebook Login Success");
                    fbLoginBtn.setText("LOGGING YOU IN");

                    final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference usersReference = FirebaseDatabase.getInstance().getReference("users");
                    Query checkUser = usersReference.orderByChild("uid").equalTo(uid);
                    checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String _nameFromDb = snapshot.child(uid).child("name").getValue(String.class);
                                String _dobFromDb = snapshot.child(uid).child("dob").getValue(String.class);
                                String _sexFromDb = snapshot.child(uid).child("sex").getValue(String.class);
                                String _emailFromDb = snapshot.child(uid).child("email").getValue(String.class);
                                String _mobileFromDb = snapshot.child(uid).child("mobile").getValue(String.class);

                                /* create login session */
                                sessionManager newSessionManager = new sessionManager(login.this);
                                newSessionManager.createLoginSession(uid, _mobileFromDb, _nameFromDb, _sexFromDb, _dobFromDb, _emailFromDb);

                                /* Go to Profile */
                                Intent intent = new Intent(login.this, profile.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(login.this, loginLogoView, ViewCompat.getTransitionName(loginLogoView));
                                startActivity(intent, optionsCompat.toBundle());
                                finish();
                            } else {
                                /* create a session only with only email address */
                                sessionManager newLoginSession = new sessionManager(login.this);
                                String fb_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                                newLoginSession.createInitLoginSessionWithEmail(uid, fb_email);

                                Intent intent = new Intent(login.this, initProfile.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(login.this, loginLogoView, ViewCompat.getTransitionName(loginLogoView));
                                startActivity(intent, optionsCompat.toBundle());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(login.this, "Signin failed! Try again", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(login.this, "Signin failed! Try again", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}