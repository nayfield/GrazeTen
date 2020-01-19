package com.grazerss;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.grazerss.storage.AbstractStorageAdapter;
import com.grazerss.util.U;

public class Entry
{

  private static final Map<String, String> mobilizersMap                 = new HashMap<String, String>();

  public static final int                  STATE_DOWNLOAD_ERROR          = 3;
  public static final int                  STATE_DOWNLOADED_FEED_CONTENT = 1;
  public static final int                  STATE_DOWNLOADED_FULL_PAGE    = 2;

  public static final int                  STATE_NOT_DOWNLOADED          = 0;

  public static final File getAssetsDir(EntryManager em, String shortAtomId)
  {
    return new File(em.getStorageAdapter().getAbsolutePathForAsset("a" + shortAtomId));
  }

  public static String getShortAtomId(String fullAtomId)
  {
    return AbstractStorageAdapter.longAtomIdToShortAtomId(fullAtomId);
  }

  public static final File getThumbnailFile(EntryManager em, String shortAtomId)
  {

    File f = new File(Entry.getAssetsDir(em, shortAtomId), "preview.pngnr");
    if (f.exists())
    {
      return f;
    }
    else
    {
      return null;
    }
  }

  private String      alternateHRef;

  private String      atomId;

  private String      author;
  private String      content;
  private String      contentType;

  private String      contentURL;
  private int         displayPref;

  private int         downloaded            = STATE_NOT_DOWNLOADED;

  private int         downloadPref;
  private String      error;

  private String      feedAlternateUrl;

  private String      feedAtomId;
  private long        feedId;
  private String      feedTitle;
  private boolean     fitToWidthEnabled     = true;

  private String      hash;

  private long        id                    = -1;
  private boolean     isPinnedStatePending;

  private boolean     isReadStatePending    = false;

  private boolean     isStarred             = false;

  private boolean     isStarredStatePending = false;

  private boolean     javaScriptEnabled;
  private List<Label> labels                = new ArrayList<Label>();

  private ReadState   readState             = ReadState.UNREAD;

  private String      snippet;

  private String      storageLocation;

  private String      title;

  private long        updated;

  public Entry()
  {
    this(-1l);
  }

  public Entry(long id)
  {
    this.id = id;

    mobilizersMap.put("instapaper", "http://www.instapaper.com/m?u=");
    mobilizersMap.put("gwt", "http://www.google.com/gwt/n?u=");
    mobilizersMap.put("readability", "https://www.readability.com/read?url=");
  }

  public void addLabel(Label label)
  {
    labels.add(label);
  }

  public String getAlternateHRef()
  {
    return alternateHRef;
  }

  public final File getAssetsDir(EntryManager em)
  {
    return new File(em.getStorageAdapter().getAbsolutePathForAsset("a" + this.getShortAtomId()));
  }

  public String getAtomId()
  {
    return this.atomId;
  }

  public String getAuthor()
  {
    return author;
  }

  public String getBaseUrl(EntryManager entryManager)
  {
    if (getAlternateHRef() == null)
    {
      return "";
    }

    if (shouldUseMobilizer(entryManager))
    {

      final String mobilizer = entryManager.getMobilizer();
      final String mobilizerUrl = mobilizersMap.get(mobilizer);

      if (mobilizerUrl != null)
      {
        return mobilizerUrl + URLEncoder.encode(getAlternateHRef());
      }
    }

    return getAlternateHRef();
  }

  public String getContent()
  {
    return content;
  }

  long getContentSize()
  {
    long contentSize = 0;
    if (content != null)
    {
      contentSize = content.length();
    }
    return contentSize;
  }

  public String getContentType()
  {
    if (contentType == null)
    {
      return "html";
    }
    return contentType;
  }

  public String getContentURL()
  {
    return contentURL;
  }

  public int getDisplayPref()
  {
    return displayPref;
  }

  public int getDownloaded()
  {
    return downloaded;
  }

  public int getDownloadPref()
  {
    return downloadPref;
  }

  public String getError()
  {
    return error;
  }

  public String getFeedAlternateUrl()
  {
    return feedAlternateUrl;
  }

  public String getFeedAtomId()
  {
    return feedAtomId;
  }

  public long getFeedId()
  {
    return feedId;
  }

  public String getFeedTitle()
  {
    return feedTitle != null ? feedTitle : "Untitled";
  }

  public String getHash()
  {
    return hash;
  }

  public long getId()
  {
    return id;
  }

  List<Label> getLabels()
  {
    return labels;
  }

  public ReadState getReadState()
  {
    return readState;
  }

  public int getResolvedDisplayPref(EntryManager entryManager)
  {
    if (getDisplayPref() == Feed.DISPLAY_PREF_DEFAULT)
    {
      int resolvedDownloadPref = getResolvedDownloadPref(entryManager);
      if ((resolvedDownloadPref == Feed.DOWNLOAD_HEADERS_ONLY) || (resolvedDownloadPref == Feed.DOWNLOAD_PREF_FEED_ONLY))
      {
        return Feed.DISPLAY_PREF_FEED;
      }
      return Feed.DISPLAY_PREF_WEBPAGE;
    }
    return getDisplayPref();
  }

  public int getResolvedDownloadPref(EntryManager entryManager)
  {
    return (getDownloadPref() == Feed.DOWNLOAD_PREF_DEFAULT ? entryManager.getDefaultDownloadPref() : getDownloadPref());

  }

  public String getShortAtomId()
  {
    return getShortAtomId(atomId);
  }

  public String getSnippet()
  {
    if (snippet == null)
    {
      String s = null;
      if (content != null)
      {
        s = content;

        s = U.htmlToText(s);
        if (s.length() > 700)
        {
          s = s.substring(0, 700);
        }

        int idx = s.indexOf(getTitle());
        if ((idx > -1) && (idx < (70 + getTitle().length())))
        {
          // eliminate content that is only the
          // repeated title,
          // but keep the content if the title is repeated
          // later on in the body
          s = null;
        }
      }
      snippet = s;
    }
    return snippet;
  }

  public String getStorageLocation()
  {
    return storageLocation;
  }

  public String getTitle()
  {
    return title != null ? title : "";
  }

  public Date getUpdated()
  {
    return new Date(updated);
  }

  public long getUpdatedInHighResolution()
  {
    return updated;
  }

  public boolean isFitToWidthEnabled()
  {
    return fitToWidthEnabled;
  }

  public boolean isJavaScriptEnabled()
  {
    return javaScriptEnabled;
  }

  public boolean isPinnedStatePending()
  {
    return isPinnedStatePending;
  }

  public final boolean isRead()
  {
    return readState == ReadState.READ;
  }

  public boolean isReadStatePending()
  {
    return isReadStatePending;
  }

  public boolean isStarred()
  {
    return isStarred;
  }

  public boolean isStarredStatePending()
  {
    return isStarredStatePending;
  }

  public void setAlternateHRef(String alternateHRef)
  {
    this.alternateHRef = alternateHRef;
  }

  public void setAtomId(String atomId)
  {
    this.atomId = atomId;
  }

  public void setAuthor(String author)
  {
    this.author = author;
  }

  public void setContent(String content)
  {
    this.content = content;
    setSnippet(null);
  }

  void setContentType(String contentType)
  {
    this.contentType = contentType;
  }

  public void setContentURL(String contentURL)
  {
    this.contentURL = contentURL;
  }

  public void setDisplayPref(int displayPref)
  {
    this.displayPref = displayPref;
  }

  void setDownloaded(int downloaded)
  {
    this.downloaded = downloaded;
  }

  public void setDownloadPref(int downloadPref)
  {
    this.downloadPref = downloadPref;
  }

  void setError(String error)
  {
    this.error = error;
  }

  public void setFeedAlternateUrl(String feedAlternateUrl)
  {
    this.feedAlternateUrl = feedAlternateUrl;
  }

  public void setFeedAtomId(String feedAtomId)
  {
    this.feedAtomId = feedAtomId;
  }

  public void setFeedId(long feedId)
  {
    this.feedId = feedId;
  }

  public void setFeedTitle(String feedTitle)
  {
    this.feedTitle = feedTitle;
  }

  public void setFitToWidthEnabled(boolean enabled)
  {
    this.fitToWidthEnabled = enabled;
  }

  public void setHash(String hash)
  {
    this.hash = hash;
  }

  public void setId(long id)
  {
    this.id = id;
  }

  public void setJavaScriptEnabled(boolean enabled)
  {
    this.javaScriptEnabled = enabled;
  }

  public void setPinnedStatePending(boolean newValue)
  {
    isPinnedStatePending = newValue;
  }

  public void setReadState(ReadState readState)
  {
    this.readState = readState;
  }

  public void setReadStatePending(boolean newValue)
  {
    isReadStatePending = newValue;
  }

  void setSnippet(String snippet)
  {
    this.snippet = snippet;
  }

  public void setStarred(boolean newValue)
  {
    isStarred = newValue;
  }

  void setStarredStatePending(boolean newValue)
  {
    isStarredStatePending = newValue;
  }

  public void setStorageLocation(String location)
  {
    this.storageLocation = location;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public void setUpdated(long entryUpdated)
  {
    this.updated = entryUpdated;
  }

  private boolean shouldUseMobilizer(EntryManager entryManager)
  {
    return getResolvedDownloadPref(entryManager) == Feed.DOWNLOAD_PREF_FEED_AND_MOBILE_WEBPAGE;
  }

  @Override
  public String toString()
  {
    StringBuilder labelsRepresentation = new StringBuilder();
    for (Label label : labels)
    {
      labelsRepresentation.append(label.getName() + ", ");
    }
    String labelsString;
    if (labelsRepresentation.length() > 0)
    {
      labelsString = labelsRepresentation.substring(0, labelsRepresentation.length() - 2);
    }
    else
    {
      labelsString = labelsRepresentation.toString();
    }

    return String.format(
        "Entry title: %s, row-id: %s, atom-id: %s labels: %s, alternate: %s, content-size: %10d, content-type: %s content:\n%s", title, id,
        atomId, labelsString, alternateHRef, getContentSize(), contentType, content);
  }
}
