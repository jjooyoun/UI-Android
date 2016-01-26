package com.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ui.gridview.GridViewActivity;
import com.ui.gridview.GridViewDragAndDropActivity;
import com.ui.listview.ListViewActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("GridView");
        arrayList.add("GridView Drag & Drop");
        arrayList.add("ListView");
//        arrayList.add("ListView Loading");

        ArrayAdapter<String> Adapter;
        Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);

        ListView list = (ListView)findViewById(R.id.list);
        list.setOnItemClickListener(this);
        list.setAdapter(Adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            startGridViewActivity();
        } else if (position == 1) {
            startGridViewDragAndDropActivity();
        } else if (position == 2) {
            startListViewActivity();
        }
    }

    private void startGridViewActivity() {
        Intent intent = new Intent(this, GridViewActivity.class);
        startActivity(intent);
    }

    private void startGridViewDragAndDropActivity() {
        Intent intent = new Intent(this, GridViewDragAndDropActivity.class);
        startActivity(intent);
    }

    private void startListViewActivity() {
        Intent intent = new Intent(this, ListViewActivity.class);
        startActivity(intent);
    }
}
