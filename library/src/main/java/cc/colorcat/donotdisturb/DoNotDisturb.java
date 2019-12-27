package cc.colorcat.donotdisturb;

import android.content.Context;
import android.os.Build;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import java.util.LinkedHashSet;

/**
 * Author: cxx
 * Date: 2019-10-23
 * GitHub: https://github.com/ccolorcat
 */
public abstract class DoNotDisturb {
    @NonNull
    public static DoNotDisturb newInstance() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? new DoNotDisturbM() : new DoNotDisturbCompat();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    protected static <T> T getService(@NonNull Context context, @NonNull String serviceName) {
        T service = (T) context.getSystemService(serviceName);
        if (service == null) {
            throw new IllegalArgumentException("getSystemService failed by " + serviceName);
        }
        return service;
    }

    Context mContext;
    private LinkedHashSet<StatusChangeListener> mListeners = new LinkedHashSet<>(6);

    @CallSuper
    public void onCreate(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    @CallSuper
    public void onDestroy() {
        mListeners.clear();
    }

    /**
     * switch the status between enabled and disabled
     */
    public final void toggle() {
        setEnabled(!isEnabled());
    }

    /**
     * enable or disable the do not disturb
     * note: please check permission before
     */
    public abstract void setEnabled(boolean enabled);

    /**
     * @return {@code true} if do not disturb enabled, else {@code false}
     */
    public abstract boolean isEnabled();

    public abstract boolean hasPermission();

    public abstract boolean navigateToPermissionSetting();

    public final boolean addStatusChangeListener(StatusChangeListener listener) {
        return addStatusChangeListener(listener, false);
    }

    /**
     * @param listener   the status monitor, null will be ignore.
     * @param receiveNow if {@code true} and the specified listener successfully added,
     *                   which will receive the status at once.
     * @return {@code true} if the specified listener successfully added
     */
    public final boolean addStatusChangeListener(StatusChangeListener listener, boolean receiveNow) {
        if (listener != null) {
            boolean result = mListeners.add(listener);
            if (result && receiveNow) {
                listener.onStatusChanged(isEnabled());
            }
            return result;
        }
        return false;
    }

    public final boolean removeStatusChangeListener(StatusChangeListener listener) {
        return mListeners.remove(listener);
    }

    protected void notifyStatusChanged() {
        boolean enabled = isEnabled();
        for (StatusChangeListener listener : mListeners) {
            listener.onStatusChanged(enabled);
        }
    }

    public interface StatusChangeListener {
        /**
         * this will be called when the status changed.
         */
        void onStatusChanged(boolean enabled);
    }
}
