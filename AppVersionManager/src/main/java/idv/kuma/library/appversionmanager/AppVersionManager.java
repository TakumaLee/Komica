package idv.kuma.library.appversionmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppVersionManager {
    private static final String TAG = AppVersionManager.class.getSimpleName();
    private static final String APP_VERSION_MANAGER = "app_version_manager";

    private static final String IS_NEED_UPDATE_ALERT = "is_need_update_alert";
    private static final String APP_VERSION_CODE = "app_version_code";

    private Context context;
    private static AppVersionManager instance = null;
    private AppVersion appVersion = new AppVersion();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isNeedUpdateAlert = true;
    private boolean isNeedNeverAlertBtn = false;
    private int versionCode = 0;

    private MaterialDialog dialog;

    private String apkName = "temp";
//    private RelativeLayout relativeLayout;
//    private ProgressBar progressBar;
//    private TextView textView;

    private AppVersionManager(Context context, String apkName, boolean isNeedNeverAlertBtn, VersionUpdateListener updateListener) throws PackageManager.NameNotFoundException {
        this.context = context;
        this.apkName = apkName;
        this.isNeedNeverAlertBtn = isNeedNeverAlertBtn;
        setUpdateListener(updateListener);

        appVersion.setCurrentVersion(context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionName);

        sharedPreferences = context.getSharedPreferences(APP_VERSION_MANAGER, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        isNeedUpdateAlert = sharedPreferences.getBoolean(IS_NEED_UPDATE_ALERT, true);
        versionCode = sharedPreferences.getInt(APP_VERSION_CODE, 0);
        int currentVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT).versionCode;
        if (versionCode < currentVersionCode) {
            versionCode = currentVersionCode;
            editor.putInt(APP_VERSION_CODE, versionCode);
            editor.commit();
            this.updateListener.installOrUpdate();
        }
//        relativeLayout = new RelativeLayout(context);
//        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
//        textView = new TextView(context);
//        RelativeLayout.LayoutParams progressBarParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        progressBarParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        progressBarParams.setMargins(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
//
//        RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        textViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//
//        relativeLayout.addView(progressBar, progressBarParams);
//        relativeLayout.addView(textView, textViewParams);
//
//        progressBar.setMax(100);
//        textView.setText(R.string.update_message);
//        textView.setTextColor(Color.BLACK);
//
//        progressBar.setVisibility(View.GONE);
    }

    public static synchronized AppVersionManager initSingleton(Context context) {
        return initSingleton(context, false);
    }

    public static synchronized AppVersionManager initSingleton(Context context, String apkName) {
        return initSingleton(context, apkName, false);
    }

    public static synchronized AppVersionManager initSingleton(Context context, boolean isNeedNeverAlertBtn) {
        return initSingleton(context, "x", isNeedNeverAlertBtn, null);
    }

    public static synchronized AppVersionManager initSingleton(Context context, VersionUpdateListener updateListener) {
        return initSingleton(context, "x", false, updateListener);
    }

    public static synchronized AppVersionManager initSingleton(Context context, String apkName, boolean isNeedNeverAlertBtn) {
        return initSingleton(context, apkName, isNeedNeverAlertBtn, null);
    }

    public static synchronized AppVersionManager initSingleton(Context context, String apkName, VersionUpdateListener updateListener) {
        return initSingleton(context, apkName, false, updateListener);
    }

    public static synchronized AppVersionManager initSingleton(Context context, String apkName, boolean isNeedNeverAlertBtn, VersionUpdateListener updateListener) {
        if (instance == null && context != null) {
            Context appContext = context.getApplicationContext();
            try {
                instance = new AppVersionManager(appContext, apkName, isNeedNeverAlertBtn, updateListener);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public static AppVersionManager getInstance() {
        return instance;
    }

    public void updateVersion(final Context context, final String updateUrl) {
                OkHttpClientConnect clientConnect = new OkHttpClientConnect();
                clientConnect.excuteSSLGet(updateUrl, new NetworkCallback() {
                    @Override
                    public void onFailure(IOException e) {

                    }

                    @Override
                    public void onResponse(int responseCode, final String result) {
                        Log.v(TAG, result);
                        JSONObject object = null;
                        try {
                            object = new JSONObject(result);
                            if (object.has("update_force")) {
                                appVersion.setForceUpdate(object.getBoolean("update_force"));
                            }
                            if (object.has("update_link")) {
                                appVersion.setUpdateLink(object.getString("update_link"));
                            }
                            if (object.has("update_version_code")) {
                                appVersion.setUpdateVersionCode(object.getInt("update_version_code"));
                            }
                            if (object.has("update_version")) {
                                appVersion.setUpdateSwitch(object.getString("update_version"));
                            }
                            if (appVersion.hasNewVersion()) {
                                updateAKP(context);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    private void updateAKP(final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                alertUpdate(context);
            }
        });
    }

    private void alertUpdate(final Context context) {
        if (isNeedUpdateAlert) {

            String content = context.getString(R.string.update_message2,
                    context.getString(R.string.html_format_color, apkName + " ( " + appVersion.getUpdateSwitch() + " )")).trim();

            MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(context)
                    .title(R.string.new_version_available)
                    .content(content)
//                    .progress(false, 100)
//                    .customView(relativeLayout, false)
                    .positiveText(R.string.button_update_now)
                    .positiveColor(ColorStateList.valueOf(Color.BLUE))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(final MaterialDialog materialDialog, DialogAction dialogAction) {
//                            progressBar.setVisibility(View.VISIBLE);
//                            textView.setVisibility(View.GONE);

                            // download file
//                            new AsyncDownloader().execute();
                            final String googlePlayUrl = "https://play.google.com/store/apps/details?id=" + context.getPackageName();
                            ;
                            OkHttpClientConnect clientConnect = new OkHttpClientConnect();
                            clientConnect.excuteSSLGet(googlePlayUrl, new NetworkCallback() {
                                @Override
                                public void onFailure(IOException e) {

                                }

                                @Override
                                public void onResponse(int responseCode, String result) {
                                    if (checkPlayStoreIsExist(result)) {
                                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(googlePlayUrl)));
                                    } else {
                                        materialDialog.getProgressBar().setVisibility(View.VISIBLE);
                                        new AsyncDownloader().execute();
//                                        downloadAPKFromURL(context);
//                                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(appVersion.getUpdateLink())));
                                    }
                                }

                            });

                        }
                    });
            if (appVersion.isForceUpdate()) {
                dialogBuilder.cancelable(false);
                dialogBuilder.autoDismiss(false);
            }
            if (!appVersion.isForceUpdate()) {
                dialogBuilder.negativeText(R.string.button_later)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {

                            }
                        });
            }
            if (isNeedNeverAlertBtn && !appVersion.isForceUpdate()) {
                dialogBuilder.neutralText(R.string.button_never_do)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                isNeedUpdateAlert = false;
                                editor.putBoolean(IS_NEED_UPDATE_ALERT, isNeedUpdateAlert);
                                editor.commit();
                            }
                        });
            }
            dialog = dialogBuilder.build();
            dialog.getContentView().setText(Html.fromHtml(content));
            dialog.getActionButton(DialogAction.POSITIVE).setTextSize(14);
            dialog.getActionButton(DialogAction.NEGATIVE).setTextSize(14);
            dialog.getActionButton(DialogAction.NEUTRAL).setTextSize(14);
            dialog.show();
        }
    }

    public void destroyDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public boolean checkPlayStoreIsExist(String result) {
        Pattern p = Pattern.compile("\"softwareVersion\"\\W*([\\d\\.]+)");
        Matcher matcher = p.matcher(result);
        return matcher.find();
    }

    private void downloadAPKFromURL(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra("url", appVersion.getUpdateLink());
        context.startService(intent);
    }

    private void setUpdateListener(VersionUpdateListener updateListener) {
        this.updateListener = updateListener == null ? this.updateListener : updateListener;
    }

    private VersionUpdateListener updateListener = new VersionUpdateListener() {
        @Override
        public void installOrUpdate() {

        }
    };

    private class AsyncDownloader extends AsyncTask<Void, Long, Boolean> {
        String path = context.getFilesDir() + "/Download/" + apkName + ".apk";

        @Override
        protected Boolean doInBackground(Void... params) {

            File file = new File(path);

            OkHttpClient httpClient = new OkHttpClient();
            Call call = httpClient.newCall(new Request.Builder().url(appVersion.getUpdateLink()).get().build());
            try {
                Response response = call.execute();
                Log.d("VaryTest", "Vary: " + response.header("Vary"));
                Log.d("VaryTest", "Content-Length: " + response.header("Content-Length"));
                if (response.code() == 200) {
                    InputStream inputStream = null;
                    try {
                        inputStream = response.body().byteStream();
                        byte[] buff = new byte[1024 * 4];
                        long downloaded = 0;
                        long target = response.body().contentLength();

                        OutputStream output = new FileOutputStream(file);
                        try {
                            publishProgress(0L, target);
                            while (true) {
                                int readed = inputStream.read(buff);
                                if (readed == -1) {
                                    break;
                                }
                                //write buff
                                output.write(buff, 0, readed);
                                downloaded += readed;
                                publishProgress(downloaded, target);
                                if (isCancelled()) {
                                    return false;
                                }
                            }
                            output.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            output.close();
                        }
                        return downloaded == target;
                    } catch (IOException ignore) {
                        return false;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            Log.v(TAG, String.valueOf(values[0].intValue()));
//            progressBar.setProgress(values[0].intValue() * 100 / values[1].intValue());

//            textViewProgress.setText(String.format("%d / %d", values[0], values[1]));
        }

        @Override
        protected void onPostExecute(Boolean result) {
//            Snackbar.make(relativeLayout, result ? "Downloaded" : "Failed", Snackbar.LENGTH_LONG).show();
            if (result) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
