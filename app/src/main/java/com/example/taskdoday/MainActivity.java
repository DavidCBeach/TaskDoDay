package com.example.taskdoday;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private EditText et;
    private Button bt;
    private TextView date;
    private Calendar calendar;

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

        // specify an adapter (see also next example)

        et = findViewById(R.id.et);
        et.setVisibility(View.GONE);
        bt = findViewById(R.id.addtask);
        bt.setVisibility(View.GONE);
        calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd");
        String strDate =  mdformat.format(calendar.getTime());
        date = findViewById(R.id.date);
        date.setText(strDate);


        ArrayList<String> myDataset = Read();
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + myDataset);
        mAdapter = new MyAdapter(getApplicationContext(),myDataset);
        recyclerView.setAdapter(mAdapter);


        Context context = getApplicationContext();
        CharSequence text = calendar.getTime().toString();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        setSwipes();

// Create a new map of values, where column names are the keys



    }
    private void SwipeRight() {
        Toast.makeText(getApplicationContext(), "right", Toast.LENGTH_SHORT).show();
        calendar.add(5,-1);
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd");
        String setDate =  mdformat.format(calendar.getTime());
        date.setText(setDate);
        refreshTasks();
    }
    private void SwipeLeft()  {
        Toast.makeText(getApplicationContext(), "left", Toast.LENGTH_SHORT).show();
        calendar.add(5,1);
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd");
        String setDate =  mdformat.format(calendar.getTime());
        date.setText(setDate);
        refreshTasks();

    }

    private ArrayList<String> Read(){

        SQLiteDatabase db = dbHelper.getReadableDatabase();

// Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT
        };

// Filter results WHERE "title" = 'My Title'
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_DATE + " = ?";
        //String[] selectionArgs = { "My Title" };
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
        String strDate =  mdformat.format(calendar.getTime());
        String[] selectionArgs = {strDate};

// How you want the results sorted in the resulting Cursor
        String sortOrder = FeedReaderContract.FeedEntry._ID + " DESC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
        String contents = new String();
        ArrayList<String> listCon = new ArrayList();
        while(cursor.moveToNext()) {
            String content = cursor.getString(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT));
            contents = contents + content;
            listCon.add(content);

        }
        final TextView helloTextView = (TextView) findViewById(R.id.taskshow);
        //helloTextView.setText(contents);
        cursor.close();
        Context context = getApplicationContext();
        CharSequence text = contents;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        return listCon;

    }
    private void Update() {
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//
//// New value for one column
//        String title = "MyNewTitle";
//        ContentValues values = new ContentValues();
//        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title);
//
//// Which row to update, based on the title
//        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
//        String[] selectionArgs = { "MyOldTitle" };
//
//        int count = db.update(
//                FeedReaderContract.FeedEntry.TABLE_NAME,
//                values,
//                selection,
//                selectionArgs);

    }
    private void Write(String content) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        SimpleDateFormat mdformat = new SimpleDateFormat("MM/dd/yyyy");
        String strDate =  mdformat.format(calendar.getTime());
        String date = strDate;
        String status = "0";
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_CONTENT , content);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, status);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_DATE, date);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);
        Context context = getApplicationContext();
        CharSequence text = Long.toString(newRowId);
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        Read();
    }

    public void addthing(View view) {
        Button atton = findViewById(R.id.atton);
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + atton.getText().toString());
        if(atton.getText().toString().equals("add thing")){
            et.setVisibility(View.VISIBLE);
            bt.setVisibility(View.VISIBLE);

            atton.setText("Done");
            et.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);

        } else {
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
        et.setText("");
        Write(taskContent);

        refreshTasks();

    }
    private void refreshTasks(){
        ArrayList<String> myDataset = Read();
        System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ" + myDataset);
        mAdapter = new MyAdapter(getApplicationContext(),myDataset);
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
