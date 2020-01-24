package com.grazeten.activities;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.BadTokenException;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.grazeten.BackendProvider;
import com.grazeten.EntryManager;
import com.grazeten.R;
import com.grazeten.auth.AccountManagementUtils;
import com.grazeten.auth.IAccountManagementUtils;
import com.grazeten.auth.IAuthenticationCallback;
import com.grazeten.util.SDK9Helper;
import com.grazeten.util.U;

public class AccountListActivity extends ListActivity
{

  private Handler                 handler = new Handler();
  private IAccountManagementUtils accountManagementUtils;

  protected void doAuth(final String accountName)
  {
    final IAuthenticationCallback callback = new IAuthenticationCallback()
    {

      @Override
      public void onAuthTokenReceived(String googleAccount, String authToken)
      {
        EntryManager entryManager = EntryManager.getInstance(AccountListActivity.this);
        entryManager.doLogin(googleAccount, authToken);

        LoginActivity.onSuccess(entryManager, googleAccount);
        SDK9Helper.apply(entryManager.getSharedPreferences().edit().remove(EntryManager.SETTINGS_PASS));
        AccountListActivity.this.finish();
      }

      @Override
      public void onError(Exception e)
      {
        AccountListActivity.this.onError(e);
      }
    };

    accountManagementUtils.getAuthToken(AccountListActivity.this, handler, callback, accountName);
  }

  protected EntryManager getEntryManager()
  {
    return EntryManager.getInstance(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.account_list);

    getEntryManager().getNewsRobNotificationManager().cancelSyncProblemNotification();

    accountManagementUtils = AccountManagementUtils.getAccountManagementUtils(this);

    Button addAccountButton = (Button) findViewById(R.id.add_account);
    addAccountButton.setVisibility(View.GONE);

    final Intent i = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
    if (getPackageManager().resolveActivity(i, 0) != null)
    {

      addAccountButton.setVisibility(View.VISIBLE);
      addAccountButton.setOnClickListener(new View.OnClickListener()
      {

        @Override
        public void onClick(View v)
        { // com.google.android.gsf, GOOGLE,
          // google, com.google, com.android.contacts (cac -> all
          // acounts)
          // i.putExtra("authorities", new String[] { "" });
          // startActivity(i);
          accountManagementUtils.addAccount(AccountListActivity.this);
        }
      });
    }

    Button useClassicButton = (Button) findViewById(R.id.use_classic);
    useClassicButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent i = new Intent();
        BackendProvider grf = getEntryManager().getSyncInterface();
        i.setClass(AccountListActivity.this, grf.getLoginClass());
        i.putExtra("USE_CLASSIC", true);
        startActivity(i);
        finish();
      }
    });

  }

  protected void onError(Exception ex)
  {

    final AlertDialog dialog = new AlertDialog.Builder(this).create();
    dialog.setIcon(android.R.drawable.ic_dialog_alert);
    dialog.setButton("OK", new DialogInterface.OnClickListener()
    {

      public void onClick(DialogInterface dialog, int which)
      {
        dialog.dismiss();
      }

    }); // i18n
    dialog.setTitle(U.t(this, R.string.login_error_dialog_title));

    String message = null;
    if (ex instanceof IOException)
    {
      message = "Could not reach the Google server. Are you online?";
    }
    else
    {
      message = U.t(this, R.string.login_error_dialog_message) + " " + ex.getMessage();
    }
    dialog.setMessage(message); // i18n
    try
    {
      dialog.show();
    }
    catch (BadTokenException e)
    {
      //
    }
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id)
  {
    super.onListItemClick(l, v, position, id);
    final String accountName = (String) getListAdapter().getItem(position);
    final String s1 = accountName.substring(0, accountName.indexOf('@'));

    new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle("Login").setMessage("Login using " + s1 + "?")
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {

          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            new Thread(new Runnable()
            {
              @Override
              public void run()
              {
                doAuth(accountName);
              }
            }).start();
          }
        }).setNegativeButton(android.R.string.cancel, null).show();

  }

  @Override
  protected void onResume()
  {
    super.onResume();
    setListAdapter(new ArrayAdapter<String>(this, R.layout.account_row, R.id.account_name, accountManagementUtils.getAccounts(this)));
  }
}
