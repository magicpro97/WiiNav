package com.linh.wiinav.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public abstract class BaseActivity extends AppCompatActivity
{
    protected DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    protected SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected abstract void addEvents();

    protected abstract void addControls();

    public void showProgressDialog() {

    }

    public void hideProgressDialog() {

    }

    public void showToastMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showSnackbarMessage(View v, String message, int duration) {
        Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getUid().toString();
    }

    public void hideKeyboard(final View view)
    {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void backToMapsScreen() {
        startActivity(new Intent(this, MapsActivity.class));
        finish();
    }
}
