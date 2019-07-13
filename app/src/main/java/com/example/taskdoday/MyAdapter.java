package com.example.taskdoday;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Paint;
import android.provider.BaseColumns;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>  {
    private ArrayList<String> mDataset;
    private ArrayList<Boolean> mStatus;
    private ArrayList<String> mID;
    private int mPosition;
    private Context mContext;
    private boolean mIsOld;
    private boolean mDeleteMode;
    private ArrayList<String> deletables;
    private boolean mDarkMode;



    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public RelativeLayout parentView;
        public CheckBox checkView;
        public CheckBox deletecheckView;
        public View divider;
        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.content);
            parentView = v.findViewById(R.id.taskLayout);
            checkView = v.findViewById(R.id.check);
            deletecheckView = v.findViewById(R.id.deletecheck);
            divider = v.findViewById(R.id.divider);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context, ArrayList<String> myDataset, ArrayList<Boolean> myStatus, ArrayList<String> myID,boolean isOld, boolean deleteMode,boolean darkmode) {
        mDataset = myDataset;
        mStatus = myStatus;
        mID = myID;
        mContext = context;
        mIsOld = isOld;
        mDeleteMode = deleteMode;
        deletables = new ArrayList<>();
        mDarkMode = darkmode;

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

        String textColor = "#5A5A5A";
        String  textColorAlt = "#DFDFDF";
        if(mDarkMode){
            textColorAlt = "#5A5A5A";
            textColor = "#DFDFDF";
            ColorStateList darkStateList = ContextCompat.getColorStateList(mContext, R.color.colorDarkAccent);
            CompoundButtonCompat.setButtonTintList(holder.checkView, darkStateList);
            holder.divider.setBackgroundColor(Color.parseColor("#707070"));
        }
        holder.checkView.setText(mDataset.get(position));
        if(!mDeleteMode){
            holder.deletecheckView.setVisibility(View.GONE);
        }

        if(mStatus.get(position)){
            holder.checkView.setChecked(true);
            holder.checkView.setTextColor(Color.parseColor(textColorAlt));
            holder.checkView.setPaintFlags(holder.checkView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.checkView.setChecked(false);
            holder.checkView.setPaintFlags( holder.checkView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            holder.checkView.setTextColor(Color.parseColor(textColor));

        }

            holder.checkView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view){
                    if(mStatus.get(position)){
                        if(mIsOld){
                            holder.checkView.setChecked(true);
                            return;
                        }
                        holder.checkView.setChecked(false);
                        Update(mID.get(position), 0);
                        mStatus.set(position,false);
                        if(mDarkMode){
                            holder.checkView.setTextColor(Color.parseColor("#DFDFDF"));
                        }
                        else {
                            holder.checkView.setTextColor(Color.parseColor("#5A5A5A"));
                        }

                        holder.checkView.setPaintFlags( holder.textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                    } else {
                        if(mIsOld){
                            holder.checkView.setChecked(false);
                            return;
                        }
                        holder.checkView.setChecked(true);
                        Update(mID.get(position), 1);
                        mStatus.set(position,true);
                        if(mDarkMode){
                            holder.checkView.setTextColor(Color.parseColor("#5A5A5A"));
                        }
                        else {
                            holder.checkView.setTextColor(Color.parseColor("#DFDFDF"));
                        }

                        holder.checkView.setPaintFlags(holder.textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                }
            });
            holder.deletecheckView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view){

                    if(deletables.isEmpty()){
                        holder.deletecheckView.setChecked(true);
                        deletables.add(mID.get(position));
                        holder.parentView.setBackgroundColor(Color.parseColor("#ff0000"));
                        holder.checkView.setTextColor(Color.parseColor("#ffffff"));
                    } else if(!deletables.contains(mID.get(position))){
                        holder.deletecheckView.setChecked(true);
                        deletables.add(mID.get(position));
                        holder.parentView.setBackgroundColor(Color.parseColor("#ff0000"));
                        holder.checkView.setTextColor(Color.parseColor("#ffffff"));

                    } else {
                        holder.deletecheckView.setChecked(false);
                        deletables.remove(deletables.indexOf(mID.get(position)));
                        if(mDarkMode){
                            holder.parentView.setBackgroundColor(Color.parseColor("#313131"));
                            holder.checkView.setTextColor(Color.parseColor("#DFDFDF"));
                        } else {
                            holder.parentView.setBackgroundColor(Color.parseColor("#ffffff"));
                            holder.checkView.setTextColor(Color.parseColor("#5A5A5A"));
                        }
                    }
                    System.out.println(deletables);

                }
            });



    }
    public ArrayList<String> getDeletables(){
        return deletables;
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


