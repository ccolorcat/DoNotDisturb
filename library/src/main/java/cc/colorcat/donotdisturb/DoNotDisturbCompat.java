package cc.colorcat.donotdisturb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import androidx.annotation.NonNull;


/**
 * Author: cxx
 * Date: 2019-10-22
 * GitHub: https://github.com/ccolorcat
 */
public class DoNotDisturbCompat extends DoNotDisturb {
    private static final int INVALID_RINGER_MODE = -1;
    private static final String ACTION = AudioManager.RINGER_MODE_CHANGED_ACTION;
    private static final int DND_ENABLED = AudioManager.RINGER_MODE_SILENT;
    private static final int DND_DISABLED_DEFAULT = AudioManager.RINGER_MODE_NORMAL;

    private BroadcastReceiver mReceiver;
    private AudioManager mManager;
    private int mLastDisabledMode = INVALID_RINGER_MODE;
    private int mCurrentMode = INVALID_RINGER_MODE;

    DoNotDisturbCompat() {
    }

    @Override
    public void onCreate(@NonNull Context context) {
        super.onCreate(context);
        mManager = getService(mContext, Context.AUDIO_SERVICE);
        mReceiver = new DndAudioBroadcastReceiver();
        mContext.registerReceiver(mReceiver, new IntentFilter(ACTION));
        updateRingerMode(true);
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
        return true;
    }

    @Override
    public boolean navigateToPermissionSetting() {
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            if (mCurrentMode != DND_ENABLED) {
                mManager.setRingerMode(DND_ENABLED);
            }
        } else {
            if (mCurrentMode == DND_ENABLED) {
                mManager.setRingerMode(mLastDisabledMode);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return judgeEnabled(mCurrentMode);
    }

    private void updateRingerMode(boolean forceUpdateLast) {
        int temp = mManager.getRingerMode();
        if (mCurrentMode == temp) return;
        int lastMode = mCurrentMode;
        mCurrentMode = temp;
        if (mCurrentMode != DND_ENABLED) {
            mLastDisabledMode = mCurrentMode;
        } else if (forceUpdateLast) {
            mLastDisabledMode = DND_DISABLED_DEFAULT;
        }
        if (judgeEnabled(lastMode) != judgeEnabled(mCurrentMode)) {
            notifyStatusChanged();
        }
    }

    private boolean judgeEnabled(int mode) {
        return mode == DND_ENABLED;
    }

    private class DndAudioBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION.equals(intent.getAction())) {
                updateRingerMode(false);
            }
        }
    }
}
