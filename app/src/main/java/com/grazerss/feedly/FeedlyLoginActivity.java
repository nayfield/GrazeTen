package com.grazerss.feedly;

import static com.grazerss.feedly.FeedlyApi.AUTH_URL;
import static com.grazerss.feedly.FeedlyApi.CLIENT_ID;
import static com.grazerss.feedly.FeedlyApi.REDIRECT_URI;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.grazerss.EntryManager;
import com.grazerss.R;

public class FeedlyLoginActivity extends Activity
{
  private ProgressBar mProgress;

  private WebView     webview;
  private String      accessCode;

  private String extractToken(String url)
  {
    String[] sArray = url.split("code=");
    return (sArray[1].split("&"))[0];
  }

  private String getAuthorizationRequestUri()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(AUTH_URL);
    sb.append("?response_type=code");
    sb.append("&client_id=" + CLIENT_ID);
    sb.append("&redirect_uri=" + REDIRECT_URI);
    sb.append("&scope=https://cloud.feedly.com/subscriptions");
    return sb.toString();
  }

  private void hideProgress()
  {
    if (mProgress.getVisibility() != View.GONE)
    {
      final Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
      mProgress.setVisibility(View.GONE);
      mProgress.startAnimation(fadeOut);
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.feedly_login);

    webview = (WebView) findViewById(R.id.feedly_Login_webview);
    webview.setVisibility(View.VISIBLE);
    webview.getSettings().setJavaScriptEnabled(true);
    mProgress = (ProgressBar) findViewById(R.id.progress);
    hideProgress();

    // set up webview for OAuth2 login
    webview.setWebViewClient(new WebViewClient()
    {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url)
      {
        // Log.d(TAG, "** in shouldOverrideUrlLoading(), url is: " + url);
        if (url.startsWith(REDIRECT_URI))
        {

          // extract OAuth2 access code appended in url
          if (url.indexOf("code=") != -1)
          {
            showProgress();

            accessCode = extractToken(url);

            // store in SharedPreferences
            EntryManager entryManager = EntryManager.getInstance(getApplicationContext());
            SharedPreferences preferences = entryManager.getSharedPreferences();

            Editor e = preferences.edit();
            e.putString(FeedlyApi.SHPREF_KEY_ACCESS_CODE, accessCode);
            e.commit();

            entryManager.saveLastSuccessfulLogin();
            entryManager.requestSynchronization(false);

            finish();
          }

          // don't go to redirectUri
          return true;
        }

        // load the webpage from url (login and grant access)
        return super.shouldOverrideUrlLoading(view, url); // return false;
      }
    });

    // do OAuth2 login
    String authorizationUri = getAuthorizationRequestUri();
    webview.loadUrl(authorizationUri);
  }

  private void showProgress()
  {
    if (mProgress.getVisibility() != View.VISIBLE)
    {
      final Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
      mProgress.setVisibility(View.VISIBLE);
      mProgress.startAnimation(fadeIn);
    }
  }
}
