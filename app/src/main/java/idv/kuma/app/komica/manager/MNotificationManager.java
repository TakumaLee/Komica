package idv.kuma.app.komica.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;

/**
 * Created by TakumaLee on 15/2/1.
 */
public class MNotificationManager {
    private static final String TAG = MNotificationManager.class.getSimpleName();

    public static final int MMM_NOTIFICATION_ID = -Integer.MAX_VALUE;

    private MMMBroadcastReceiver eventReceiver;
    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews remoteViews;
    private IntentFilter intentFilter;

    private String ZERO_TWICE = "zero_twice",
            ZERO = "zero",
            ONE = "one",
            TWO = "two",
            THREE = "three",
            FOUR = "four",
            FIVE = "five",
            SIX = "six",
            SEVEN = "seven",
            EIGHT = "eight",
            NINE = "nine",
            DELETE = "delete",
            CLEAR = "clear",
            DOT = "dot",
            INCOME = "income",
            OUTLAY = "outlay",
            CLOSE = "close";

//    public MNotificationManager(MoeService service) {
//        this.service = service;
//        eventReceiver = new MMMBroadcastReceiver();
//        intentFilter = new IntentFilter();
//        intentFilter.addAction(ZERO_TWICE);
//        intentFilter.addAction(ZERO);
//        intentFilter.addAction(ONE);
//        intentFilter.addAction(TWO);
//        intentFilter.addAction(THREE);
//        intentFilter.addAction(FOUR);
//        intentFilter.addAction(FIVE);
//        intentFilter.addAction(SIX);
//        intentFilter.addAction(SEVEN);
//        intentFilter.addAction(EIGHT);
//        intentFilter.addAction(NINE);
//        intentFilter.addAction(DELETE);
//        intentFilter.addAction(CLEAR);
//        intentFilter.addAction(DOT);
//        intentFilter.addAction(INCOME);
//        intentFilter.addAction(OUTLAY);
//        intentFilter.addAction(CLOSE);
//    }

//    public void generateNotification() {
//        Log.v(TAG, "generate notification");
//        if (!service.isFastInsertOpen()) {
//            return;
//        }
//        notificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
////        KeyboardLayout keyboardLayout = new KeyboardLayout(context);
//        remoteViews = new RemoteViews(service.getPackageName(), R.layout.notification_inout_money);
////        DecimalFormat df = new DecimalFormat("#0.##");
////        String moneyResult = df.format(service.getMoneyCount());
//        String moneyResult = service.getMoneyCount();
//        remoteViews.setTextViewText(R.id.textView_NotificationInput, moneyResult);
//        remoteViews.setTextViewText(R.id.textView_NotificationCurrency, Currency.getInstance(MoeService.getInstance().getMainCurrencyCode()).getSymbol());
//
//        Intent intent = new Intent(service.getApplicationContext(), MHomeActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(service.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(service);
//        notification = mBuilder.setSmallIcon(R.drawable.ic_mmm_monochrome)
//                .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), R.mipmap.ic_launcher))
//                .setContentTitle("ＭＭＭ")
//                .setContentText(service.getResources().getString(R.string.pull_down_to_use))
//                .setPriority(NotificationCompat.PRIORITY_MAX)
//                .setOngoing(true)
//                .setContentIntent(pendingIntent)
//                .build();
//
//        notification.bigContentView = remoteViews;
//        notification.flags |= Notification.FLAG_NO_CLEAR;
//        notification.flags |= Intent.FLAG_ACTIVITY_SINGLE_TOP;
//
////        Intent switchIntent = new Intent(service, MMMBroadcastReceiver.class);
////        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, switchIntent, 0);
//
//        remoteViews.setOnClickPendingIntent(R.id.button0, eventReceiver.getButtonEventIntent(service, 0, new Intent(ZERO)));
//        remoteViews.setOnClickPendingIntent(R.id.button1, eventReceiver.getButtonEventIntent(service, 1, new Intent(ONE)));
//        remoteViews.setOnClickPendingIntent(R.id.button2, eventReceiver.getButtonEventIntent(service, 2, new Intent(TWO)));
//        remoteViews.setOnClickPendingIntent(R.id.button3, eventReceiver.getButtonEventIntent(service, 3, new Intent(THREE)));
//        remoteViews.setOnClickPendingIntent(R.id.button4, eventReceiver.getButtonEventIntent(service, 4, new Intent(FOUR)));
//        remoteViews.setOnClickPendingIntent(R.id.button5, eventReceiver.getButtonEventIntent(service, 5, new Intent(FIVE)));
//        remoteViews.setOnClickPendingIntent(R.id.button6, eventReceiver.getButtonEventIntent(service, 6, new Intent(SIX)));
//        remoteViews.setOnClickPendingIntent(R.id.button7, eventReceiver.getButtonEventIntent(service, 7, new Intent(SEVEN)));
//        remoteViews.setOnClickPendingIntent(R.id.button8, eventReceiver.getButtonEventIntent(service, 8, new Intent(EIGHT)));
//        remoteViews.setOnClickPendingIntent(R.id.button9, eventReceiver.getButtonEventIntent(service, 9, new Intent(NINE)));
//        remoteViews.setOnClickPendingIntent(R.id.button00, eventReceiver.getButtonEventIntent(service, 0, new Intent(ZERO_TWICE)));
//        remoteViews.setOnClickPendingIntent(R.id.button_delete, eventReceiver.getButtonEventIntent(service, 0, new Intent(DELETE)));
//        remoteViews.setOnClickPendingIntent(R.id.button_clear, eventReceiver.getButtonEventIntent(service, 0, new Intent(CLEAR)));
//        remoteViews.setOnClickPendingIntent(R.id.button_dot, eventReceiver.getButtonEventIntent(service, 0, new Intent(DOT)));
//        remoteViews.setOnClickPendingIntent(R.id.button_income, eventReceiver.getButtonEventIntent(service, 0, new Intent(INCOME)));
//        remoteViews.setOnClickPendingIntent(R.id.button_expense, eventReceiver.getButtonEventIntent(service, 0, new Intent(OUTLAY)));
//        remoteViews.setOnClickPendingIntent(R.id.button_Close, eventReceiver.getButtonEventIntent(service, 0, new Intent(CLOSE)));
//
//        notificationManager.notify(MMM_NOTIFICATION_ID, notification);
//    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

//    public void registerReceiver() {
//        service.registerReceiver(eventReceiver, intentFilter);
//    }
//
//    public void unRegisterReceiver() {
//        service.unregisterReceiver(eventReceiver);
//    }

    public class MMMBroadcastReceiver extends BroadcastReceiver {
        private final String TAGB = MMMBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
//            Log.v(TAGB, "onReceive()");
//
//            Tracker tracker = ((MMMApplication) context.getApplicationContext()).getTracker();
//
//            int tmpNumber = intent.getExtras().getInt("number");
//
//            if (intent.getAction().equals(CLEAR)) {
//                service.resetNumber();
//            } else if (intent.getAction().equals(DELETE)) {
//                service.deleteNumber();
//            } else if (intent.getAction().equals(INCOME)) {
//                service.insertRecordToDB(Double.valueOf(service.getMoneyCount()));
//                tracker.send(new HitBuilders.EventBuilder()
//                        .setCategory("快速輸入")
//                        .setAction("收入")
//                        .setLabel(String.valueOf(service.getMoneyCount()))
//                        .build());
//                service.resetNumber();
//            } else if (intent.getAction().equals(OUTLAY)) {
//                service.insertRecordToDB(-Double.valueOf(service.getMoneyCount()));
//                tracker.send(new HitBuilders.EventBuilder()
//                        .setCategory("快速輸入")
//                        .setAction("支出")
//                        .setLabel(String.valueOf(service.getMoneyCount()))
//                        .build());
//                service.resetNumber();
//            } else if (intent.getAction().equals(DOT)) {
//                service.enableDot();
//            } else if (intent.getAction().equals(ZERO_TWICE)) {
//                service.addTwiceZeroNumber();
//            } else {
//                service.addNumber(String.valueOf(tmpNumber));
//            }
////            DecimalFormat df = new DecimalFormat("#0.##");
//            String moneyResult = service.getMoneyCount();
//            remoteViews.setTextViewText(R.id.textView_NotificationInput, moneyResult);
//
//            notificationManager.notify(MMM_NOTIFICATION_ID, notification);
//
//            if (intent.getAction().equals(CLOSE)) {
//                tracker.send(new HitBuilders.EventBuilder()
//                        .setCategory("快速輸入")
//                        .setAction("關閉")
//                        .setLabel("關閉快速輸入")
//                        .build());
//                notificationManager.cancel(MMM_NOTIFICATION_ID);
//                service.commitSwitchOfFastInsert(false);
//            }

//            Log.v(TAGB, String.valueOf(service.getMoneyCount()));


        }

        public PendingIntent getButtonEventIntent(Context context, int flag, Intent intent) {
            intent.putExtra("number", flag);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, flag, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            return pendingIntent;
        }
    }
}
