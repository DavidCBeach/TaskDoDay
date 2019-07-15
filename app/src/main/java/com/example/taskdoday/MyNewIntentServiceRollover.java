package com.example.taskdoday;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(getApplicationContext());
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
        Calendar calendar = Calendar.getInstance();
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
        mContents = new ArrayList<>();
        allcontent = new String();
        ArrayList<String> listCon = new ArrayList();
        while(cursor.moveToNext()) {
            String content = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT));
            allcontent = allcontent + content + "\n";
            mContents.add(content);

        }
        if(mContents.isEmpty()){
            return;
        }


        cursor.close();
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