package cc.colorcat.donotdisturb;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


/**
 * Author: cxx
 * Date: 2019-10-22
 * GitHub: https://github.com/ccolorcat
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class DoNotDisturbM extends DoNotDisturb {
    private static final int INVALID_INTERRUPTION_FILTER = -1;
    private static final String ACTION = NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED;
    private static final int DND_DISABLED = NotificationManager.INTERRUPTION_FILTER_ALL;
    private static final int DND_ENABLED_DEFAULT = NotificationManager.INTERRUPTION_FILTER_PRIORITY;

    private BroadcastReceiver mReceiver;
    private NotificationManager mManager;
    private int mLastNotDisabledInterruptionFilter = INVALID_INTERRUPTION_FILTER;
    private int mCurrentInterruptionFilter = INVALID_INTERRUPTION_FILTER;

    DoNotDisturbM() {
    }

    @Override
    public void onCreate(@NonNull Context context) {
        super.onCreate(context);
        mManager = getService(mContext, Context.NOTIFICATION_SERVICE);
        mReceiver = new DndNotificationBroadcastReceiver();
        mContext.registerReceiver(mReceiver, new IntentFilter(ACTION));
        updateInterruptionFilter(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        mContext = null;
        mManager = null;
    }

    @Override
    public boolean hasPermission() {
        return mManager.isNotificationPolicyAccessGranted();
    }

    @Override
    public boolean navigateToPermissionSetting() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(mContext.getPackageManager()) != null) {
            mContext.startActivity(intent);
            return true;
        }
        return false;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            if (mCurrentInterruptionFilter == DND_DISABLED) {
                mManager.setInterruptionFilter(mLastNotDisabledInterruptionFilter);
            }
        } else {
            if (mCurrentInterruptionFilter != DND_DISABLED) {
                mManager.setInterruptionFilter(DND_DISABLED);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return judgeEnabled(mCurrentInterruptionFilter);
    }

    private void updateInterruptionFilter(boolean forceUpdateLast) {
        int temp = mManager.getCurrentInterruptionFilter();
        if (mCurrentInterruptionFilter == temp) return;
        int lastInterruptionFilter = mCurrentInterruptionFilter;
        mCurrentInterruptionFilter = temp;
        if (mCurrentInterruptionFilter != DND_DISABLED) {
            mLastNotDisabledInterruptionFilter = mCurrentInterruptionFilter;
        } else if (forceUpdateLast) {
            mLastNotDisabledInterruptionFilter = DND_ENABLED_DEFAULT;
        }
        if (judgeEnabled(lastInterruptionFilter) != judgeEnabled(mCurrentInterruptionFilter)) {
            notifyStatusChanged();
        }
    }

    private boolean judgeEnabled(int interruptionFilter) {
        return interruptionFilter != DND_DISABLED;
    }

    private class DndNotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION.equals(intent.getAction())) {
                updateInterruptionFilter(false);
            }
        }
    }
}
