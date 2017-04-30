package com.omar.capstoneproject.ui;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.TimeUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.omar.capstoneproject.R;
import com.omar.capstoneproject.data.DataContract;

import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Cursor cursor;
    private ImageView imageView;
    private CollapsingToolbarLayout collapsingToolbar;
    private RecyclerAdapter recyclerAdapter;
    private RecyclerView recyclerView;
    private static FloatingActionButton fab;
    private static String NAME;
    private StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        /* attach views */
        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        imageView = (ImageView) findViewById(R.id.toolbar_parallax_image);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.detail_recycle_view);
        fab = (FloatingActionButton) findViewById(R.id.order_fab);

        /* Firebase Storage Ref */
        storageRef = FirebaseStorage.getInstance().getReference("Menu/Images/");

        /* setup tool bar */
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* get intent to get data from cursor */
        NAME = getIntent().getStringExtra(DataContract.COLUMN_NAME);
        cursor = getContentResolver().query(DataContract.appendToUri(NAME), null, null, null, null);
        cursor.moveToFirst();

        /* setup collapse tool bar with title and image*/
        collapsingToolbar.setTitle(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME)));
        Glide.with(this).using(new FirebaseImageLoader())
                .load(storageRef.child(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_IMAGE))))
                .centerCrop().crossFade().into(imageView);
        imageView.setContentDescription(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME)));

        /* setup recycle view */
        recyclerAdapter= new RecyclerAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerAdapter.updateCursor(cursor);

        /* update fab drawable */
        fab.setImageResource(cursor.getInt(cursor.getColumnIndex(DataContract.COLUMN_FAVORITE))==1 ?
                R.drawable.ic_done_white_48dp : R.drawable.ic_add_shopping_cart_white_48dp);

        /* order food */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cursor.getInt(cursor.getColumnIndex(DataContract.COLUMN_FAVORITE))==0){
                    // create dialog to get #of orders
                    OrderDialog orderDialog = new OrderDialog();
                    orderDialog.show(getSupportFragmentManager(), OrderDialog.class.getSimpleName());
                }else{
                    Snackbar.make(fab, R.string.add_snackbar, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    /////////////////////////////// Dialog /////////////////////////////////////////
    public static class OrderDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_number_picker,null);
            // Number picker
            final NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.num_pick);
            numberPicker.setMaxValue(100);
            numberPicker.setMinValue(1);
            numberPicker.setWrapSelectorWheel(true);

            builder.setTitle(R.string.num_of_orders)
                    .setMessage(R.string.choose_num_of_orders)
                    .setView(view)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // OK
                            ContentValues cv = new ContentValues(1);
                            cv.put(DataContract.COLUMN_FAVORITE, 1);
                            cv.put(DataContract.COLUMN_ORDER, numberPicker.getValue());
                            cv.put(DataContract.COLUMN_TS, String.valueOf(System.currentTimeMillis()));
                            getActivity().getContentResolver().update(DataContract.appendToUri(NAME), cv, null, null);
                            Snackbar.make(fab, R.string.added_snackbar, Snackbar.LENGTH_SHORT).show();
                            fab.setImageResource(R.drawable.ic_done_white_48dp);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
    /////////////////////////////// Recycle Adapter /////////////////////////////////
    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private Cursor cursor;
        private String[] data = {DataContract.COLUMN_PRICE, DataContract.COLUMN_DESCRIPTION, DataContract.COLUMN_DETAILS, DataContract.COLUMN_INGREDIENT};

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ((NormalViewHolder) holder).setupData();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return new NormalViewHolder(inflater.inflate(R.layout.card_item_detail, parent, false));

        }


        @Override
        public int getItemCount() {
            if ( null == cursor ) return 0;
            return data.length;
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

            private final TextView headerText, contentText;


            public NormalViewHolder(View itemView) {
                super(itemView);    // pass view to super class
                /* attach views */
                headerText = (TextView) itemView.findViewById(R.id.item_header);
                contentText = (TextView) itemView.findViewById(R.id.item_content);
            }

            public void setupData(){
                String column = data[getAdapterPosition()];
                headerText.setText(column);
                contentText.setText(cursor.getString(cursor.getColumnIndex(column)));
            }
        }
    }
}
