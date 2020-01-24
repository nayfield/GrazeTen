package com.grazeten.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.grazeten.EntryManager;
import com.grazeten.PL;

public final class FireReceiver extends BroadcastReceiver
{

  @Override
  public void onReceive(final Context context, final Intent intent)
  {
    EntryManager entryManager = EntryManager.getInstance(context);
    PL.log("FireReceiver with intent action: " + intent.getAction(), context);
    if ("com.grazeten.CANCEL_SYNC".equals(intent.getAction()))
    {
      entryManager.cancel();
      PL.log("Externally triggered cancel.", context);
    }
    else if ("com.grazeten.UP_SYNC".equals(intent.getAction()))
    {
      entryManager.requestSynchronization(true);
      PL.log("Externally triggered refresh (up sync only).", context);
    }
    else
    {
      entryManager.requestSynchronization(false);
      PL.log("Externally triggered refresh (full).", context);

    }
  }

}