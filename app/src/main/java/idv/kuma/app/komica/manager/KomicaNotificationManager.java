package idv.kuma.app.komica.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import idv.kuma.app.komica.R;
import idv.kuma.app.komica.activities.KomicaHomeActivity;


/**
 * Created by TakumaLee on 2016/5/25.
 */
public class KomicaNotificationManager {
    public static final int NOTIFICATION_ID = -Integer.MAX_VALUE;

    public static KomicaNotificationManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static KomicaNotificationManager INSTANCE = new KomicaNotificationManager();
    }

    public void generateNotification(Context context, String message) {
        String title = context.getString(R.string.app_name);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Notification notification = getNotification(context, bitmap, title, message);

        notifyNotification(context, notification);
    }

    public void generateNotification(Context context, String title, String message) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        Notification notification = getNotification(context, bitmap, title, message);

        notifyNotification(context, notification);
    }

    public void generateNotification(Context context, Bitmap bitmap, String title, String message) {
        Notification notification = getNotification(context, bitmap, title, message);

        notifyNotification(context, notification);
    }

    private void notifyNotification(Context context, Notification notification) {
        if (notification != null) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    protected Notification getNotification(Context context, Bitmap bitmap, String title, String message) {
        Intent intent = new Intent(context.getApplicationContext(), KomicaHomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        notification.flags |= Intent.FLAG_ACTIVITY_SINGLE_TOP;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        return notification;
    }
}
