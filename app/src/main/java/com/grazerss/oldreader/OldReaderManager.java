package com.grazerss.oldreader;

import retrofit.RestAdapter;
import retrofit.client.Response;

import com.grazerss.AuthenticationFailedException;
import com.grazerss.oldreader.ItemsResponse.Item;

public class OldReaderManager
{
  private static final String FOLDER_SEARCH_PREFIX = "user/-/label/";
  // private static final String SERVER = "http://10.1.0.110:8080";
  private static final String SERVER               = "https://theoldreader.com";
  private OldReaderApi        api                  = new RestAdapter.Builder().setEndpoint(SERVER).build().create(OldReaderApi.class);
  private String              authToken            = null;

  private String getAuthHeader()
  {
    return "GoogleLogin auth=" + authToken;
  }

  public ItemContentResponse getItemContents(ItemsResponse itemResp)
  {
    StringBuilder items = new StringBuilder();

    for (Item item : itemResp.itemRefs)
    {
      if (items.length() == 0)
      {
        items.append(item.id);
      }
      else
      {
        items.append("&i=" + item.id);
      }
    }

    return api.getItemContents(getAuthHeader(), items.toString());
  }

  public SubscriptionResponse getSubscriptionList()
  {
    return api.getSubscriptionList(getAuthHeader());
  }

  public TagResponse getTagList()
  {
    return api.getTagList(getAuthHeader());
  }

  public UnreadCountResponse getUnreadCounts()
  {
    return api.getUnreadCounts(getAuthHeader());
  }

  public ItemsResponse getUnreadItems(String continuation, String folderName, boolean newestFirst, Long lastUpdate, Integer maxItems)
  {
    String direction = null;
    String ot = null;
    String nt = null;

    if (newestFirst)
    {
      ot = lastUpdate.toString();
    }
    else
    {
      direction = "o";
      nt = lastUpdate.toString();
    }

    if (folderName == null)
    {
      return api.getUnreadItems(getAuthHeader(), continuation, direction, nt, ot, maxItems);
    }
    else
    {
      return api.getUnreadItemsInFolder(getAuthHeader(), continuation, FOLDER_SEARCH_PREFIX + folderName, direction, nt, ot, maxItems);
    }
  }

  public boolean haveAuthToken()
  {
    return authToken != null;
  }

  public LoginResp login(String email, String password) throws AuthenticationFailedException
  {
    LoginResp data = api.login("GrazeRSS", "HOSTED_OR_GOOGLE", "reader", email, password, "json");
    authToken = data.Auth;

    if ((authToken == null) || (data.errors != null))
    {
      throw new AuthenticationFailedException(data.errors.get(0));
    }

    return data;
  }

  public Response updateArticles(UpdateArticlesRequest update)
  {
    return api.updateArticles(getAuthHeader(), update);
  }
}
