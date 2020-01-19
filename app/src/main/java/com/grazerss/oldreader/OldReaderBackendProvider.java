package com.grazerss.oldreader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import retrofit.client.Response;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.grazerss.ArticleDbState;
import com.grazerss.AuthenticationFailedException;
import com.grazerss.BackendProvider;
import com.grazerss.DB;
import com.grazerss.DiscoveredFeed;
import com.grazerss.Entry;
import com.grazerss.EntryManager;
import com.grazerss.Feed;
import com.grazerss.Label;
import com.grazerss.NeedsSessionException;
import com.grazerss.NewsRob;
import com.grazerss.PL;
import com.grazerss.ReadState;
import com.grazerss.SyncJob;
import com.grazerss.activities.LoginActivity;
import com.grazerss.download.HtmlEntitiesDecoder;
import com.grazerss.jobs.Job;
import com.grazerss.oldreader.ItemContentResponse.Item;
import com.grazerss.oldreader.ItemContentResponse.Item.Link;
import com.grazerss.oldreader.SubscriptionResponse.Subscriptions;
import com.grazerss.oldreader.UpdateArticlesRequest.MarkType;
import com.grazerss.util.Timing;

public class OldReaderBackendProvider implements BackendProvider
{
  private static final int HTTP_OK      = 200;
  private OldReaderManager api          = null;
  private Context          context;
  private EntryManager     entryManager = null;
  private volatile long    lastUpdate   = -1;

  public OldReaderBackendProvider(Context context)
  {
    this.context = context.getApplicationContext();
  }

  @Override
  public boolean authenticate(Context context, String email, String password, String captchaToken, String captchaAnswer)
      throws ClientProtocolException, IOException, AuthenticationFailedException
  {
    api = new OldReaderManager();
    api.login(email, password);

    return true;
  }

  @Override
  public void differentialUpdateOfArticlesStates(EntryManager entryManager, Job job, String stream, String excludeState,
      ArticleDbState articleDbState) throws SAXException, IOException, ParserConfigurationException, ServerBadRequestException,
      ServerBadRequestException, AuthenticationExpiredException
  {
    // TODO Auto-generated method stub
  }

  @Override
  public List<DiscoveredFeed> discoverFeeds(String query) throws SyncAPIException, IOException, ServerBadRequestException,
      ParserConfigurationException, SAXException, ServerBadRequestException, AuthenticationExpiredException
  {
    // TODO Auto-generated method stub
    return null;
  }

  private int fetchAndStoreArticles(ItemsResponse itemList, List<Feed> feeds)
  {
    ItemContentResponse content = api.getItemContents(itemList);
    List<Entry> entriesToBeInserted = new ArrayList<Entry>(20);
    int articlesFetchedCount = 0;

    if ((content == null) || (content.errors != null))
    {
      return 0;
    }

    for (Item story : content.items)
    {
      // Don't save one we already have.
      if (getEntryManager().findEntryByAtomId(story.id) != null)
      {
        continue;
      }

      Entry newEntry = new Entry();
      newEntry.setAtomId(story.id);
      newEntry.setContentURL(getLink(story.canonical));
      newEntry.setContent(story.summary.content);
      newEntry.setTitle(HtmlEntitiesDecoder.decodeString(story.title));
      newEntry.setReadState(ReadState.UNREAD);
      newEntry.setFeedAtomId(story.origin.streamId);
      // newEntry.setAuthor(story.authors);
      newEntry.setAlternateHRef(getLink(story.alternate));
      newEntry.setHash(story.id);
      newEntry.setStarred(false);
      newEntry.setUpdated(story.crawlTimeMsec == null ? new Date().getTime() : story.crawlTimeMsec);
      setLastUpdate(story.crawlTimeMsec);

      // Fill in some data from the feed record....
      Feed nrFeed = getFeedFromAtomId(feeds, story.origin.streamId);

      if (nrFeed != null)
      {
        newEntry.setFeedId(nrFeed.getId());
        newEntry.setDownloadPref(nrFeed.getDownloadPref());
        newEntry.setDisplayPref(nrFeed.getDisplayPref());

        if (story.categories != null)
        {
          for (String labelName : story.categories)
          {
            if (labelName.contains("user/-/label/"))
            {
              newEntry.addLabel(new Label(labelName.replaceAll("user/-/label/", "")));
            }
          }
        }
      }

      entriesToBeInserted.add(newEntry);
      articlesFetchedCount++;
    }

    entryManager.insert(entriesToBeInserted);
    entriesToBeInserted.clear();
    entryManager.fireModelUpdated();

    return articlesFetchedCount;
  }

  @Override
  public int fetchNewEntries(EntryManager entryManager, SyncJob job, boolean manualSync) throws ClientProtocolException, IOException,
      NeedsSessionException, SAXException, IllegalStateException, ParserConfigurationException, FactoryConfigurationError, SyncAPIException,
      ServerBadRequestException, AuthenticationExpiredException
  {

    if (handleAuthenticate(entryManager) == false)
    {
      return 0;
    }

    lastUpdate = getEntryManager().getGRUpdated();
    Long localLastUpdate = lastUpdate;

    int maxCapacity = entryManager.getNewsRobSettings().getStorageCapacity();
    int currentUnreadArticlesCount = entryManager.getUnreadArticleCountExcludingPinned();
    int maxDownload = maxCapacity - currentUnreadArticlesCount;
    int fetchedArticleCount = 0;

    job.setJobDescription("Fetching feed information");

    // Update the feed list, make sure we have feed records for everything...
    final List<Feed> feeds = entryManager.findAllFeeds();
    SubscriptionResponse remoteFeeds = api.getSubscriptionList();
    updateFeeds(remoteFeeds, feeds);

    UnreadCountResponse unreadCounts = api.getUnreadCounts();

    // Here we start getting stories.
    job.setJobDescription("Fetching new articles");
    job.actual = 0;
    job.target = Math.min(maxDownload, unreadCounts.max);
    entryManager.fireStatusUpdated();

    String continuation = null;
    int seenItems = 0;

    List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();
    ExecutorService executor = Executors.newFixedThreadPool(10);

    while ((fetchedArticleCount < maxDownload) && ((currentUnreadArticlesCount + fetchedArticleCount) < maxCapacity))
    {
      final ItemsResponse items = api.getUnreadItems(continuation, getEntryManager().isGrazeRssOnlySyncingEnabled() ? GRAZERSS_LABEL : null,
          getEntryManager().shouldShowNewestArticlesFirst(), localLastUpdate, 20);

      continuation = items.continuation;
      seenItems += items.itemRefs.size();

      // Start new article fetches in the background
      Future<Integer> future = executor.submit(new Callable<Integer>()
      {
        @Override
        public Integer call() throws Exception
        {
          return fetchAndStoreArticles(items, feeds);
        }
      });

      futureList.add(future);

      // If we might have enough articles, wait for the background threads to be done.
      if (seenItems >= maxDownload)
      {
        waitForFetchThreads(futureList);
      }

      // Check any completed jobs and process results
      List<Future<Integer>> toDelete = new ArrayList<Future<Integer>>();
      for (Future<Integer> f : futureList)
      {
        if (f.isDone())
        {
          try
          {
            int count = f.get();

            job.actual += count;
            fetchedArticleCount += count;
            entryManager.fireStatusUpdated();

            toDelete.add(f);
          }
          catch (InterruptedException e)
          {
            PL.log(e.getMessage(), e, context);
          }
          catch (ExecutionException e)
          {
            PL.log(e.getMessage(), e, context);
            e.printStackTrace();
            job.cancel();
          }
        }
      }

      // Remove processed results from the list
      futureList.removeAll(toDelete);

      if (job.isCancelled())
      {
        break;
      }
    }

    job.actual = job.target;
    entryManager.fireStatusUpdated();

    entryManager.setGRUpdated(lastUpdate);

    return fetchedArticleCount;
  }

  private final EntryManager getEntryManager()
  {
    if (entryManager == null)
    {
      entryManager = EntryManager.getInstance(context);
    }

    return entryManager;
  }

  private Feed getFeedFromAtomId(List<Feed> feeds, String atomId)
  {
    try
    {
      for (Feed feed : feeds)
      {
        if (atomId.equals(feed.getAtomId()))
        {
          return feed;
        }
      }
    }
    catch (Exception e)
    {
      String message = "Problem during getFeedFromAtomId: " + e.getMessage();
      PL.log(message, context);
    }

    return null;
  }

  private String getLink(List<Link> links)
  {
    for (Link link : links)
    {
      if ((link != null) && (link.href != null))
      {
        return link.href;
      }
    }

    return null;
  }

  @Override
  public Class getLoginClass()
  {
    return LoginActivity.class;
  }

  @Override
  public String getServiceName()
  {
    return "The Old Reader";
  }

  @Override
  public String getServiceUrl()
  {
    return "http://www.theoldreader.com";
  }

  private boolean handleAuthenticate(EntryManager entryManager)
  {
    try
    {
      if ((api == null) || (api.haveAuthToken() == false))
      {
        return authenticate(this.context, entryManager.getEmail(), entryManager.getAuthToken().getAuthToken(), null, null);
      }
      else
      {
        return true; // We have an auth token here, so move on....
      }

    }
    catch (Exception e)
    {
      String message = "Problem during handleAuthenticate: " + e.getMessage();
      PL.log(message, context);
    }

    return false;
  }

  @Override
  public void logout()
  {
    api = null;
    getEntryManager().clearAuthToken();
    getEntryManager().setGoogleUserId(null);
  }

  private int remotelyAlterReadState(Collection<Entry> entries, final String column, String desiredState)
  {
    try
    {
      if (desiredState.equals("1"))
      {
        UpdateArticlesRequest update = new UpdateArticlesRequest(MarkType.READ);

        for (Entry e : entries)
        {
          update.addId(e.getAtomId());
        }

        Response r = api.updateArticles(update);

        if (r.getStatus() == HTTP_OK)
        {
          getEntryManager().removePendingStateMarkers(update.getIds(), column);
          return entries.size();
        }
      }
      else if (desiredState.equals("0"))
      {
        UpdateArticlesRequest update = new UpdateArticlesRequest(MarkType.UNREAD);

        for (Entry e : entries)
        {
          update.addId(e.getAtomId());
        }

        Response r = api.updateArticles(update);

        if (r.getStatus() == HTTP_OK)
        {
          getEntryManager().removePendingStateMarkers(update.getIds(), column);
          return entries.size();
        }
      }
    }
    catch (Exception e)
    {
      String message = "Problem during marking entry as un-/read: " + e.getMessage();
      PL.log(message, context);
    }

    return 0;
  }

  private int remotelyAlterState(Collection<Entry> entries, final String column, String desiredState)
  {
    if (column.equals(DB.Entries.READ_STATE_PENDING))
    {
      return remotelyAlterReadState(entries, column, desiredState);
    }

    return 0;
  }

  private synchronized void setLastUpdate(Long update)
  {
    if ((update != null) && (update > lastUpdate))
    {
      lastUpdate = update;
    }
  }

  @Override
  public void startLogin(Activity activity, Context context)
  {
    activity.startActivity(new Intent().setClass(context, LoginActivity.class));
  }

  @Override
  public boolean submitSubscribe(String url2subscribe) throws SyncAPIException
  {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int synchronizeArticles(EntryManager entryManager, SyncJob syncJob) throws MalformedURLException, IOException,
      ParserConfigurationException, FactoryConfigurationError, SAXException, ParseException, NeedsSessionException, ParseException
  {
    try
    {
      if (handleAuthenticate(entryManager) == false)
      {
        return 0;
      }

      int noOfUpdated = syncServerReadStates(entryManager, syncJob);

      String[] fields = { DB.Entries.READ_STATE_PENDING, DB.Entries.STARRED_STATE_PENDING
      // DB.Entries.PINNED_STATE_PENDING
      };
      for (String f : fields)
      {

        String progressLabel;
        if (f == DB.Entries.READ_STATE_PENDING)
        {
          progressLabel = "read";
        }
        else if (f == DB.Entries.STARRED_STATE_PENDING)
        {
          progressLabel = "starred";
        }
        else if (f == DB.Entries.PINNED_STATE_PENDING)
        {
          progressLabel = "pinned";
        }
        else
        {
          progressLabel = "unknown";
        }

        String[] desiredStates = { "0", "1" };
        for (String desiredState : desiredStates)
        {
          List<Entry> allEntries = entryManager.findAllStatePendingEntries(f, desiredState);

          if (allEntries.size() == 0)
          {
            continue;
          }

          syncJob.setJobDescription("Syncing state: " + progressLabel);
          syncJob.target = allEntries.size();
          syncJob.actual = 0;
          entryManager.fireStatusUpdated();

          // LATER make this cancelable? Add Job here.

          int offset = 0;

          while (offset < allEntries.size())
          {
            int nextPackSize = Math.min(allEntries.size() - offset, 25);
            if (nextPackSize == 0)
            {
              break;
            }

            List<Entry> currentPack = new ArrayList<Entry>(allEntries.subList(offset, offset + nextPackSize));
            offset += nextPackSize;
            noOfUpdated += remotelyAlterState(currentPack, f, desiredState);
            syncJob.actual = noOfUpdated;
            entryManager.fireStatusUpdated();
          }
        }
      }
      return noOfUpdated;
    }
    catch (NullPointerException e)
    {
      String message = "Problem during syncArticles: " + e.getMessage();
      PL.log(message, context);
    }
    return 0;
  }

  private int syncServerReadStates(EntryManager entryManager, SyncJob job)
  {
    try
    {
      job.setJobDescription("Syncing server read states");
      // UnreadHashResponse hashes = apiManager.getUnreadStoryHashes(null);
      //
      // entryManager.populateTempTableHashes(TempTable.READ_HASHES, hashes.flatHashList);
      // entryManager.updateStatesFromTempTableHash(TempTable.READ_HASHES, ArticleDbState.READ);
      job.setJobDescription("Server read states synced");
    }
    catch (Exception e)
    {
      String message = "Problem during syncServerReadStates: " + e.getMessage();
      PL.log(message, context);
    }

    return 0;
  }

  @Override
  public void unsubscribeFeed(String feedAtomId) throws IOException, NeedsSessionException, SyncAPIException
  {
    // TODO Auto-generated method stub

  }

  private void updateFeeds(SubscriptionResponse remoteFeeds, List<Feed> feeds)
  {
    for (Subscriptions remoteFeed : remoteFeeds.subscriptions)
    {
      boolean found = false;

      for (Feed nrFeed : feeds)
      {
        if ((nrFeed != null) && nrFeed.getAtomId().equals(remoteFeed.id))
        {
          found = true;
          break;
        }
      }

      if (found == false)
      {
        Feed newFeed = new Feed();
        newFeed.setAtomId(remoteFeed.id);
        newFeed.setTitle(remoteFeed.title);
        newFeed.setUrl(remoteFeed.url);
        newFeed.setDownloadPref(Feed.DOWNLOAD_PREF_DEFAULT);
        newFeed.setDisplayPref(Feed.DISPLAY_PREF_DEFAULT);

        long id = entryManager.insert(newFeed);
        newFeed.setId(id);

        feeds.add(newFeed);
      }

    }
  }

  @Override
  public void updateSubscriptionList(EntryManager entryManager, Job job) throws IOException, ParserConfigurationException, SAXException,
      ServerBadRequestException, AuthenticationExpiredException
  {
    Timing t = null;

    try
    {
      if (handleAuthenticate(entryManager) == false)
      {
        return;
      }

      if (job.isCancelled())
      {
        return;
      }

      if ((entryManager.getLastSyncedSubscriptions() != -1l)
          && (System.currentTimeMillis() < (entryManager.getLastSyncedSubscriptions() + ONE_DAY_IN_MS)))
      {
        PL.log("Not updating subscription list this time.", context);
        return;
      }

      PL.log("Updating subscription list.", context);

      t = new Timing("UpdateSubscriptionList", context);
      final Map<String, String> remoteTitlesAndIds = new HashMap<String, String>(107);

      SubscriptionResponse feeds = api.getSubscriptionList();

      for (Subscriptions feed : feeds.subscriptions)
      {
        remoteTitlesAndIds.put(feed.id, feed.title);
      }

      if (NewsRob.isDebuggingEnabled(context))
      {
        PL.log("Got subscription list with " + remoteTitlesAndIds.size() + " feeds.", context);
      }

      entryManager.updateFeedNames(remoteTitlesAndIds);
      entryManager.updateLastSyncedSubscriptions(System.currentTimeMillis());
    }
    catch (NullPointerException e)
    {
      String message = "Problem during updateSubscriptionList: " + e.getMessage();
      PL.log(message, context);
    }
    finally
    {
      if (t != null)
      {
        t.stop();
      }
    }
  }

  private void waitForFetchThreads(List<Future<Integer>> futureList)
  {
    boolean wait = true;

    while (wait)
    {
      try
      {
        Thread.sleep(100);
      }
      catch (InterruptedException e)
      {
        PL.log(e.getMessage(), e, context);
      }

      wait = false;
      for (Future<Integer> f : futureList)
      {
        if (f.isDone() == false)
        {
          wait = true;
          break;
        }
      }
    }
  }

}
