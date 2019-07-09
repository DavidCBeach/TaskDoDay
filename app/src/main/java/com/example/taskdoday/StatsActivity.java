package com.example.taskdoday;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class StatsActivity extends AppCompatActivity {
    FeedReaderDbHelper dbHelper;
    Calendar calendar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        setTitle("Statistics");
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        calendar = Calendar.getInstance();
        percentRead();


    }
    private void percentRead(){

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM");
        String month =  mdformat.format(calendar.getTime());
        int imonth = Integer.parseInt(month);
        SimpleDateFormat dmdformat = new SimpleDateFormat("dd");
        String day =  dmdformat.format(calendar.getTime());
        int iday = Integer.parseInt(day);
        SimpleDateFormat ymdformat = new SimpleDateFormat("yyyy");
        String year =  ymdformat.format(calendar.getTime());
        int iyear= Integer.parseInt(year);

        calendar.set(iyear,imonth,iday,0,0);



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
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " < ?";
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
        }



        cursor.close();

        db.close();

    }
}