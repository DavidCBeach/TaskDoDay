package com.example.taskdoday;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;

import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.example.taskdoday.R.menu.menu_main;
import static com.example.taskdoday.R.menu.menu_main2;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private EditText et;
    private Button bt;
    private Button done;
    private TextView date;
    private Calendar calendar;
    private ArrayList<Boolean> myStatus;
    private ArrayList<String> myID;
    private CalendarDialog calendarDialog;
    private SortDialog sortDialog;
    private FragmentManager fm = getSupportFragmentManager();
    CalendarView cv ;
    private GregorianCalendar Gcalendar;
    private String sortOrder;
    private int lastOld;
    private int theme;
    private int primaryColor;
    private int accentColor;
    private int buttonBackColor;
    private boolean darkMode;



    FeedReaderDbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        calendar = Calendar.getInstance();

        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            setInit();
            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("firstrun", false).apply();
        }

        themeRead();

        // Gets the data repository in write mode
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myID = new ArrayList<>();
        myStatus = new ArrayList<>();
        // specify an adapter (see also next example)

        et = findViewById(R.id.et);
        //et.setVisibility(View.GONE);
        done = findViewById(R.id.done);
        //done.setVisibility(View.GONE);
        bt = findViewById(R.id.addtask);
       // bt.setVisibility(View.GONE);

        calendarDialog = new CalendarDialog();
        sortDialog = new SortDialog();
        View old = findViewById(R.id.oldfilter);
        //old.setVisibility(View.GONE);
        sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " ASC," + FeedReaderContract.FeedEntry._ID + " DESC";
        LinearLayout deleteinterface = findViewById(R.id.deleteinterface);
        //deleteinterface.setVisibility(View.GONE);
        lastOld = 0;


        setDate();
        refreshTasks();
        setSwipes();
        createNotificationChannel();
        notificationSetup();





        // Create a new map of values, where column names are the keys



    }
    public void notificationSetup(){

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS
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
        String status = new String();
        while(cursor.moveToNext()) {
            time = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE));
            status = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS));


        }
        cursor.close();
        db.close();
        if(status == "reminder") {


            String[] timespit = time.split(":");
            int hour = Integer.parseInt(timespit[0]);
            int minute = Integer.parseInt(timespit[1]);
            Calendar tempcal = Calendar.getInstance();
            tempcal.set(Calendar.HOUR, hour);
            tempcal.set(Calendar.MINUTE, minute);
            tempcal.add(Calendar.DAY_OF_MONTH, -1);
            System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDD3");
            System.out.println(tempcal.getTime());

            Intent notifyIntent = new Intent(this, MyReceiver.class);
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
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, tempcal.getTimeInMillis(),
                    milli, pendingIntent);
        } else {
            Intent notifyIntent = new Intent(this, MyReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast
                    (getApplicationContext(), 3, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
        }

    }


    public void goToDate(View view) {
        if(bt.getVisibility()==View.VISIBLE  || findViewById(R.id.deleteinterface).getVisibility() == View.VISIBLE){
            findViewById(R.id.deleteinterface).setVisibility(View.GONE);
            done(view);
        }
        calendar.setTimeInMillis(calendarDialog.getGcalendar().getTimeInMillis());
        System.out.println(calendar.getTime());

        setDate();
        if(getIsOld()){
            setIsOld();
            lastOld = 1;
        } else {
            setIsNotOld();
            lastOld = 0;
        }


        refreshTasks();



        calendarDialog.dismisser();
    }

    public void setSortCompleted(View view){
        sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " ASC," + FeedReaderContract.FeedEntry._ID + " DESC";
        refreshTasks();
        sortDialog.dismisser();
    }
    public void setSortTime(View view){
        sortOrder = FeedReaderContract.FeedEntry._ID + " DESC";
        refreshTasks();
        sortDialog.dismisser();
    }
    public void setSortName(View view){
        sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT + " ASC";
        refreshTasks();
        sortDialog.dismisser();
    }

    public void CancelDate(View view) {

        calendarDialog.dismisser();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Add items to action bar

        if(theme == 0){
            getMenuInflater().inflate(menu_main, menu);
        } else if (theme == 1){
            getMenuInflater().inflate(menu_main2, menu);
        } else {
            getMenuInflater().inflate(menu_main, menu);
        }


        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Is item is selected, if so do correlated action
        switch(item.getItemId())
        {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.calendar_show:
                calendarDialog.setArguements(calendar,accentColor,buttonBackColor);
                calendarDialog.show(fm, "photo");
                return true;
            case R.id.stats_show:
                startActivity(new Intent(this, StatsActivity.class));
                return true;
            case R.id.sort:
                sortDialog.setAttributes(primaryColor,buttonBackColor);
                sortDialog.show(fm, "sort");


                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.delete:
                if(!getIsOld() && (et.getVisibility() == View.GONE)) {
                    SlideAnimationUtil.slideInFromBottom(getApplicationContext(), findViewById(R.id.atton));
                    SlideAnimationUtil.slideInFromBottom(getApplicationContext(), findViewById(R.id.attonu));
                    ArrayList<String> myDataset = Read();
                    if (!myDataset.isEmpty()) {
                        TextView notask = findViewById(R.id.notasks);
                        notask.setVisibility(View.GONE);
                        RelativeLayout notaskl = findViewById(R.id.notaskslayout);
                        notaskl.setVisibility(View.GONE);
                    } else {
                        TextView notask = findViewById(R.id.notasks);
                        notask.setVisibility(View.VISIBLE);
                        RelativeLayout notaskl = findViewById(R.id.notaskslayout);
                        notaskl.setVisibility(View.VISIBLE);
                    }
                    System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + myDataset);
                    mAdapter = new MyAdapter(getApplicationContext(), myDataset, myStatus, myID, getIsOld(), true, darkMode);
                    recyclerView.setAdapter(mAdapter);
                    LinearLayout deleteinterface = findViewById(R.id.deleteinterface);
                    deleteinterface.setVisibility(View.VISIBLE);
                    Button atton = findViewById(R.id.atton);
                    atton.setVisibility(View.GONE);
                    Button attonu = findViewById(R.id.attonu);
                    attonu.setVisibility(View.GONE);
                }
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
    public void cancelDelete(View view){

        LinearLayout deleteinterface = findViewById(R.id.deleteinterface);
        deleteinterface.setVisibility(View.GONE);
        SlideAnimationUtil.slideInFromTopSlow(getApplicationContext(), findViewById(R.id.atton));
        SlideAnimationUtil.slideInFromTopSlow(getApplicationContext(), findViewById(R.id.attonu));
        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.VISIBLE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.VISIBLE);
        refreshTasks();
    }
    public void delete(View view){
        LinearLayout deleteinterface = findViewById(R.id.deleteinterface);
        deleteinterface.setVisibility(View.GONE);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<String> deletables = mAdapter.getDeletables();
        String deleteList = new String();
        for(int i = 0; i < deletables.size(); i++){
            deleteList = deleteList + deletables.get(i) + ",";
        }
        System.out.println("ZZZZZZZZZZZZZZ" + deletables);
        System.out.println("ZZZZZZZZZZZZZZ" + deleteList.substring(0,deleteList.length()-1));
        db.execSQL("delete from "+FeedReaderContract.FeedEntry.TABLE_NAME+" where "+FeedReaderContract.FeedEntry._ID+" in ("+deleteList.substring(0,deleteList.length()-1)+")");
        db.close();
        SlideAnimationUtil.slideInFromTopSlow(getApplicationContext(), findViewById(R.id.atton));
        SlideAnimationUtil.slideInFromTopSlow(getApplicationContext(), findViewById(R.id.attonu));
        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.VISIBLE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.VISIBLE);
        refreshTasks();
    }

    private void setDate(){

        SimpleDateFormat mdformat = new SimpleDateFormat("E, MM/dd");
        String strDate =  mdformat.format(calendar.getTime());
        Calendar today = Calendar.getInstance();
        String stoday= mdformat.format(today.getTime());
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(5,1);
        String stomorrow = mdformat.format(tomorrow.getTime());
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(5,-1);
        String syesterday = mdformat.format(yesterday.getTime());
        if(stoday.equals(strDate)){
            strDate = "Today";
        } else if(stomorrow.equals(strDate)) {
            strDate = "Tomorrow";
        } else if(syesterday.equals(strDate)){
            strDate = "Yesterday";
        }
        setTitle(strDate);
        System.out.println(this.getCurrentFocus());
    }

    private void SwipeRight() {
        if(bt.getVisibility()==View.GONE && findViewById(R.id.deleteinterface).getVisibility() == View.GONE){

            SlideAnimationUtil.slideInFromLeft(getApplicationContext(), findViewById(R.id.transitions_container));
        calendar.add(5,-1);
        setDate();
        if(getIsOld()){
            setIsOld();
            lastOld = 1;
        } else {
            setIsNotOld();
            lastOld = 0;
        }
        refreshTasks();
        }
    }
    private void setIsOld(){
        if(lastOld == 0){
            SlideAnimationUtil.slideInFromBottom(getApplicationContext(), findViewById(R.id.atton));
        }

        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.GONE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.GONE);
        View old = findViewById(R.id.oldfilter);
        old.setVisibility(View.VISIBLE);

    }
    private void setIsNotOld(){
        if(lastOld == 1){
            SlideAnimationUtil.slideInFromTop(getApplicationContext(), findViewById(R.id.atton));
            SlideAnimationUtil.slideInFromTop(getApplicationContext(), findViewById(R.id.attonu));
        }
        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.VISIBLE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.VISIBLE);

        View old = findViewById(R.id.oldfilter);
        old.setVisibility(View.GONE);

    }
    private void SwipeLeft()  {

        if(bt.getVisibility()==View.GONE && findViewById(R.id.deleteinterface).getVisibility() == View.GONE){
            SlideAnimationUtil.slideInFromRight(getApplicationContext(), findViewById(R.id.transitions_container));

            calendar.add(5,1);
            setDate();
            refreshTasks();
            if(getIsOld()){
                setIsOld();
                lastOld = 1;
            } else {
                setIsNotOld();
                lastOld = 0;
            }
        }


    }

    private ArrayList<String> Read(){

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
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = ?";
        //String[] selectionArgs = { "My Title" };
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
        String strDate =  mdformat.format(calendar.getTime());
        String[] selectionArgs = {strDate};

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
        myID = new ArrayList<>();
        myStatus = new ArrayList<>();
        String contents = new String();
        ArrayList<String> listCon = new ArrayList();
        while(cursor.moveToNext()) {
            String content = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT));
            contents = contents + content;
            listCon.add(content);
            String contentid = cursor.getString(
                    cursor.getColumnIndexOrThrow(BaseColumns._ID));
            myID.add(contentid);
            Integer conten = cursor.getInt(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS));
            if(conten == 1){
                myStatus.add(true);
            } else {
                myStatus.add(false);
            }

        }
        while(cursor.moveToNext()) {

        }
        final TextView helloTextView = (TextView) findViewById(R.id.taskshow);
        //helloTextView.setText(contents);
        cursor.close();
        db.close();

        return listCon;

    }

    private void Write(String content) {
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
        db.close();
        Read();
    }

    public void addthing(View view) {
        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.GONE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.GONE);
        et.setVisibility(View.VISIBLE);
        bt.setVisibility(View.VISIBLE);
        done.setVisibility(View.VISIBLE);

        et.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);




    }

    public void addTask(View view) {
        String taskContent = et.getText().toString();
        if(!et.getText().toString().replace(" ","").isEmpty()){
            et.setText("");
            Write(taskContent);
            refreshTasks();
        }
    }
    private boolean getIsOld(){
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(5,-1);
        if(calendar.before(yesterday)){
            return true;
        }
        return false;
    }
    private void refreshTasks(){
        ArrayList<String> myDataset = Read();
        if(!myDataset.isEmpty()){
            TextView notask = findViewById(R.id.notasks);
            notask.setVisibility(View.GONE);
            RelativeLayout notaskl = findViewById(R.id.notaskslayout);
            notaskl.setVisibility(View.GONE);
        } else {
            TextView notask = findViewById(R.id.notasks);
            notask.setVisibility(View.VISIBLE);
            RelativeLayout notaskl = findViewById(R.id.notaskslayout);
            notaskl.setVisibility(View.VISIBLE);
        }
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + myDataset);
        mAdapter = new MyAdapter(getApplicationContext(),myDataset,myStatus,myID,getIsOld(),false,darkMode);
        recyclerView.setAdapter(mAdapter);
    }
    private void setSwipes(){
        RelativeLayout rl = findViewById(R.id.base);
        rl.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {

            public void onSwipeRight() {
                SwipeRight();
            }
            public void onSwipeLeft() {
                SwipeLeft();
            }
        });

        recyclerView.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {

            public void onSwipeRight() {
                SwipeRight();
            }
            public void onSwipeLeft() {
                SwipeLeft();
            }
        });
    }

    public void done(View view) {
        et.setVisibility(View.GONE);
        bt.setVisibility(View.GONE);
        done.setVisibility(View.GONE);

        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

        SlideAnimationUtil.slideInFromTopSlow(getApplicationContext(), findViewById(R.id.atton));
        SlideAnimationUtil.slideInFromTopSlow(getApplicationContext(), findViewById(R.id.attonu));
        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.VISIBLE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.VISIBLE);
    }


    private void setInit(){




        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar tempcal = Calendar.getInstance();
        tempcal.setTimeInMillis(4718552490997L);
        String strDate =  mdformat.format(tempcal.getTime());
        String date = strDate;
        Integer status = 1;
        String content = "start";
        Long millicode = Long.getLong("4718552490997L");
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT , content);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, status);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE_MILLI, millicode );

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDD"+newRowId);

        values = new ContentValues();
        mdformat = new SimpleDateFormat("HH:mm");
        tempcal = Calendar.getInstance();
        tempcal.set(Calendar.HOUR, 10);
        tempcal.set(Calendar.MINUTE, 18);
        tempcal.add(Calendar.MINUTE, 1);
        System.out.println(tempcal.getTime());
        strDate =  mdformat.format(tempcal.getTime());
        System.out.println(strDate);
        date = strDate;
        status = -1;
        content = "reminder";
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT , content);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, status);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE_MILLI, millicode );
// Insert the new row, returning the primary key value of the new row
        newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDD"+newRowId);
        db.close();

    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("1338", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void setTheme(){
        if(theme == 0){
            setTheme( R.style.AppTheme);
            buttonBackColor =R.color.white;
            primaryColor=R.color.colorPrimary;
            accentColor=R.color.colorAccent;
            darkMode = false;
        } else if(theme == 1){
            setTheme( R.style.AppTheme2);
            buttonBackColor =R.color.white;
            primaryColor=R.color.grey2;
            accentColor=R.color.colorAccent;
            darkMode = false;
        } else {
            setTheme( R.style.AppTheme3);
            buttonBackColor =R.color.colorDarkPrimary;
            primaryColor=R.color.white;
            accentColor=R.color.colorDarkAccent;


            darkMode = true;



        }

        setContentView(R.layout.activity_main);
        if(theme == 2){
            Button addthing = findViewById(R.id.atton);
            addthing.setBackgroundResource(R.drawable.ic_add_circle50_yellow);
            Button addthingu = findViewById(R.id.attonu);
            addthingu.setBackgroundResource(R.drawable.blackcircle);
        }

        done = findViewById(R.id.done);
        done.setTextColor(getResources().getColor(primaryColor));
        done.setBackgroundColor(getResources().getColor(buttonBackColor));
        Button addtask = findViewById(R.id.addtask);
        addtask.setTextColor(getResources().getColor(accentColor));
        addtask.setBackgroundColor(getResources().getColor(buttonBackColor));
        LinearLayout addtasklayout = findViewById(R.id.addtasklayout);
        addtasklayout.setBackgroundColor(getResources().getColor(buttonBackColor));

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
        setTheme();
    }
}
