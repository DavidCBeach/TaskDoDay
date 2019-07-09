package com.example.taskdoday;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
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


    FeedReaderDbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new FeedReaderDbHelper(getApplicationContext());
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
        et.setVisibility(View.GONE);
        done = findViewById(R.id.done);
        done.setVisibility(View.GONE);
        bt = findViewById(R.id.addtask);
        bt.setVisibility(View.GONE);
        calendar = Calendar.getInstance();
        calendarDialog = new CalendarDialog();
        sortDialog = new SortDialog();
        View old = findViewById(R.id.oldfilter);
        old.setVisibility(View.GONE);
        sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " ASC," + FeedReaderContract.FeedEntry._ID + " DESC";
        LinearLayout deleteinterface = findViewById(R.id.deleteinterface);
        deleteinterface.setVisibility(View.GONE);


        setDate();

        refreshTasks();


        setSwipes();

        // Create a new map of values, where column names are the keys



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
        } else {
            setIsNotOld();
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


        getMenuInflater().inflate(menu_main, menu);



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
                calendarDialog.setArguements(calendar);
                calendarDialog.show(fm, "photo");
                return true;
            case R.id.stats_show:
                startActivity(new Intent(this, StatsActivity.class));
                return true;
            case R.id.sort:
                sortDialog.show(fm, "sort");
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.delete:
                if(!getIsOld()) {
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
                    mAdapter = new MyAdapter(getApplicationContext(), myDataset, myStatus, myID, getIsOld(), true);
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
        } else {
            setIsNotOld();
        }
        refreshTasks();
        }
    }
    private void setIsOld(){
        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.GONE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.GONE);
        View old = findViewById(R.id.oldfilter);
        old.setVisibility(View.VISIBLE);
    }
    private void setIsNotOld(){
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
            } else {
                setIsNotOld();
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
        //the below 3 lines is for backlogging 1 week of task for testing purposes
//        calendar.add(5,-7);
//        String strDate =  mdformat.format(calendar.getTime());
//        calendar.add(5,7);

        String strDate =  mdformat.format(calendar.getTime());
        String date = strDate;
        Integer status = 0;
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT , content);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, status);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, date);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE_MILLI, calendar.getTimeInMillis() );

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
        mAdapter = new MyAdapter(getApplicationContext(),myDataset,myStatus,myID,getIsOld(),false);
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
//        final ViewGroup transitionsContainer = (ViewGroup) view.findViewById(R.id.transitions_container2);
//        TransitionManager.beginDelayedTransition(transitionsContainer);
        Button atton = findViewById(R.id.atton);
        atton.setVisibility(View.VISIBLE);
        Button attonu = findViewById(R.id.attonu);
        attonu.setVisibility(View.VISIBLE);
    }
}
