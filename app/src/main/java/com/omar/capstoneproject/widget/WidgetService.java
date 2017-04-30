package com.omar.capstoneproject.widget;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.omar.capstoneproject.R;
import com.omar.capstoneproject.data.DataContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetViewsFactory();
    }

    private class WidgetViewsFactory implements RemoteViewsFactory {

        private Cursor cursor = null;

        @Override
        public RemoteViews getViewAt(int position) {
            if (cursor == null || !cursor.moveToPosition(position)) {
                return null;
            }
            RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_food_item);
            // get data from cursor and inflate layout with data
            views.setTextViewText(R.id.widget_text, cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME)));
            views.setTextViewText(R.id.widget_price, cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_PRICE)));
            // fill intent to launch detail activity
            final Intent fillInIntent = new Intent();
            fillInIntent.setData(DataContract.appendToUri(
                    cursor.getString(cursor.getColumnIndex(DataContract.COLUMN_NAME))));
            views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_food_item);

        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public int getCount() {
            return cursor == null ? 0 : cursor.getCount();
        }

        @Override
        public void onDataSetChanged() {
            if (cursor != null) {
                cursor.close();
            }
            final long identityToken = Binder.clearCallingIdentity();
            cursor = getContentResolver().query(DataContract.DATA_URI,
                    new String[]{DataContract.COLUMN_NAME, DataContract.COLUMN_PRICE}, null, null, null);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        @Override
        public void onCreate() {

        }
    }

}
