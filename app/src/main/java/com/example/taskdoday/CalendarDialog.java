package com.example.taskdoday;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarDialog extends DialogFragment {



    private Calendar calendar;
    private GregorianCalendar Gcalendar;
    private int color;
    private int colorBackground;
    public CalendarDialog() {

    }
    public void setArguements(Calendar mycalendar,int myColor, int myColorBackground){
        calendar = mycalendar;
        Gcalendar = new GregorianCalendar();
        Gcalendar.setTimeInMillis(calendar.getTimeInMillis());
        color = myColor;
        colorBackground = myColorBackground;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_calendar, container);
        CalendarView cv = view.findViewById(R.id.simpleCalendarView);
        Long milli = calendar.getTimeInMillis();
        cv.setDate(milli);
        cv.setOnDateChangeListener( new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Gcalendar = new GregorianCalendar( year, month, dayOfMonth );
            }//met
        });
        Button cancel = view.findViewById(R.id.calendarCancel);
        cancel.setTextColor(getResources().getColor(color));
        cancel.setBackgroundColor(getResources().getColor(colorBackground));
        Button go = view.findViewById(R.id.go);
        go.setTextColor(getResources().getColor(color));
        go.setBackgroundColor(getResources().getColor(colorBackground));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }
    public void dismisser(){
        dismiss();
    }
    public GregorianCalendar getGcalendar(){
        return Gcalendar;
    }

}