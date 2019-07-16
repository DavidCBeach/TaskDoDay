package com.example.taskdoday;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MyNewIntentServiceRollover extends IntentService {
    private static final int NOTIFICATION_ID = 3;
    private String allcontent;
    private ArrayList<String> mContents;
    Calendar calendar;
    FeedReaderDbHelper dbHelper;
    public MyNewIntentServiceRollover() {
        super("MyNewIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        startForeground(1338,buildForegroundNotification());

        Rollover();

        stopForeground(true);
        //notificationManager.notify(1, builder.build());

    }
    private void Rollover(){
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = ? AND " + FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " = ?";
        //String[] selectionArgs = { "My Title" };
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " ASC," + FeedReaderContract.FeedEntry._ID + " DESC";
        calendar = Calendar.getInstance();
        String strDate =  mdformat.format(calendar.getTime());
        String[] selectionArgs = {strDate, "0"};

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
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR,0);
        calendar.set(Calendar.MINUTE,1);
        while(cursor.moveToNext()) {
            String content = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT));
            rolloverWrite(content);
        }
        if(mContents.isEmpty()){
            return;
        }
        cursor.close();
        db.close();


    }
    private void rolloverWrite(String content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
        //the below 2 lines is for backlogging 1 week of task for testing purposes
//     calendar.add(5,-7);
//     String strDate =  mdformat.format(calendar.getTime());

        Long dateMilli = calendar.getTimeInMillis();
        String strDate =  mdformat.format(calendar.getTime());
        String date = strDate;
        Integer status = 0;
        //and this line
//              calendar.add(5,7);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT , content);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, status);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE_MILLI, dateMilli );
        System.out.println(calendar.getTimeInMillis());

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        System.out.println(newRowId);
        db.close();
    }

    private Notification buildForegroundNotification() {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this);

        b.setOngoing(true)
                .setContentTitle("title")
                .setContentText("filename")
                .setSmallIcon(android.R.drawable.checkbox_on_background)
                .setTicker("filename");

        return(b.build());
    }
}