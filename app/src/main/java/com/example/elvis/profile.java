package com.example.elvis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.HashMap;

public class profile extends AppCompatActivity {

    private String name;
    private String dob;
    private String sex;
    private String email;
    private String mobile;
    private String uid;

    private ChipNavigationBar chipNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.parseColor("#1f1f1d"));
        setContentView(R.layout.activity_profile);

        /* Retrieve data from session */
        sessionManager newSessionManager = new sessionManager(profile.this);
        HashMap<String, String> loginSession = newSessionManager.getUserDetailsFromSession();
        name = loginSession.get(sessionManager.KEY_NAME);
        dob = loginSession.get(sessionManager.KEY_DOB);
        sex = loginSession.get(sessionManager.KEY_SEX);
        email = loginSession.get(sessionManager.KEY_EMAIL);
        mobile = loginSession.get(sessionManager.KEY_MOBILE);
        uid = loginSession.get(sessionManager.KEY_UID);

        getSupportFragmentManager().beginTransaction().replace(R.id.profileFragmentContainer, new fragmentSearch()).commit();

        chipNavigationBar = (ChipNavigationBar)findViewById(R.id.profileNavBar);
        chipNavigationBar.setItemSelected(R.id.bottom_nav_search, true);
        bottomMenu();
    }

    private void bottomMenu() {
        chipNavigationBar.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                switch (i) {
                    case R.id.bottom_nav_profile:
                        fragment = new fragmentProfile();
                        break;
                    case R.id.bottom_nav_search:
                        fragment = new fragmentSearch();
                        break;
                    case R.id.bottom_nav_message:
                        fragment = new fragmentMessages();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.profileFragmentContainer, fragment).commit();
            }
        });
    }

    public void logOutFromFacebook() {
        new GraphRequest(AccessToken.getCurrentAccessToken(),
                "/me/permissions/",
                null,
                HttpMethod.DELETE, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                AccessToken.setCurrentAccessToken(null);
                LoginManager.getInstance().logOut();

                Intent intent = new Intent(profile.this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }).executeAsync();
    }

    public void btnProfileSignOutClick(View view) {
        FirebaseAuth.getInstance().signOut();

        /* Destroy session */
        sessionManager newLoginSession = new sessionManager(profile.this);
        newLoginSession.destroyLoginSession();

        if(AccessToken.getCurrentAccessToken() != null)
            logOutFromFacebook();
        else {
            Intent intent = new Intent(profile.this, login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}