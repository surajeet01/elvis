package com.example.elvis;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class fragmentProfile extends Fragment {

    private TextInputEditText profileFragNameEditText;
    private AutoCompleteTextView profileFragSexDropDown;
    private TextInputEditText profileFragDobEditText;
    private TextInputEditText profileFragEmailEditText;
    private TextInputEditText profileFragMobileEditText;
    private Button profileFragSubmitButton;

    private TextView profileFragNameHeader;

    private String uid;
    private String name;
    private String dob;
    private String sex;
    private String email;
    private String mobile;

    FirebaseDatabase rootNode;
    DatabaseReference usersReference;

    public fragmentProfile() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        profileFragNameEditText = (TextInputEditText) rootView.findViewById(R.id.etProfileName);
        profileFragDobEditText = (TextInputEditText) rootView.findViewById(R.id.etProfileDob);
        profileFragEmailEditText = (TextInputEditText) rootView.findViewById(R.id.etProfileEmail);
        profileFragMobileEditText = (TextInputEditText) rootView.findViewById(R.id.etProfileMobile);
        profileFragNameHeader = rootView.findViewById(R.id.tvProfileHeader);
        profileFragSexDropDown = rootView.findViewById(R.id.ddProfileSex);
        profileFragSubmitButton = rootView.findViewById(R.id.btnProfileSubmit);

        /* Retrieve data from session */
        sessionManager newSessionManager = new sessionManager(getActivity());
        HashMap<String, String> loginSession = newSessionManager.getUserDetailsFromSession();
        name = loginSession.get(sessionManager.KEY_NAME);
        dob = loginSession.get(sessionManager.KEY_DOB);
        sex = loginSession.get(sessionManager.KEY_SEX);
        email = loginSession.get(sessionManager.KEY_EMAIL);
        mobile = loginSession.get(sessionManager.KEY_MOBILE);
        uid = loginSession.get(sessionManager.KEY_UID);

        /* set value in header */
        profileFragNameHeader.setText("Hi " + name);

        /* Set values in sex dropdown */
        profileFragSexDropDown.setFocusable(false);
        profileFragSexDropDown.setClickable(true);
        String[] sex_items = new String[] {"Male", "Female", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item, sex_items);
        profileFragSexDropDown.setAdapter(adapter);

        /* Date Picker in DOB field */
        final int year = 1990, month = 0, day = 1;
        profileFragDobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        String s;
                        month++;
                        if(day >= 10) s = day + "/";
                        else s = "0" + day + "/";
                        if(month >= 10) s = s + month + "/" + year;
                        else s = s + "0" + month + "/" + year;
                        profileFragDobEditText.setText(s);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });

        /* Pre-fill form with existing data */
        profileFragNameEditText.setText(name);
        profileFragDobEditText.setText(dob);
        profileFragSexDropDown.setText(sex, false);
        profileFragEmailEditText.setText(email);
        profileFragMobileEditText.setText(mobile);

        if(AccessToken.getCurrentAccessToken() == null)
            profileFragMobileEditText.setEnabled(false);
        else
            profileFragEmailEditText.setEnabled(false);

        profileFragSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileUpdateSubmitClick(null);
            }
        });

        return rootView;
    }


    public void profileUpdateSubmitClick(View view) {
        String _name = profileFragNameEditText.getText().toString();
        String _dob = profileFragDobEditText.getText().toString();
        String _sex = profileFragSexDropDown.getText().toString();
        String _email = profileFragEmailEditText.getText().toString();
        String _mobile = profileFragMobileEditText.getText().toString();

        if(_name.isEmpty() || _dob.isEmpty() || _sex.isEmpty() || _email.isEmpty() || _mobile.isEmpty()) {
            Toast.makeText(getActivity(), "Provide all the details!", Toast.LENGTH_LONG).show();
            return;
        }

        name = _name;
        dob = _dob;
        sex = _sex;
        email =_email;
        mobile = _mobile;

        userClass userData = new userClass(_name,_dob,_sex,_email,_mobile, uid);

        /* put data into database */
        rootNode = FirebaseDatabase.getInstance();
        usersReference = rootNode.getReference("users");
        usersReference.child(uid).setValue(userData);

        /* create a full session */
        sessionManager newLoginSession = new sessionManager(getActivity());
        newLoginSession.createLoginSession(uid, _mobile, _name, _sex, _dob, _email);

        /* update header */
        profileFragNameHeader.setText("Hi " + name);
        Toast.makeText(getActivity(), "Details updated", Toast.LENGTH_LONG).show();
    }

}