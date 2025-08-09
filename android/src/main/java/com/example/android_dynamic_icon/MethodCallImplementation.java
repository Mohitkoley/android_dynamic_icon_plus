package com.example.android_dynamic_icon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

// public void updateIcon() {
  //   if (!iconChanged || activity == null || args == null || args.isEmpty() || classNames == null) {
  //       Log.w(TAG, "Icon update skipped: not fully initialized");
  //       return;
  //   }

  //   try {
  //     String className = args.get(0);
  //     PackageManager pm = activity.getPackageManager();
  //     String packageName = activity.getPackageName();

  //     for (String alias : classNames) {
  //       ComponentName componentName = new ComponentName(packageName, packageName + "." + alias);
  //       int state = alias.equals(className)
  //           ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
  //           : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
  //       pm.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP);
  //     }

  //     iconChanged = false;
  //     Log.d(TAG, "Icon switched to: " + className);

  //   } catch (Exception e) {
  //     Log.e(TAG, "Error updating icon: " + e.getMessage(), e);
  //   }
  // }

public class MethodCallImplementation implements MethodChannel.MethodCallHandler {
  
private static final String TAG = "[android_dynamic_icon]";
    private static final String PREFS_NAME = "dynamic_icon_prefs";
    private static final String KEY_CURRENT_ICON = "current_icon";
    private static final String KEY_IS_ACTIVATED = "is_activated";
    List<String> args = new ArrayList<>();

    private final Context context;
    private Activity activity;
    private static List<String> classNames = null;

    public MethodCallImplementation(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean isReady() {
        // ✅ FIXED: Added classNames null check
        return activity != null && classNames != null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "initialize":
                classNames = call.arguments();
                Log.d(TAG, "Initialized with aliases: " + classNames);
                result.success(null);
                break;

            case "changeIcon":
                if(call.arguments() !=null){
                args = call.arguments();
                if (!args.isEmpty()) {
                   
                    updateIcon();
                }}
                result.success(null);
                break;

            case "resetToDefault":
              resetToDefault();
              break;

            default:
                result.notImplemented();
        }
    }

    // 🚀 PROFESSIONAL METHOD: Twitter/Snapchat/Zomato approach
    public void updateIcon() {
       String newIcon = args.get(0);
        if (!isReady() || newIcon == null) {
            Log.w(TAG, "Icon change skipped: not ready or invalid icon");
            return;
        }

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String currentIcon = prefs.getString(KEY_CURRENT_ICON, "DefaultIconAlias");
            boolean isActivated = prefs.getBoolean(KEY_IS_ACTIVATED, false);

            if (newIcon.equals(currentIcon)) {
                Log.d(TAG, "Icon already set to: " + newIcon);
                return;
            }

            PackageManager pm = activity.getPackageManager();
            String packageName = activity.getPackageName();

            if (!isActivated) {
                // 🔥 FIRST TIME: Switch from enabled to disabled (kills app once)
                performFirstTimeSwitch(pm, packageName, newIcon, prefs);
            } else {
                // ⚡ SUBSEQUENT: Use DEFAULT state (no kill!)
                performSubsequentSwitch(pm, packageName, currentIcon, newIcon, prefs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in professional icon change: " + e.getMessage(), e);
        }
    }

    // 🎯 First time switch (unavoidable app kill)
    private void performFirstTimeSwitch(PackageManager pm, String packageName, String newIcon, SharedPreferences prefs) {
        Log.d(TAG, "First time icon switch - app will restart once");

        ComponentName defaultAlias = new ComponentName(packageName, packageName + ".DefaultIconAlias");
        pm.setComponentEnabledSetting(defaultAlias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        ComponentName newAlias = new ComponentName(packageName, packageName + "." + newIcon);
        pm.setComponentEnabledSetting(newAlias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        prefs.edit()
                .putBoolean(KEY_IS_ACTIVATED, true)
                .putString(KEY_CURRENT_ICON, newIcon)
                .apply();

        Log.d(TAG, "First switch complete - activated system");
    }

    // ⚡ Subsequent switches (no app kill!)
    private void performSubsequentSwitch(PackageManager pm, String packageName, String currentIcon, String newIcon, SharedPreferences prefs) {
        Log.d(TAG, "Subsequent switch: " + currentIcon + " -> " + newIcon + " (no restart)");

        ComponentName currentAlias = new ComponentName(packageName, packageName + "." + currentIcon);
        pm.setComponentEnabledSetting(currentAlias, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

        ComponentName newAlias = new ComponentName(packageName, packageName + "." + newIcon);
        pm.setComponentEnabledSetting(newAlias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        prefs.edit().putString(KEY_CURRENT_ICON, newIcon).apply();

        Log.d(TAG, "Icon switched to: " + newIcon + " (seamless!)");
    }

    // 🔄 Reset to default icon
    public void resetToDefault() {
        if (!isReady()) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isActivated = prefs.getBoolean(KEY_IS_ACTIVATED, false);

        if (isActivated) {
            // Use CloneDefaultIconAlias for reset (always disabled in manifest) "CloneDefaultIconAlias"
            args.set(0,"CloneDefaultIconAlias");
            updateIcon();
        }
    }



}
