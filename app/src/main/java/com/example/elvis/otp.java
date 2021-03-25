package com.example.elvis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class otp extends AppCompatActivity {

    private TextView headerText;
    private com.chaos.view.PinView otpPinView;
    private String otpBySystem;
    private View otpLogoView;
    private String mobileNo;
    private Button otpVerifyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.parseColor("#1f1f1d"));
        setContentView(R.layout.activity_otp);

        headerText = (TextView)findViewById(R.id.otpHeaderText);
        otpPinView = (com.chaos.view.PinView)findViewById(R.id.otpPinView);
        mobileNo = getIntent().getStringExtra("mobileNo");
        mobileNo = "+91" + mobileNo;
        headerText.setText("OTP has been sent on\n" + mobileNo);
        otpLogoView = findViewById(R.id.otpLogo);
        otpVerifyBtn = (Button)findViewById(R.id.otpVerifyBtn);
        
        sendVerificationCodeToUser(mobileNo);
    }

    private void sendVerificationCodeToUser(String mobileNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNo,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks
        );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    otpBySystem = s;
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if(code != null) {
                        otpPinView.setText(code);
                        verifyCode(code);
                    } else {
                        if(otpBySystem == null) {
                            headerText.setText("Failed to send OTP on\n" + mobileNo);
                            otpPinView.setVisibility(View.INVISIBLE);
                            otpVerifyBtn.setVisibility(View.INVISIBLE);
                        } else {
                            headerText.setText("Couldn't verify \n" + mobileNo);
                            Toast.makeText(otp.this, "Recheck your OTP!", Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Toast.makeText(otp.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            };

    public void otpVerifyClick(View view) {
        String code = otpPinView.getText().toString();
        if(!code.isEmpty() && code.length() == 6) {
            verifyCode(code);
        } else {
            otpPinView.setError("Enter OTP");
            otpPinView.requestFocus();
            return;
        }
    }

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpBySystem, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
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
                                        sessionManager newSessionManager = new sessionManager(otp.this);
                                        newSessionManager.createLoginSession(uid, _mobileFromDb, _nameFromDb, _sexFromDb, _dobFromDb, _emailFromDb);

                                        /* Go to Profile */
                                        Intent intent = new Intent(otp.this, profile.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(otp.this, otpLogoView, ViewCompat.getTransitionName(otpLogoView));
                                        startActivity(intent, optionsCompat.toBundle());
                                        finish();
                                    } else {
                                        /* create a session only with mobile no */
                                        sessionManager newLoginSession = new sessionManager(otp.this);
                                        newLoginSession.createInitLoginSessionWithMobile(uid, mobileNo);

                                        Intent intent = new Intent(otp.this, initProfile.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(otp.this, otpLogoView, ViewCompat.getTransitionName(otpLogoView));
                                        startActivity(intent, optionsCompat.toBundle());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(otp.this, "Signin failed! Try again", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}