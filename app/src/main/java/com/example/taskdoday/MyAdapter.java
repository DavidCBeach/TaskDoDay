package com.example.taskdoday;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.provider.BaseColumns;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements View.OnLongClickListener {
    private ArrayList<String> mDataset;
    private ArrayList<Boolean> mStatus;
    private ArrayList<String> mID;
    private int mPosition;
    private Context mContext;
    private boolean mIsOld;

    @Override
    public boolean onLongClick(View v) {
        Context context = mContext;
        CharSequence text = mDataset.get(mPosition);
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();





        return false;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public RelativeLayout parentView;
        public ImageView checkView;
        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.content);
            parentView = v.findViewById(R.id.taskLayout);
            checkView = v.findViewById(R.id.check);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context, ArrayList<String> myDataset, ArrayList<Boolean> myStatus, ArrayList<String> myID,boolean isOld) {
        mDataset = myDataset;
        mStatus = myStatus;
        mID = myID;
        mContext = context;
        mIsOld = isOld;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_text_view,parent,false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;

    }
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        mPosition = position;
        Log.d(TAG, "taco");
        holder.textView.setText(mDataset.get(position));
        if(mStatus.get(position)){
            holder.checkView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check_box_black_24dp));
        } else {
            holder.checkView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check_box_outline_blank_black_24dp));
        }
        if(!mIsOld){
            holder.checkView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view){
                    if(mStatus.get(position)){
                        holder.checkView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check_box_outline_blank_black_24dp));
                        Update(mID.get(position), 0);
                    } else {
                        holder.checkView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_check_box_black_24dp));
                        Update(mID.get(position), 1);
                    }

                }
            });
        }


    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    private void Update(String id, Integer status) {
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

// New value for one column

        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_STATUS, status);

// Which row to update, based on the title
        String selection = BaseColumns._ID + " LIKE ?";
        String[] selectionArgs = { id };

        int count = db.update(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

    }

}


