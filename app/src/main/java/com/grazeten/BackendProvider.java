package com.grazeten;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;

import com.grazeten.jobs.Job;

public interface BackendProvider
{
  class AuthenticationExpiredException extends Exception
  {
  }

  public static class AuthToken
  {
    public enum AuthType
    {
      AUTH_STANDALONE, AUTH
    };

    private AuthType type;
    private String   authToken;

    public AuthToken(AuthType type, String authToken)
    {
      this.setType(type);
      this.authToken = authToken;
    }

    public String getAuthToken()
    {
      return authToken;
    }

    AuthType getAuthType()
    {
      return getType();
    }

    public AuthType getType()
    {
      return type;
    }

    public void setType(AuthType type)
    {
      this.type = type;
    }

    @Override
    public String toString()
    {
      if (authToken != null)
      {
        return "AuthToken " + authToken.substring(0, 4) + " of type " + getType() + ".";
      }
      else
      {
        return "";
      }
    }
  }

  class ServerBadRequestException extends Exception
  {
  }

  public static class StateChange
  {
    public static final int OPERATION_REMOVE = 0;
    public static final int OPERATION_ADD    = 1;
    public static final int STATE_READ       = 2;
    public static final int STATE_STARRED    = 3;

    private int             state;
    private int             operation;

    private String          atomId;

    public StateChange(String atomId, int state, int operation)
    {
      this.atomId = atomId;
      this.state = state;
      this.operation = operation;
    }

    String getAtomId()
    {
      return atomId;
    }

    int getOperation()
    {
      return operation;
    }

    int getState()
    {
      return state;
    }

    @Override
    public String toString()
    {
      String stateLabel = "State?";
      switch (state)
      {
        case STATE_READ:
          stateLabel = "read";
          break;
        case STATE_STARRED:
          stateLabel = "starred";
          break;
      }

      String operationLabel = operation == OPERATION_ADD ? "add" : "remove";
      return "State: " + operationLabel + " " + stateLabel + " for " + getAtomId() + ".";
    }

  }

  public class SyncAPIException extends Exception
  {

    private static final long serialVersionUID = -4038203280616398790L;

    public SyncAPIException(String message)
    {
      super(message);
    }

    public SyncAPIException(String message, Throwable rootCause)
    {
      super(message, rootCause);
    }

  }

  final static long          ONE_DAY_IN_MS  = 1000 * 60 * 60 * 24;

  public static final String GRAZERSS_LABEL = "grazerss";

  public boolean authenticate(Context context, String email, String password, String captchaToken, String captchaAnswer)
      throws ClientProtocolException, IOException, AuthenticationFailedException;

  /**
   * differentialUpdateOfArticlesStates is where the actual exact syncing magic happens
   * 
   * @throws AuthenticationExpiredException
   */
  public void differentialUpdateOfArticlesStates(final EntryManager entryManager, Job job, String stream, String excludeState,
      ArticleDbState articleDbState) throws SAXException, IOException, ParserConfigurationException, ServerBadRequestException,
      ServerBadRequestException, AuthenticationExpiredException;

  public List<DiscoveredFeed> discoverFeeds(final String query) throws SyncAPIException, IOException, ServerBadRequestException,
      ParserConfigurationException, SAXException, ServerBadRequestException, AuthenticationExpiredException;

  public int fetchNewEntries(final EntryManager entryManager, final SyncJob job, boolean manualSync) throws ClientProtocolException,
      IOException, NeedsSessionException, SAXException, IllegalStateException, ParserConfigurationException, FactoryConfigurationError,
      SyncAPIException, ServerBadRequestException, AuthenticationExpiredException;

  public Class getLoginClass();

  public String getServiceName();

  public String getServiceUrl();

  public void logout();

  public void startLogin(Activity activity, Context context);

  public boolean submitSubscribe(String url2subscribe) throws SyncAPIException;

  public int synchronizeArticles(EntryManager entryManager, SyncJob syncJob) throws MalformedURLException, IOException,
      ParserConfigurationException, FactoryConfigurationError, SAXException, ParseException, NeedsSessionException, ParseException;

  public void unsubscribeFeed(String feedAtomId) throws IOException, NeedsSessionException, SyncAPIException;

  public void updateSubscriptionList(EntryManager entryManager, Job job) throws IOException, ParserConfigurationException, SAXException,
      ServerBadRequestException, AuthenticationExpiredException;

}