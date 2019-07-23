package com.taskdoday.taskdoday;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarActivity extends AppCompatActivity {
    private GregorianCalendar calendar;
    private Calendar ccalendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        CalendarView cv = findViewById(R.id.simpleCalendarView);
        String smilli = getIntent().getStringExtra("calendar");


        if(smilli != null){
            Long milli = Long.decode(smilli);
            cv.setDate(milli);
        }

        calendar = new GregorianCalendar();
        cv.setOnDateChangeListener( new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                calendar = new GregorianCalendar( year, month, dayOfMonth );
                calendar.getTimeInMillis();
            }//met
        });

    }

    public void goToDate(View view) {
        ccalendar = Calendar.getInstance();
                ccalendar.setTimeInMillis(calendar.getTimeInMillis());
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("calendar", Long.toString(calendar.getTimeInMillis()));
        startActivity(intent);
    }
}
