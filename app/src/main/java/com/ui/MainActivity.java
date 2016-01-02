package com.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ui.gridview.GridViewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startGridViewActivity();
    }

    private void startGridViewActivity() {
        Intent intent = new Intent(this, GridViewActivity.class);
        startActivity(intent);
    }
}
