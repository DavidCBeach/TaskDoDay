package com.taskdoday.taskdoday;

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
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.taskdoday.taskdoday.R.menu.menu_main;
import static com.taskdoday.taskdoday.R.menu.menu_main2;

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
    private DatabaseUtil dbUtil;



    FeedReaderDbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
        calendar = Calendar.getInstance();

        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            setInit();
            try {
                notificationSetup();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            createNotificationChannel();
            startActivity(new Intent(this, StartActivity.class));
            prefs.edit().putBoolean("firstrun", false).apply();
        }
        themeRead();
        dbUtil = new DatabaseUtil(getApplicationContext());

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        myID = new ArrayList<>();
        myStatus = new ArrayList<>();


        et = findViewById(R.id.et);
        done = findViewById(R.id.done);
        bt = findViewById(R.id.addtask);


        calendarDialog = new CalendarDialog();
        sortDialog = new SortDialog();

        sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " ASC," + FeedReaderContract.FeedEntry._ID + " DESC";

        lastOld = 0;

        setDate();
        refreshTasks();
        setSwipes();

    }



    public void notificationSetup() throws JSONException {

        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        String notification = prefs.getString("notification","");
        JSONObject obj = new JSONObject(notification);
        Boolean status = obj.getBoolean("status");
        if(status) {
            int hour = obj.getInt("hour");
            int minute = obj.getInt("minute");
            SimpleDateFormat mdformat = new SimpleDateFormat("HH");
            Calendar actualTime = Calendar.getInstance();
            String hourActual =  mdformat.format(actualTime.getTime());
            int ihourActual = Integer.valueOf(hourActual);
            if(ihourActual > 12){
                hour=(hour +12)%24;
            }
            Calendar tempcal = Calendar.getInstance();
            tempcal.set(Calendar.HOUR, hour);
            tempcal.set(Calendar.MINUTE, minute);
            tempcal.add(Calendar.DAY_OF_MONTH, -1);

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
        if(!deleteList.isEmpty())
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

        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT,
                FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS,
                FeedReaderContract.FeedEntry.COLUMN_NAME_DATE
        };

        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = ?";
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
        String strDate =  mdformat.format(calendar.getTime());
        String[] selectionArgs = {strDate};



        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
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
        rollover();
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
        mAdapter = new MyAdapter(getApplicationContext(),myDataset,myStatus,myID,getIsOld(),false,darkMode);
        recyclerView.setAdapter(mAdapter);
    }

    private void rollover() {
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        boolean rollover = prefs.getBoolean("rollover",false);
        if(rollover){
            String latestdate = prefs.getString("latestdate","");
            Calendar tempcal = Calendar.getInstance();
            tempcal.set(Calendar.HOUR,0);
            tempcal.set(Calendar.MINUTE,1);
            SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
            String today =  mdformat.format(tempcal.getTime());
            if(!today.equals(latestdate)){
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("latestdate", today );
                editor.commit();
                tempcal.add(Calendar.DAY_OF_MONTH, -1);
                dbUtil.Rollover(tempcal);
            }

        }

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
    //hides add task layout on done button click
    public void done(View view) {
        et.setVisibility(View.GONE);
        bt.setVisibility(View.GONE);
        done.setVisibility(View.GONE);

//        InputMethodManager inputManager = (InputMethodManager)
//                getSystemService(Context.INPUT_METHOD_SERVICE);

//        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
//                InputMethodManager.HIDE_NOT_ALWAYS);
        InputMethodManager manager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(this.findViewById(android.R.id.content).getWindowToken(), 0);
        }

        SlideAnimationUtil.slideInFromTopSlow(getApplicationContext(), findViewById(R.id.atton));
        SlideAnimationUtil.slideInFromTopSlow(getApplicationContext(), findViewById(R.id.attonu));
        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.VISIBLE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.VISIBLE);
    }


    private void setInit(){
        //Setting initial theme settings to preferences
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("theme", 1);
        editor.commit();

        //Setting initial notification settings to preferences
        JSONObject jo = new JSONObject();
        Calendar tempcal = Calendar.getInstance();
        tempcal.add(Calendar.DAY_OF_MONTH, -1);
        tempcal.set(Calendar.HOUR,5);
        tempcal.set(Calendar.MINUTE, 17);
        SimpleDateFormat mdformat = new SimpleDateFormat("HH");
        String hour =  mdformat.format(tempcal.getTime());
        int ihour = Integer.parseInt(hour);
        mdformat = new SimpleDateFormat("mm");
        String minute =  mdformat.format(tempcal.getTime());
        int iminute = Integer.parseInt(minute);

        try {
            jo.put("hour",ihour);
            jo.put("minute",iminute);
            jo.put("status",true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String date = jo.toString();
        editor.putString("notification", date);
        editor.commit();

        //setting initial rollover state
        editor.putBoolean("rollover", false);
        editor.commit();
        tempcal = Calendar.getInstance();
        mdformat = new SimpleDateFormat("MM/dd/yyyy");
        String latestdate =  mdformat.format(tempcal.getTime());
        editor.putString("latestdate", latestdate );
        editor.commit();

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
        SharedPreferences prefs = getSharedPreferences("com.exmample.taskdoday", MODE_PRIVATE);
        theme = prefs.getInt("theme",1);
        setTheme();
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
}
