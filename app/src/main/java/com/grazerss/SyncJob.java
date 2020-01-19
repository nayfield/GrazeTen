package com.grazerss;

import android.content.Context;

import com.grazerss.EntryManager.SyncJobStatus;
import com.grazerss.jobs.Job;

public abstract class SyncJob extends Job
{
  private EntryManager  entryManager;
  private Context       context;
  private SyncJobStatus status;
  public int            target;
  public int            actual;

  SyncJob(Context context, EntryManager entryManager, SyncJobStatus status, String message)
  {
    super(message, entryManager);
    this.entryManager = entryManager;
    this.context = context;
    this.status = status;
  }

  protected EntryManager getEntryManager()
  {
    return entryManager;
  }

  protected Context getContext()
  {
    return context;
  }

  protected SyncJobStatus getSyncJobStatus()
  {
    return status;
  }

  @Override
  public boolean isProgressMeassurable()
  {
    return target != -1;
  }

  @Override
  public int[] getProgress()
  {
    return new int[] { actual, target };
  }

  protected abstract int doRun() throws Throwable;

  public void run() throws Throwable
  {
    PL.log("About to be executed: " + getJobDescription(), context);
    target = -1;
    actual = -1;
    int noOfArticlesAffected = doRun();
    PL.log("No of articles affected=" + noOfArticlesAffected, context);
    status.noOfEntriesUpdated += noOfArticlesAffected;
    if (status.noOfEntriesUpdated > 0)
      entryManager.fireModelUpdated();

  }
}