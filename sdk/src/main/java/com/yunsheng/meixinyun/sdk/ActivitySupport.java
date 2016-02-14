package com.yunsheng.meixinyun.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.yunsheng.meixinyun.sdk.comm.Constant;
import com.yunsheng.meixinyun.sdk.javabean.LoginConfig;
import com.yunsheng.meixinyun.sdk.service.IMChatService;
import com.yunsheng.meixinyun.sdk.service.IMContactService;
import com.yunsheng.meixinyun.sdk.service.ReConnectService;

/**
 * Actity 工具类
 *
 */
public class ActivitySupport extends Activity{

    protected Context context = null;
    protected SharedPreferences preferences;
    protected ProgressDialog pg = null;
    protected NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        preferences = getSharedPreferences(Constant.LOGIN_SET, 0);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        pg = new ProgressDialog(context);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public ProgressDialog getProgressDialog() {
        return pg;
    }

    public void startService() {
        //好友联系人服务
        Intent server = new Intent(context, IMContactService.class);
        context.startService(server);
        // 聊天服务
        Intent chatServer = new Intent(context, IMChatService.class);
        context.startService(chatServer);
        // 自动恢复连接服务
        Intent reConnectService = new Intent(context, ReConnectService.class);
        context.startService(reConnectService);
    }

    /**
     * 销毁服务
     *
     */
    public void stopService() {
        // 好友联系人服务
        Intent server = new Intent(context, IMContactService.class);
        context.stopService(server);
        // 聊天服务
        Intent chatServer = new Intent(context, IMChatService.class);
        context.stopService(chatServer);

        // 自动恢复连接服务
        Intent reConnectService = new Intent(context, ReConnectService.class);
        context.stopService(reConnectService);
    }


    public boolean hasInternetConnected() {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo network = manager.getActiveNetworkInfo();
            if (network != null && network.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

    public boolean validateInternet() {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            openWirelessSet();
            return false;
        } else {
            NetworkInfo[] info = manager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        openWirelessSet();
        return false;
    }

    public boolean hasLocationGPS() {
        LocationManager manager = (LocationManager) context
                .getSystemService(context.LOCATION_SERVICE);
        if (manager
                .isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasLocationNetWork() {
        LocationManager manager = (LocationManager) context
                .getSystemService(context.LOCATION_SERVICE);
        if (manager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void checkMemoryCard() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.prompt)
                    .setMessage("请检查内存卡")
                    .setPositiveButton(R.string.menu_settings,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                    Intent intent = new Intent(
                                            Settings.ACTION_SETTINGS);
                                    context.startActivity(intent);
                                }
                            })
                    .setNegativeButton("退出",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    dialog.cancel();
                                }
                            }).create().show();
        }
    }

    public void openWirelessSet() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder
                .setTitle(R.string.prompt)
                .setMessage(context.getString(R.string.check_connection))
                .setPositiveButton(R.string.menu_settings,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                Intent intent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                context.startActivity(intent);
                            }
                        })
                .setNegativeButton(R.string.close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.cancel();
                            }
                        });
        dialogBuilder.show();
    }


    public void showToast(String text, int longint) {
        Toast.makeText(context, text, longint).show();
    }

    public void showToast(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public void closeInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && this.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void setNotiType(int iconId, String contentTitle,
                            String contentText, Class activity, String from) {
        /*
		 * 创建新的Intent，作为点击Notification留言条时， 会运行的Activity
		 */
        Intent notifyIntent = new Intent(this, activity);
        notifyIntent.putExtra("to", from);
        // notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		/* 创建PendingIntent作为设置递延运行的Activity */
        PendingIntent appIntent = PendingIntent.getActivity(this, 0,
                notifyIntent, 0);

		/* 创建Notication，并设置相关参数 */
        Notification.Builder builder = new Notification.Builder(this);
        builder.setAutoCancel(true)
                .setSmallIcon(iconId)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(appIntent);
        Notification notification = builder.build();

        notificationManager.notify(0,notification);
    }

    public Context getContext() {
        return context;
    }

    public SharedPreferences getLoginUserSharedPre() {
        return preferences;
    }

    public void saveLoginConfig(LoginConfig loginConfig) {
        preferences.edit()
                .putString(Constant.XMPP_HOST, loginConfig.getXmppHost())
                .commit();
        preferences.edit()
                .putInt(Constant.XMPP_PORT, loginConfig.getXmppPort()).commit();
        preferences
                .edit()
                .putString(Constant.XMPP_SEIVICE_NAME,
                        loginConfig.getXmppServiceName()).commit();
        preferences.edit()
                .putString(Constant.USERNAME, loginConfig.getUsername())
                .commit();
        preferences.edit()
                .putString(Constant.PASSWORD, loginConfig.getPassword())
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_AUTOLOGIN, loginConfig.isAutoLogin())
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_NOVISIBLE, loginConfig.isNovisible())
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_REMEMBER, loginConfig.isRemember())
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_ONLINE, loginConfig.isOnline())
                .commit();
        preferences.edit()
                .putBoolean(Constant.IS_FIRSTSTART, loginConfig.isFirstStart())
                .commit();
    }

    public LoginConfig getLoginConfig() {
        LoginConfig loginConfig = new LoginConfig();
        String a = preferences.getString(Constant.XMPP_HOST, null);
        String b = getResources().getString(R.string.xmpp_host);
        loginConfig.setXmppHost(preferences.getString(Constant.XMPP_HOST,
                getResources().getString(R.string.xmpp_host)));
        loginConfig.setXmppPort(preferences.getInt(Constant.XMPP_PORT,
                getResources().getInteger(R.integer.xmpp_port)));
        loginConfig.setUsername(preferences.getString(Constant.USERNAME, null));
        loginConfig.setPassword(preferences.getString(Constant.PASSWORD, null));
        loginConfig.setXmppServiceName(preferences.getString(
                Constant.XMPP_SEIVICE_NAME,
                getResources().getString(R.string.xmpp_service_name)));
        loginConfig.setAutoLogin(preferences.getBoolean(Constant.IS_AUTOLOGIN,
                getResources().getBoolean(R.bool.is_autologin)));
        loginConfig.setNovisible(preferences.getBoolean(Constant.IS_NOVISIBLE,
                getResources().getBoolean(R.bool.is_novisible)));
        loginConfig.setRemember(preferences.getBoolean(Constant.IS_REMEMBER,
                getResources().getBoolean(R.bool.is_remember)));
        loginConfig.setFirstStart(preferences.getBoolean(
                Constant.IS_FIRSTSTART, true));
        return loginConfig;
    }

    public boolean getUserOnlineState() {
        // preferences = getSharedPreferences(Constant.LOGIN_SET,0);
        return preferences.getBoolean(Constant.IS_ONLINE, true);
    }

    public void setUserOnlineState(boolean isOnline) {
        // preferences = getSharedPreferences(Constant.LOGIN_SET,0);
        preferences.edit().putBoolean(Constant.IS_ONLINE, isOnline).commit();

    }

}
