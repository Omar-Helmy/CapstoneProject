package com.omar.capstoneproject.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.omar.capstoneproject.R;
import com.omar.capstoneproject.ui.DetailActivity;
import com.omar.capstoneproject.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class FoodWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_food);
            // Intent to start service
            Intent serviceIntent = new Intent(context, WidgetService.class);
            // Add the app widget ID to the intent extras.
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            // Set up the collection
            views.setRemoteAdapter(R.id.widget_list, serviceIntent);
            // Start service
            //context.startService(new Intent(context, WidgetService.class));
            // Create an Intent to launch MainActivity
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent);
            // Create an Intent to launch DetailActivity
            Intent clickIntentTemplate = new Intent(context, DetailActivity.class);
            PendingIntent pendingIntentTemplate = PendingIntent.getActivity(context, 0, clickIntentTemplate, 0);
            views.setPendingIntentTemplate(R.id.widget_list_item, pendingIntentTemplate);
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

