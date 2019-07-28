package com.taskdoday.taskdoday;

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

public class SortDialog extends DialogFragment {


    private int accentColor;
    private int colorBackground;

    public SortDialog() {

    }
    public void setAttributes(int myaccentColor, int mycolorBackground){
        accentColor = myaccentColor;
        colorBackground = mycolorBackground;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_sort, container);
        Button sort_by_completed = view.findViewById(R.id.sort_by_completed);
        sort_by_completed.setTextColor(getResources().getColor(accentColor));
        sort_by_completed.setBackgroundColor(getResources().getColor(colorBackground));
        Button sort_by_time = view.findViewById(R.id.sort_by_time);
        sort_by_time.setTextColor(getResources().getColor(accentColor));
        sort_by_time.setBackgroundColor(getResources().getColor(colorBackground));
        Button sort_by_name = view.findViewById(R.id.sort_by_name);
        sort_by_name.setTextColor(getResources().getColor(accentColor));
        sort_by_name.setBackgroundColor(getResources().getColor(colorBackground));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }
    public void dismisser(){
        dismiss();
    }

}