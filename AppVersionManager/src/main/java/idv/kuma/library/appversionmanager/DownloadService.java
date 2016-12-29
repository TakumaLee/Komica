package idv.kuma.library.appversionmanager;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends IntentService {

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int id = 1;
        long timeNow = 0;
        //設定下載路徑名稱(名稱為strings.xml裡面的appName)
        String appName = getResources().getString(getApplication().getApplicationInfo().labelRes);
        String fileUrl = Environment.getExternalStorageDirectory() + "/Download/" + appName + ".apk";
        ;
        Intent fileIntent = new Intent(Intent.ACTION_VIEW);
        fileIntent.setDataAndType(Uri.fromFile(new File(fileUrl)), "application/vnd.android.package-archive");
        fileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, fileIntent, 0);
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //設定Notification的圖片及內容(圖片為drawable內的ic_launcher)
        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setContentTitle(appName + " Download")
                .setContentText("Download in progress")
                .setSmallIcon(getApplicationInfo().icon);
        String urlToDownload = intent.getStringExtra("url");
        try {
            int progress = 0;
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(fileUrl);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                progress = (int) (total * 100 / fileLength);
                if (System.currentTimeMillis() - timeNow > 1000) {
                    mBuilder.setProgress(100, progress, false);
                    mNotifyManager.notify(id, mBuilder.build());
                    timeNow = System.currentTimeMillis();
                }
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
            progress = 100;
            mBuilder.setContentText("Download complete")
                    .setProgress(0, 0, false)
                    .setContentIntent(contentIntent);
            mNotifyManager.notify(id, mBuilder.build());
            startActivity(fileIntent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.stopSelf();
        }
    }

}
