package com.example.taskdoday;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class StatsActivity extends AppCompatActivity {
    FeedReaderDbHelper dbHelper;
    Calendar calendar;
    private int theme;
    private int primaryColor;
    private int accentColor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        themeRead();
        setTitle("Statistics");
        calendar = Calendar.getInstance();
        percentRead();


    }
    private void percentRead(){

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM");
        String month =  mdformat.format(calendar.getTime());
        int imonth = Integer.parseInt(month);
        System.out.println(imonth);
        SimpleDateFormat dmdformat = new SimpleDateFormat("dd");
        String day =  dmdformat.format(calendar.getTime());
        int iday = Integer.parseInt(day);
        SimpleDateFormat ymdformat = new SimpleDateFormat("yyyy");
        String year =  ymdformat.format(calendar.getTime());
        int iyear= Integer.parseInt(year);
        System.out.println(calendar.getTime());
        calendar.set(iyear,imonth-1,iday,0,0);
        System.out.println(calendar.getTime());
        System.out.println(calendar.getTimeInMillis());

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE,

        };
        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " ASC";
        // Filter results WHERE "title" = 'My Title'
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE_MILLI + " < ?";
        //String[] selectionArgs = { "My Title" };
        String[] selectionArgs = {Long.toString(calendar.getTimeInMillis())};

        // How you want the results sorted in the resulting Cursor


        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );


        ArrayList<Integer> list1 = new ArrayList();
        ArrayList<Integer> list0= new ArrayList();
        while(cursor.moveToNext()) {
            int status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS));
            if(status == 0){
                list0.add(status);
            } else {
                list1.add(status);
            }


        }
        if(!(list0.isEmpty() && list1.isEmpty())){
            TextView nostats = findViewById(R.id.nostats);
            nostats.setVisibility(View.GONE);
            System.out.println(list0.size());
            System.out.println(list1.size());

            Double percent = Double.valueOf(list1.size())/Double.valueOf(list1.size()+list0.size()) * 1000.0;
            percent = Math.floor(percent)/10.0;
            System.out.println(percent);
            TextView percentview = findViewById(R.id.percent);
            percentview.setText(percent.toString() + "%");
        } else {
            TextView nostats = findViewById(R.id.percenttitle);
            nostats.setVisibility(View.GONE);
        }



        cursor.close();

        db.close();

    }
    private void setTheme(){
        if(theme == 0){
            setTheme( R.style.AppTheme);
            primaryColor=R.color.colorPrimary;
            accentColor=R.color.colorAccent;
        } else if(theme == 1){
            setTheme( R.style.AppTheme2);
            primaryColor=R.color.grey2;
            accentColor=R.color.colorAccent;



        } else {
            setTheme( R.style.AppTheme3);
            primaryColor=R.color.colorDarkPrimary;
            accentColor=R.color.colorDarkAccent;

        }

        setContentView(R.layout.activity_stats);
        TextView percent = findViewById(R.id.percent);
        percent.setTextColor(getResources().getColor(accentColor));
        TextView percenttitle = findViewById(R.id.percenttitle);
        percenttitle.setTextColor(getResources().getColor(R.color.grey));



    }
    private void themeRead(){
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        theme = prefs.getInt("theme",1);
        setTheme();
    }
}
