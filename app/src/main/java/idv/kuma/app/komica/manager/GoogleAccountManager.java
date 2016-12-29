package idv.kuma.app.komica.manager;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by TakumaLee on 2015/10/23.
 */
public class GoogleAccountManager {

    private List<Account> accountList;

    public GoogleAccountManager() {
        accountList = new ArrayList<>();
    }

    public static GoogleAccountManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static class SingletonHolder {
        public static GoogleAccountManager INSTANCE = new GoogleAccountManager();
    }

    public void getGoogleAccountList(Context context) {
//        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            accountList.add(account);
        }
    }

    public String getFirstAccountMail() {
        List<String> emailArray = new ArrayList<>();
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        for (Account account : accountList) {
            if (emailPattern.matcher(account.name).matches()) {
                emailArray.add(account.name);
            }
        }

        return emailArray.toString();
    }
}
