package com.omar.capstoneproject.ui;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.omar.capstoneproject.R;
import com.omar.capstoneproject.data.DataContract;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private ViewPager viewPager;
    private FirstFragment firstFragment;
    private SecondFragment secondFragment;
    private FloatingActionButton fab;
    private TabLayout tabLayout;
    private final String DATA_STORED_KEY = "dataStored";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* find views */
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout =(TabLayout) findViewById(R.id.tab_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        /* setup toolbar */
        setSupportActionBar(toolbar);

        /* setup drawer toggle */
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_toggle_open,R.string.drawer_toggle_close);
        drawerLayout.addDrawerListener(drawerToggle);

        /* setup navigation view */
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);      // Highlight the selected item
                        drawerLayout.closeDrawers();    // Close drawer
                        return true;
                    }
                });

        /* setup pager */
        FragmentAdapter fa = new FragmentAdapter(getSupportFragmentManager());
        firstFragment = new FirstFragment();
        secondFragment = new SecondFragment();
        viewPager.setAdapter(fa);

        /* setup tab layout with pager */
        tabLayout.setupWithViewPager(viewPager);

        /* FAB */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Refreshing data...", Snackbar.LENGTH_SHORT).show();
                fetchDataFromFirebase();
            }
        });

        /* sync data between firebase and local database  */
        syncDataWithFirebase();



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void syncDataWithFirebase(){
        // check if data fetched before
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if(sharedPref.getBoolean(DATA_STORED_KEY,false))
            // data already found
            return;

        // connect to internet
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (!networkInfo.isConnectedOrConnecting()){
            // no internet
            Toast.makeText(MainActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchDataFromFirebase();

    }

    private void fetchDataFromFirebase(){
        // show progress dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        // delete the database table:
        getContentResolver().delete(DataContract.DATA_URI,null,null);
        // Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        // Read from the database
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Map<String,String> map = (Map<String, String>) child.getValue();
                    Parcel parcel = Parcel.obtain();
                    parcel.writeMap(map);
                    parcel.setDataPosition(0);
                    ContentValues  cv = ContentValues.CREATOR.createFromParcel(parcel);
                    getContentResolver().insert(DataContract.DATA_URI, cv);
                }
                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(DATA_STORED_KEY,true);
                editor.apply();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /******************** Pager Adapter ***************************/
    private class FragmentAdapter extends FragmentPagerAdapter{

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return firstFragment;
                case 1:
                    return secondFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch(position){
                case 0:
                    return "Explore";
                case 1:
                    return "Orders";
                default:
                    return null;
            }
        }
    }
}

/*
    http://guides.codepath.com/android/handling-scrolls-with-coordinatorlayout
    http://guides.codepath.com/android/fragment-navigation-drawer
 */