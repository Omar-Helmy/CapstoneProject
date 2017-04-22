package com.omar.capstoneproject.ui;


import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.omar.capstoneproject.R;
import com.omar.capstoneproject.data.DataContract;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class SecondFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private Context context;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private final int ID_LOADER = 2;

    public SecondFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentLayout = inflater.inflate(R.layout.fragment_second, container, false);
        recyclerView = (RecyclerView) fragmentLayout.findViewById(R.id.recycle_view_orders);

        /* setup recycle view with adapter */
        recyclerAdapter= new RecyclerAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);


        return fragmentLayout;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // creates new loader with cursor carries the required data based on id (salty or sweet)
        String[] projection = {DataContract.COLUMN_NAME, DataContract.COLUMN_TS, DataContract.COLUMN_PRICE, DataContract.COLUMN_ORDER};
        String selection = DataContract.COLUMN_FAVORITE+" == 1";
        switch (id){
            case ID_LOADER:
                return new CursorLoader(context, DataContract.DATA_URI, projection, selection, null, null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // update cursors of adapters
        switch (loader.getId()){
            case ID_LOADER:
                recyclerAdapter.updateCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(ID_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    /////////////////////////////// Recycle Adapter /////////////////////////////////
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private Cursor cursor;

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ((NormalViewHolder) holder).setupData();

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new NormalViewHolder(inflater.inflate(R.layout.card_item_order, parent, false));
        }


        @Override
        public int getItemCount() {
            if ( null == cursor ) return 0;
            // items = cursor count
            return cursor.getCount();
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        /* this method not overridden */
        public void updateCursor(Cursor c){
            cursor = c;
            notifyDataSetChanged();
        }

        public class NormalViewHolder extends RecyclerView.ViewHolder{

            private final TextView nameText, timeText, priceText, countText; // must be final


            public NormalViewHolder(View itemView) {
                super(itemView);    // pass view to super class
                /* attach views */
                nameText = (TextView) itemView.findViewById(R.id.order_name);
                timeText = (TextView) itemView.findViewById(R.id.order_time);
                priceText = (TextView) itemView.findViewById(R.id.order_price);
                countText = (TextView) itemView.findViewById(R.id.order_count);

                /* on click listener on the whole view */
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // Delete Dialog:
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Remove Order")
                                .setMessage("Are you sure ?")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ContentValues cv = new ContentValues();
                                        cv.put(DataContract.COLUMN_FAVORITE, 0);
                                        cursor.moveToPosition(getAdapterPosition());
                                        context.getContentResolver().update(
                                                DataContract.appendToUri(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME))),
                                                cv,null,null);

                                    }
                                })
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // User cancelled the dialog
                                    }
                                });
                        builder.create().show();
                        return true;
                    }
                });
            }

            public void setupData(){
                cursor.moveToPosition(getAdapterPosition());
                nameText.setText(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME)));
                timeText.setText(DateFormat.format("hh:mm a - dd/MM/yyyy",
                        Long.parseLong(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_TS)))));
                priceText.setText(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_PRICE)));
                countText.setText("Orders: "+cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_ORDER)));
            }
        }
    }

}
