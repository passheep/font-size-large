package com.passheep.fontsizelarge.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.passheep.fontsizelarge.MainActivity;
import com.passheep.fontsizelarge.R;
import com.passheep.fontsizelarge.service.WidgetService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 */

public class WidgetProvider extends AppWidgetProvider {

    // 更新 widget 的广播对应的action
    private final String ACTION_UPDATE_ALL = "com.passheep.widget.UPDATE_ALL";
    // 保存 widget 的id的HashSet，每新建一个 widget 都会为该 widget 分配一个 id。
    private static Set<String> idsSet = new HashSet();

    public static int mIndex;

    /**
     * 接收窗口小部件点击时发送的广播
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        final String action = intent.getAction();
        SharedPreferences sharedPreferences = context.getSharedPreferences("widgetData", Context.MODE_PRIVATE);
        idsSet = sharedPreferences.getStringSet("battery", new HashSet());
        if (ACTION_UPDATE_ALL.equals(action) && null != idsSet) {
            // “更新”广播
            updateAllAppWidgets(context, AppWidgetManager.getInstance(context), idsSet);
        }
    }

    // 更新所有的 widget
    private void updateAllAppWidgets(Context context, AppWidgetManager appWidgetManager, Set set) {
        // widget 的id
        int appID;
        // 迭代器，用于遍历所有保存的widget的id
        Iterator it = set.iterator();

        // 要显示的那个数字，每更新一次 + 1
        // TODO:可以在这里做更多的逻辑操作，比如：数据处理、网络请求等。然后去显示数据
//        mIndex++;

        Map<String, String> mm = getBattery(context);

        while (it.hasNext()) {
//            appID = ((Integer) it.next()).intValue();
            appID = Integer.parseInt(String.valueOf(it.next()));

            // 获取 example_appwidget.xml 对应的RemoteViews
            RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.app_widget);

            // 设置显示数字
            remoteView.setTextViewText(R.id.widget_txt, mm.get("level") + "%");

            // 设置点击按钮对应的PendingIntent：即点击按钮时，发送广播。
//            remoteView.setOnClickPendingIntent(R.id.widget_btn_reset, getResetPendingIntent(context));
//            remoteView.setOnClickPendingIntent(R.id.widget_btn_open, getOpenPendingIntent(context));
            remoteView.setOnClickPendingIntent(R.id.ll_widgetLayout, restartService(context));

            // 更新 widget
            appWidgetManager.updateAppWidget(appID, remoteView);
        }
    }

    /**
     *
     */
    private PendingIntent restartService(Context context) {
        Intent service = new Intent(context, WidgetService.class);
        context.startService(service);
        PendingIntent pi = PendingIntent.getService(context, 0, service, 0);
        return pi;
    }

    /**
     * 获取 重置数字的广播
     */
    private PendingIntent getResetPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, WidgetProvider.class);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        return pi;
    }

    /**
     * 获取 打开 MainActivity 的 PendingIntent
     */
    private PendingIntent getOpenPendingIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.putExtra("main", "这句话是我从桌面点开传过去的。");
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        return pi;
    }

    /**
     * 当该窗口小部件第一次添加到桌面时调用该方法，可添加多次但只第一次调用
     */
    @Override
    public void onEnabled(Context context) {
        // 在第一个 widget 被创建时，开启服务
        Intent intent = new Intent(context, WidgetService.class);
        context.startService(intent);
        super.onEnabled(context);
    }

    // 当 widget 被初次添加 或者 当 widget 的大小被改变时，被调用
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle
            newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * 当小部件从备份恢复时调用该方法
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

    /**
     * 每次窗口小部件被点击更新都调用一次该方法
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.v("life--", "onUpdate");
        // 每次 widget 被创建时，对应的将widget的id添加到set中
        for (int appWidgetId : appWidgetIds) {
            idsSet.add(String.valueOf(appWidgetId));
        }
        SharedPreferences.Editor editor = context.getSharedPreferences("widgetData", Context.MODE_PRIVATE).edit();
        editor.putStringSet("battery", idsSet);
        editor.apply();
    }

    /**
     * 每删除一次窗口小部件就调用一次
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // 当 widget 被删除时，对应的删除set中保存的widget的id
        for (int appWidgetId : appWidgetIds) {
            idsSet.remove(String.valueOf(appWidgetId));
        }
        super.onDeleted(context, appWidgetIds);
        SharedPreferences.Editor editor = context.getSharedPreferences("widgetData", Context.MODE_PRIVATE).edit();
        editor.putStringSet("battery", idsSet);
        editor.apply();
    }

    /**
     * 当最后一个该窗口小部件删除时调用该方法，注意是最后一个
     */
    @Override
    public void onDisabled(Context context) {
        // 在最后一个 widget 被删除时，终止服务
        Intent intent = new Intent(context, WidgetService.class);
        context.stopService(intent);
        super.onDisabled(context);
    }

    private Map<String, String> getBattery(Context context) {
        Map<String, String> mm = new HashMap<>();
        // charging=2  level
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (null == intent) {
            mm.put("level", "暂无");
            mm.put("charging", "1");
        } else {
            mm.put("level", String.valueOf(intent.getIntExtra("level", 0)));
            mm.put("charging", String.valueOf(intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN)));
        }
        return mm;
    }
}
