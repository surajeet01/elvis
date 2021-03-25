package com.example.elvis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;

import com.google.android.material.textfield.TextInputLayout;

public class register extends AppCompatActivity {

    private TextInputLayout sexInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.parseColor("#1f1f1d"));
        setContentView(R.layout.activity_register);

        /* Set values in sex dropdown */
        sexInputLayout = (TextInputLayout) findViewById(R.id.newSexInputLayout);
        AutoCompleteTextView sexDropDown = (AutoCompleteTextView) findViewById(R.id.newSexDropDown);
        sexDropDown.setFocusable(false);
        sexDropDown.setClickable(true);
        String[] sex_items = new String[] {"Male", "Female", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(register.this, android.R.layout.select_dialog_item, sex_items);
        sexDropDown.setAdapter(adapter);

        /* Date Picker in DOB field */
        final com.google.android.material.textfield.TextInputEditText dobInputBox =
                (com.google.android.material.textfield.TextInputEditText) findViewById(R.id.newDobEditText);
        final int year = 1990, month = 0, day = 1;
        dobInputBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(register.this, new DatePickerDialog.OnDateSetListener() {
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

        /* Already has an account button */
        Button alreadyUser = findViewById(R.id.haveAccountBtn);
        alreadyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(register.this, login.class);
                View registerLogoView = findViewById(R.id.registerLogo);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(register.this, registerLogoView, ViewCompat.getTransitionName(registerLogoView));
                startActivity(intent, optionsCompat.toBundle());
            }
        });
    }
}