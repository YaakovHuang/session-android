package org.thoughtcrime.securesms;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.concurrent.TimeUnit;

import network.qki.messenger.R;

public class MuteDialog extends AlertDialog {


  protected MuteDialog(Context context) {
    super(context);
  }

  protected MuteDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
    super(context, cancelable, cancelListener);
  }

  protected MuteDialog(Context context, int theme) {
    super(context, theme);
  }

  public static void show(final Context context, final @NonNull MuteSelectionListener listener) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.MuteDialog_mute_notifications);
    builder.setItems(R.array.mute_durations, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, final int which) {
        final long muteUntil;

        switch (which) {
          case 1:  muteUntil = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2);  break;
          case 2:  muteUntil = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);   break;
          case 3:  muteUntil = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7);   break;
          case 4:  muteUntil = Long.MAX_VALUE; break;
          default: muteUntil = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1);  break;
        }

        listener.onMuted(muteUntil);
      }
    });

    builder.show();

  }

  public interface MuteSelectionListener {
    public void onMuted(long until);
  }

}
