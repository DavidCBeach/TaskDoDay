package com.example.taskdoday;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;

import org.w3c.dom.Text;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {
    int mHours;
    int mMinutes;

    public void setArguements(int hour, int minute){
        mHours = hour;
        mMinutes = minute;

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = mHours;
        int minute = mMinutes;
        TimePickerDialog tpd =  new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        // Create a new instance of TimePickerDialog and return it
        return tpd;

    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        TextView renumber1 = super.getActivity().findViewById(R.id.renumber1);
        TextView renumber2 = super.getActivity().findViewById(R.id.renumber2);
        TextView reampm = super.getActivity().findViewById(R.id.reampm);

        mHours = hourOfDay;
        mMinutes = minute;
        int hoursactual = hourOfDay%12;
        if(hourOfDay >= 12){
            reampm.setText("pm");
        } else {
            reampm.setText("am");
        }
        if(minute < 10){
            String sminute = Integer.toString(minute);
            String minuteactual= "0" + sminute;
            renumber2.setText(minuteactual);
        } else {
            renumber2.setText(Integer.toString(minute));
        }
        if(hoursactual==0){
            renumber1.setText("12");
        } else {
            renumber1.setText(Integer.toString(hoursactual));
        }


    }

    public int[] getArguements(){
        int[] args = new int[2];
        args[0] = mHours;
        args[1] = mMinutes;
        return args;
    }
}
