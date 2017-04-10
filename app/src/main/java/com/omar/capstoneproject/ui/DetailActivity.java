package com.omar.capstoneproject.ui;

import android.database.Cursor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.omar.capstoneproject.R;
import com.omar.capstoneproject.data.DataContract;

public class DetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Cursor cursor;
    private ImageView imageView;
    private CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        imageView = (ImageView) findViewById(R.id.toolbar_parallax_image);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);

        /* setup tool bar */
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* get intent to get data from cursor */
        cursor = getContentResolver().query(DataContract.DATA_URI,null,
                DataContract._ID+" == "+getIntent().getIntExtra(DataContract._ID,0),null,null);
        cursor.moveToFirst();

        /* setup collapse tool bar with title and image*/
        collapsingToolbar.setTitle(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME)));
        Glide.with(this).load(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_IMAGE)))
                .centerCrop().into(imageView);





    }
}
