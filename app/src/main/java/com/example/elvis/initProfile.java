package com.example.elvis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.HashMap;

public class initProfile extends AppCompatActivity {

    private String mobileNo;
    private String uid;
    private String email;

    private ImageView initProfileLogoView;
    private TextView initProfileHeaderTextView;
    private TextInputLayout initProfileSexLayout;

    private TextInputEditText initProfileNameEditText;
    private TextInputEditText initProfileDobEditText;
    private AutoCompleteTextView initProfileSexDropDown;
    private TextInputEditText initProfileEmailEditText;
    private TextInputEditText initProfileMobileEditText;

    FirebaseDatabase rootNode;
    DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.parseColor("#1f1f1d"));
        setContentView(R.layout.activity_init_profile);

        initProfileNameEditText = (TextInputEditText) findViewById(R.id.initProfileName);
        initProfileDobEditText = (TextInputEditText) findViewById(R.id.initProfileDob);
        initProfileSexDropDown = (AutoCompleteTextView) findViewById(R.id.initProfileSexDropDown);
        initProfileEmailEditText = (TextInputEditText) findViewById(R.id.initProfileEmail);
        initProfileMobileEditText = (TextInputEditText) findViewById(R.id.initProfileMobile);
        initProfileLogoView = (ImageView) findViewById(R.id.initProfileLogo);
        initProfileSexLayout = (TextInputLayout) findViewById(R.id.initProfileSexLayout);
        initProfileHeaderTextView = (TextView)findViewById(R.id.initProfileheader);

        sessionManager newSessionManager = new sessionManager(initProfile.this);
        HashMap<String, String> userData = newSessionManager.getUserDetailsFromSession();
        mobileNo = userData.get(sessionManager.KEY_MOBILE);
        email = userData.get(sessionManager.KEY_EMAIL);
        uid = userData.get(sessionManager.KEY_UID);
        if(mobileNo == null)
            mobileNo = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        if(email == null)
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if(uid == null)
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /* Set value in mobile */
        if(mobileNo != null) {
            initProfileMobileEditText.setText(mobileNo);
            initProfileMobileEditText.setEnabled(false);
            initProfileEmailEditText.setEnabled(true);
        }

        /* Set value in email */
        if(email != null) {
            initProfileEmailEditText.setText(email);
            initProfileEmailEditText.setEnabled(false);
            initProfileMobileEditText.setEnabled(true);
        }

        /* Set values in sex dropdown */
        initProfileSexDropDown.setFocusable(false);
        initProfileSexDropDown.setClickable(true);
        String[] sex_items = new String[] {"Male", "Female", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(initProfile.this, android.R.layout.select_dialog_item, sex_items);
        initProfileSexDropDown.setAdapter(adapter);

        /* Date Picker in DOB field */
        final com.google.android.material.textfield.TextInputEditText dobInputBox =
                (com.google.android.material.textfield.TextInputEditText) findViewById(R.id.initProfileDob);
        final int year = 1990, month = 0, day = 1;
        dobInputBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(initProfile.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        String s;
                        month++;
                        if(day >= 10) s = day + "/";
                        else s = "0" + day + "/";
                        if(month >= 10) s = s + month + "/" + year;
                        else s = s + "0" + month + "/" + year;
                        dobInputBox.setText(s);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });
    }

    public void logOutFromFacebook() {
        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                         "/me/permissions/",
                         null,
                          HttpMethod.DELETE, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                AccessToken.setCurrentAccessToken(null);
                LoginManager.getInstance().logOut();

                Intent intent = new Intent(initProfile.this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(initProfile.this, initProfileLogoView, ViewCompat.getTransitionName(initProfileLogoView));
                startActivity(intent, optionsCompat.toBundle());
            }
        }).executeAsync();
    }

    public void initProfileSignOutClick(View view) {
        FirebaseAuth.getInstance().signOut(); // Firebase log-out
        /* Destroy session */
        sessionManager newLoginSession = new sessionManager(initProfile.this);
        newLoginSession.destroyLoginSession();

        if (AccessToken.getCurrentAccessToken() != null) {
            logOutFromFacebook();  // Facebook log-out
        } else {
            Intent intent = new Intent(initProfile.this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(initProfile.this, initProfileLogoView, ViewCompat.getTransitionName(initProfileLogoView));
            startActivity(intent, optionsCompat.toBundle());
        }
    }

    public void initProfileSubmitClick(View view) {
        String _name = initProfileNameEditText.getText().toString();
        String _dob = initProfileDobEditText.getText().toString();
        String _sex = initProfileSexDropDown.getText().toString();
        String _email = initProfileEmailEditText.getText().toString();
        String _mobile = initProfileMobileEditText.getText().toString();
        userClass userData = new userClass(_name,_dob,_sex,_email,_mobile, uid);

        if(_name.isEmpty() || _dob.isEmpty() || _sex.isEmpty() || _email.isEmpty() || _mobile.isEmpty()) {
            Toast.makeText(initProfile.this, "Provide all the details!", Toast.LENGTH_LONG).show();
            return;
        }

        /* put data into database */
        rootNode = FirebaseDatabase.getInstance();
        usersReference = rootNode.getReference("users");
        usersReference.child(uid).setValue(userData);

        /* create a full session */
        sessionManager newLoginSession = new sessionManager(initProfile.this);
        newLoginSession.createLoginSession(uid, _mobile, _name, _sex, _dob, _email);

        /* Go to next activity */
        Intent intent = new Intent(initProfile.this, profile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(initProfile.this, initProfileLogoView, ViewCompat.getTransitionName(initProfileLogoView));
        startActivity(intent, optionsCompat.toBundle());
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}