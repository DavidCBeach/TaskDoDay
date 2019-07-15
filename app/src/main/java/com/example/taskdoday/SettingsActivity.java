package com.example.taskdoday;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.BaseColumns;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    private String millicode;
    private FeedReaderDbHelper dbHelper;
    private Integer theme;
    private String primaryColor;
    private String accentColor;
    private int mHours;
    private int mMinutes;
    private String contents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Settings");
        setContentView(R.layout.activity_settings);
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        themeRead();

        Switch daily = findViewById(R.id.daily);
        final RelativeLayout dailyrl = findViewById(R.id.timezone1);
        if(notifIsReminder()){
            daily.setChecked(true);
        } else {
            daily.setChecked(false);
        }
        if(daily.isChecked()){
            dailyrl.setVisibility(View.VISIBLE);
            getNotif();
        }
        Switch rollover = findViewById(R.id.rollovertask);
        final RelativeLayout rolloverrl = findViewById(R.id.timezone2);
        if(rollover.isChecked()){
            rolloverrl.setVisibility(View.VISIBLE);
        }
        daily.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SlideAnimationUtil.slideInFromTopActual(getApplicationContext(), findViewById(R.id.timezone1));
                    dailyrl.setVisibility(View.VISIBLE);
                    contents = "reminder";

                    notifUpdate(mHours, mMinutes,contents);
                    getNotif();
                } else {
                    SlideAnimationUtil.slideInToTopActual(getApplicationContext(), findViewById(R.id.timezone1));
                    dailyrl.setVisibility(View.GONE);
                    contents = "forgetter";
                    notifUpdate(mHours,mMinutes,contents);
                    stopNotif();
                }
            }
        });
        rollover.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    SlideAnimationUtil.slideInFromTopActual(getApplicationContext(), findViewById(R.id.timezone2));
//                    rolloverrl.setVisibility(View.VISIBLE);
                } else {
//                    SlideAnimationUtil.slideInToTopActual(getApplicationContext(), findViewById(R.id.timezone2));
//                    rolloverrl.setVisibility(View.GONE);
                }
            }
        });
        TextView renumber1 = findViewById(R.id.renumber1);
        renumber1.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                TextView renumber1 = findViewById(R.id.renumber1);
                TextView renumber2 = findViewById(R.id.renumber2);
                TextView reampm = findViewById(R.id.reampm);
                String hour = renumber1.getText().toString();
                if(reampm.getText().toString().equals("am") && renumber1.getText().toString().equals("12"))
                {
                    hour = "0";
                }
                String minute = renumber2.getText().toString();
                int ihour = Integer.parseInt(hour);
                int iminute = Integer.parseInt(minute);
                System.out.println(reampm.getText().toString());
                System.out.println(ihour);
                if(reampm.getText().toString().equals("pm")){
                    ihour+=12;
                }
                mHours = ihour;
                mMinutes = iminute;
                setNotif(ihour,iminute);
                notifUpdate(ihour,iminute, contents);



            }
        });



    }

    private boolean notifIsReminder() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT
        };


        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = ?";

        String[] selectionArgs = {"-1"};
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
            contents = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT));
            System.out.println(contents);
        }
        cursor.close();
        db.close();
        if(contents.equals("reminder")){
            return true;
        }
        return false;
    }

    private void setTheme(Boolean set){
        if(theme == 0){
            //setTheme( R.style.AppTheme);
            primaryColor="#008577";
            accentColor="#D81B60";

            if(!set) {
                Update("1",0);
                Toast.makeText(getApplicationContext(), "Watermelon Theme Set", Toast.LENGTH_LONG).show();
            }
        } else if(theme == 1){
            //setTheme( R.style.AppTheme2);
            primaryColor="#F5E2E2";
            accentColor="#FF9696";

                if(!set) {
                    Update("1",1);
                    Toast.makeText(getApplicationContext(), "Light Theme Set", Toast.LENGTH_LONG).show();
                }
        } else {
            //setTheme( R.style.AppTheme3);
            primaryColor="#313131";
            accentColor="#FFC800";
            Update("1",2);
                    if(!set) {
                        Update("1",2);
                        Toast.makeText(getApplicationContext(), "Dark Theme Set", Toast.LENGTH_LONG).show();
                    }

        }
        if(set){
            setContentView(R.layout.activity_settings);
        }
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
    private void Update(String id, Integer status) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();


        // New value for one column

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, status);

        // Which row to update, based on the title
        String selection = BaseColumns._ID + " LIKE ?";
        String[] selectionArgs = { id };

        int count = db.update(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

    }
    private void notifUpdate(int hour, int minute, String content) {
        TextView reampm = findViewById(R.id.reampm);
        System.out.println("notifUpdate  " + reampm.getText().toString());


        String date = Integer.toString(hour) + ":" + Integer.toString(minute);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        // New value for one column

        ContentValues values = new ContentValues();

        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT, content);
        System.out.println(content);

        // Which row to update, based on the title
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " LIKE ?";
        String[] selectionArgs = { "-1" };

        int count = db.update(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

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
        setTheme(true);

    }

    public void theme1click(View view) {
        theme = 0;

        setTheme(false);

    }


    public void theme2click(View view) {
        theme = 1;

        setTheme(false);

    }


    public void theme3click(View view) {
        theme = 2;

        setTheme(false);

    }


    private void setNotif(int hour, int minute){
        TextView reampm = findViewById(R.id.reampm);
        System.out.println("setNotif  " + reampm.getText().toString());
        Calendar tempcal = Calendar.getInstance();
        System.out.println(hour);
        tempcal.set(Calendar.HOUR, hour+12);
        tempcal.set(Calendar.MINUTE, minute);
        System.out.println(tempcal.getTime());
        tempcal.add(Calendar.DAY_OF_MONTH, -1);
        System.out.println(tempcal.getTime());
        System.out.println("setNotif");
        System.out.println(tempcal.getTime());
        Intent notifyIntent = new Intent(this,MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (getApplicationContext(), 3, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        //once a day
        Long milli = 1000 * 60 * 60 * 24L;
        //once every 10 minutes
        //Long milli = 1000 * 60 * 10L;
        //once an hour
        //Long milli = 1000 * 60 * 60L;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  tempcal.getTimeInMillis(),
                milli , pendingIntent);


    }
    private void stopNotif(){
        Calendar tempcal = Calendar.getInstance();
        tempcal.set(Calendar.HOUR, mHours);
        tempcal.set(Calendar.MINUTE, mMinutes);
        Intent notifyIntent = new Intent(this,MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (getApplicationContext(), 3, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
    private void getNotif(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT
        };


        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = ?";

        String[] selectionArgs = {"-1"};
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
        String time = new String();
        while(cursor.moveToNext()) {
            time = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE));
            contents = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT));



        }
        cursor.close();
        db.close();
        String[] timespit = time.split(":");
        mHours = Integer.parseInt(timespit[0]);
        mMinutes = Integer.parseInt(timespit[1]);
        TextView renumber1 = findViewById(R.id.renumber1);
        TextView renumber2 = findViewById(R.id.renumber2);
        TextView reampm = findViewById(R.id.reampm);
        int hoursactual = mHours%12;
        System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
        System.out.println(mHours);
        System.out.println(hoursactual);
        System.out.println(mMinutes);
        if(hoursactual==0){
            renumber1.setText("12");
        } else {
            renumber1.setText(Integer.toString(hoursactual));
        }
        if(mMinutes < 10){
            renumber2.setText("0" + Integer.toString(mMinutes));
        } else {
            renumber2.setText(Integer.toString(mMinutes));
        }

        if(mHours >= 12){
            reampm.setText("pm");
        } else {
            reampm.setText("am");
        }

    }


    public void openTime(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        setTheme(R.style.AppTheme2a);
        ((TimePickerFragment) newFragment).setArguements(mHours, mMinutes);
        newFragment.show(getSupportFragmentManager(), "timePicker");

    }


}
