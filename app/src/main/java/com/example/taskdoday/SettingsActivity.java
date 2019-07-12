package com.example.taskdoday;

import android.app.ActionBar;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    private String millicode;
    private FeedReaderDbHelper dbHelper;
    private Integer theme;
    private String primaryColor;
    private String accentColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Settings");
        setContentView(R.layout.activity_settings);
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        themeRead();


    }
    private void setTheme(){
        if(theme == 0){
            setTheme( R.style.AppTheme);
            primaryColor="#008577";
            accentColor="#D81B60";
        } else if(theme == 1){
            setTheme( R.style.AppTheme2);
            primaryColor="#F5E2E2";
            accentColor="#FF9696";
        } else {
            setTheme( R.style.AppTheme3);
            primaryColor="#313131";
            accentColor="#FFC800";
        }
        setContentView(R.layout.activity_settings);
        CheckBox theme1 = findViewById(R.id.theme1);
        CheckBox theme2 = findViewById(R.id.theme2);
        CheckBox theme3 = findViewById(R.id.theme3);
        if(theme == 0){
            theme1.setChecked(true);
            theme2.setChecked(false);
            theme3.setChecked(false);

        } else if(theme == 1){
            theme1.setChecked(false);
            theme2.setChecked(true);
            theme3.setChecked(false);

        } else {
            theme1.setChecked(false);
            theme2.setChecked(false);
            theme3.setChecked(true);

        }


    }
    private void themeRead(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS,
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = FeedReaderContract.FeedEntry._ID + " = ?";
        //String[] selectionArgs = { "My Title" };

        String[] selectionArgs = {"1"};
        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " ASC";



        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        while(cursor.moveToNext()) {
            theme = cursor.getInt(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS));


        }
        cursor.close();
        db.close();
        setTheme();
    }

    public void theme1click(View view) {
        theme = 0;

        setTheme();

    }


    public void theme2click(View view) {
        theme = 1;

        setTheme();

    }


    public void theme3click(View view) {
        theme = 2;

        setTheme();

    }
}
