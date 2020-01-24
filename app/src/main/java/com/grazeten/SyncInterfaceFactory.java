package com.grazeten;

import java.lang.reflect.Constructor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class SyncInterfaceFactory
{
  private static final String TAG = SyncInterfaceFactory.class.getName();

  public static BackendProvider getSyncInterface(Context context)
  {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    String className = sharedPreferences.getString(EntryManager.SETTINGS_SERVICE_PROVIDER, "com.grazeten.NewsBlurBackendProvider");

    Log.d(TAG, "Attempting to load sync class: " + className);

    try
    {
      Class<?> syncClass = Class.forName(className);
      Log.d(TAG, "Class loaded: " + syncClass.toString());
      Constructor<Context> constructor = (Constructor<Context>) syncClass.getConstructor(Context.class);
      Log.d(TAG, "Constructor loaded: " + constructor.toString());
      BackendProvider obj = (BackendProvider) constructor.newInstance(context);
      Log.d(TAG, "SyncInterface loaded: " + obj.toString());
      return obj;
    }
    catch (Exception e)
    {
      Log.e(TAG, e.getMessage());
      return new NewsBlurBackendProvider(context);
    }
  }
}
