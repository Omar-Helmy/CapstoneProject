package com.omar.capstoneproject.ui;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.omar.capstoneproject.R;
import com.omar.capstoneproject.data.DataContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class FirstFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private Context context;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private final int ID_LOADER = 1;
    private StorageReference storageRef;

    public FirstFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_first, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycle_view);

        /* Firebase Storage Ref */
        storageRef = FirebaseStorage.getInstance().getReference("Menu/Images/");

        /* setup recycle view with adapter */
        recyclerAdapter= new RecyclerAdapter();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context,2);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(position==0)
                    return 2;
                return 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // creates new loader with cursor carries the required data based on id (salty or sweet)
        String[] projection = {DataContract.COLUMN_NAME, DataContract.COLUMN_IMAGE};
        switch (id){
            case ID_LOADER:
                return new CursorLoader(context, DataContract.DATA_URI, projection, null, null, null);
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

        private final int VIEW_TYPE_NORMAL = 1, VIEW_TYPE_SPLIT = 2;
        private Cursor cursor;

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            // put data from cursor into the right layout
            switch (holder.getItemViewType()){
                case VIEW_TYPE_NORMAL:
                    ((NormalViewHolder) holder).setupData();
                    break;
                case VIEW_TYPE_SPLIT:
                    ((SplitViewHolder) holder).setupData();
                    break;
            }

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            // choose which layout to inflate which view holder based on view type:
            switch (viewType){
                case VIEW_TYPE_NORMAL:
                     return new NormalViewHolder(inflater.inflate(R.layout.card_item_main, parent, false));
                case VIEW_TYPE_SPLIT:
                    return new SplitViewHolder(inflater.inflate(R.layout.category_item, parent, false));
                default:
                    return null;
            }
        }


        @Override
        public int getItemCount() {
            if ( null == cursor ) return 0;
            // items = cursor count + split item
            return cursor.getCount()+1;
        }

        @Override
        public int getItemViewType(int position) {
            if(position==0)
                return VIEW_TYPE_SPLIT;   // splitter item
            else
                return VIEW_TYPE_NORMAL;   // normal item
        }

        /* this method not overridden */
        public void updateCursor(Cursor c){
            cursor = c;
            notifyDataSetChanged();
        }


        public class SplitViewHolder extends RecyclerView.ViewHolder {

            private final TextView textView; // must be final
            private final ImageView imageView;

            public SplitViewHolder(View itemView) {
                super(itemView);
                setIsRecyclable(false);
                /* attach views */
                textView = (TextView) itemView.findViewById(R.id.category_text);
                imageView = (ImageView) itemView.findViewById(R.id.category_image);
            }

            public void setupData() {
                textView.setText(R.string.explore_split);
                Glide.with(context).load(R.drawable.salty_icon).crossFade().into(imageView);
                }

            }


        public class NormalViewHolder extends RecyclerView.ViewHolder{

            private final TextView textView; // must be final
            private final ImageView imageView;


            public NormalViewHolder(View itemView) {
                super(itemView);    // pass view to super class
                /* attach views */
                textView = (TextView) itemView.findViewById(R.id.card_text);
                imageView = (ImageView) itemView.findViewById(R.id.card_image);
                /* on click listener on the whole view */
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cursor.moveToPosition(getAdapterPosition()-1);  // we sub one as there is split item at index 0
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra(DataContract.COLUMN_NAME,
                                cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME)));    // 1 is already added
                        context.startActivity(intent);
                    }
                });
            }

            public void setupData(){
                cursor.moveToPosition(getAdapterPosition()-1);  // we sub one as there is split item at index 0
                textView.setText(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME)));
                // Download image using Firebase UI Storage and Glide:
                Glide.with(context).using(new FirebaseImageLoader())
                        .load(storageRef.child(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_IMAGE))))
                        //.placeholder()
                        .centerCrop().crossFade().into(imageView);
                imageView.setContentDescription(cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME)));
            }
        }
    }
}
/*
    https://guides.codepath.com/android/Heterogenous-Layouts-inside-RecyclerView
    https://guides.codepath.com/android/using-the-recyclerview
 */