package com.example.taskdoday;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.taskdoday.R.menu.menu_main;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private EditText et;
    private Button bt;
    private TextView date;
    private Calendar calendar;
    private ArrayList<Boolean> myStatus;
    private ArrayList<String> myID;



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
        bt = findViewById(R.id.addtask);
        bt.setVisibility(View.GONE);
        calendar = Calendar.getInstance();
        String smilli = getIntent().getStringExtra("calendar");


        if(smilli != null){
            Long milli = Long.decode(smilli);
            calendar.setTimeInMillis(milli);
        }
        setDate();

        refreshTasks();


        setSwipes();

        // Create a new map of values, where column names are the keys



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
                Intent intent = new Intent(getBaseContext(), CalendarActivity.class);
                intent.putExtra("calendar", Long.toString(calendar.getTimeInMillis()));
                startActivity(intent);

                return true;

        }
        return super.onOptionsItemSelected(item);

    }
    private void setDate(){
        SimpleDateFormat mdformat = new SimpleDateFormat("E, MM/dd");
        String strDate =  mdformat.format(calendar.getTime());
        date = findViewById(R.id.date);
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
        date.setText(strDate);
    }

    private void SwipeRight() {

        calendar.add(5,-1);
        setDate();
        if(getIsOld()){
            Button atton = findViewById(R.id.atton);
            atton.setVisibility(View.GONE);
        } else {
            Button atton = findViewById(R.id.atton);
            atton.setVisibility(View.VISIBLE);
        }
        refreshTasks();
    }
    private void SwipeLeft()  {

        calendar.add(5,1);
        setDate();
        refreshTasks();
        if(getIsOld()){
            Button atton = findViewById(R.id.atton);
            atton.setVisibility(View.GONE);
        } else {
            Button atton = findViewById(R.id.atton);
            atton.setVisibility(View.VISIBLE);
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
        String sortOrder = FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS + " ASC," + FeedReaderContract.FeedEntry._ID + " DESC";

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

        return listCon;

    }

    private void Write(String content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
        String strDate =  mdformat.format(calendar.getTime());
        String date = strDate;
        Integer status = 0;
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT , content);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, status);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, date);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

        Read();
    }

    public void addthing(View view) {
        Button atton = findViewById(R.id.atton);
        int margin = 14;
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + atton.getText().toString());
        if(atton.getText().toString().equals("add thing")){
            et.setVisibility(View.VISIBLE);
            bt.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    .475f
            );
            atton.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.colorPrimary));
            param.setMargins(margin,margin,margin,margin);
            atton.setLayoutParams(param);
            atton.setText("Done");
            et.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

        } else {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            );
            atton.setBackgroundColor(getApplicationContext().getResources().getColor(R.color.colorAccent));
            param.setMargins(margin,margin,margin,margin);
            atton.setLayoutParams(param);
            et.setVisibility(View.GONE);
            bt.setVisibility(View.GONE);
            atton.setText("add thing");


            InputMethodManager inputManager = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);

            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }


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

        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + myDataset);
        mAdapter = new MyAdapter(getApplicationContext(),myDataset,myStatus,myID,getIsOld());
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

}
