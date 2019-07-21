package com.example.taskdoday;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    private FeedReaderDbHelper dbHelper;
    private Integer theme;
    private int mHours;
    private int mMinutes;
    private boolean mStatus;
    public int shownHours;
    public int shownMinutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("Settings");
        setContentView(R.layout.activity_settings);
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        themeRead();

        Switch daily = findViewById(R.id.daily);
        final RelativeLayout dailyrl = findViewById(R.id.timezone1);
        try {
            if(notifIsReminder()){
                daily.setChecked(true);
            } else {
                daily.setChecked(false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(daily.isChecked()){
            dailyrl.setVisibility(View.VISIBLE);
            try {
                getNotif();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Switch rollover = findViewById(R.id.rollovertask);
        if(getRollover())
            rollover.setChecked(true);
        else
            rollover.setChecked(false);

        TextView version = findViewById(R.id.version);
        version.setText("Version: "+BuildConfig.VERSION_NAME);



//        final RelativeLayout rolloverrl = findViewById(R.id.timezone2);
//        if(rollover.isChecked()){
//            rolloverrl.setVisibility(View.VISIBLE);
//        }
        daily.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SlideAnimationUtil.slideInFromTopActual(getApplicationContext(), findViewById(R.id.timezone1));
                    dailyrl.setVisibility(View.VISIBLE);
                    mStatus = true;

                    notifUpdate(mHours, mMinutes);
                    try {
                        getNotif();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    SlideAnimationUtil.slideInToTopActual(getApplicationContext(), findViewById(R.id.timezone1));
                    dailyrl.setVisibility(View.GONE);
                    mStatus = false;
                    notifUpdate(mHours,mMinutes);
                    stopNotif();
                }
            }
        });
        rollover.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
//                    SlideAnimationUtil.slideInFromTopActual(getApplicationContext(), findViewById(R.id.timezone2));
//                    rolloverrl.setVisibility(View.VISIBLE);
                    setRollover();
                } else {
//                    SlideAnimationUtil.slideInToTopActual(getApplicationContext(), findViewById(R.id.timezone2));
//                    rolloverrl.setVisibility(View.GONE);
                    stopRollover();

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
                if(reampm.getText().toString().equals("pm") && ihour != 12){
                    ihour+=12;
                }
                mHours = ihour;
                mMinutes = iminute;
                setNotif(ihour,iminute);
                notifUpdate(ihour,iminute);



            }
        });



    }
    private boolean getRollover() {
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        return prefs.getBoolean("rollover",false);
    }

    private void stopRollover() {
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("rollover", false);
        editor.commit();

    }

    private void setRollover() {
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("rollover", true);
        editor.commit();


    }

    private boolean notifIsReminder() throws JSONException {

        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        String notification = prefs.getString("notification","");
        JSONObject obj = new JSONObject(notification);

        mStatus = obj.getBoolean("status");
        if(mStatus){
            return true;
        }
        return false;
    }

    private void setTheme(Boolean set){
        if(theme == 0){
            if(!set) {
                Update(0);
                Toast.makeText(getApplicationContext(), "Watermelon Theme Set", Toast.LENGTH_LONG).show();
            }
        } else if(theme == 1){

                if(!set) {
                    Update(1);
                    Toast.makeText(getApplicationContext(), "Light Theme Set", Toast.LENGTH_LONG).show();
                }
        } else {
            Update(2);
                    if(!set) {
                        Update(2);
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
    private void Update( Integer theme) {
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("theme", theme);
        editor.commit();

    }
    private void notifUpdate(int hour, int minute) {
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONObject jo = new JSONObject();
        try {
            jo.put("hour",hour);
            jo.put("minute",minute);
            jo.put("status",mStatus);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String date = jo.toString();
        editor.putString("notification", date);
        editor.commit();

    }
    private void themeRead(){
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        theme = prefs.getInt("theme",1);
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
        Calendar tempcal = Calendar.getInstance();
        Log.d("test",Integer.toString(hour));
        Calendar actualTime = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("HH");
        String hourActual =  mdformat.format(actualTime.getTime());
        int ihourActual = Integer.valueOf(hourActual);
        if(ihourActual > 12){
            hour=(hour +12)%24;
        }
        Log.d("test",Integer.toString(hour));
        Log.d("test",Integer.toString(minute));
        tempcal.add(Calendar.DAY_OF_MONTH, -1);
        tempcal.set(Calendar.HOUR, hour);
        tempcal.set(Calendar.MINUTE, minute);
        Log.d("test",tempcal.getTime().toString());
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
    private void getNotif() throws JSONException {

        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        String notification = prefs.getString("notification","");
        JSONObject obj = new JSONObject(notification);
        mStatus = obj.getBoolean("status");
        mHours = obj.getInt("hour");
        mMinutes = obj.getInt("minute");
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
