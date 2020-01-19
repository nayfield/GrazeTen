package com.grazerss.feedly;

import java.util.List;

public class SubscribeFeedRequest
{
  public String           id;
  public String           title;

  public List<Categories> categories;

  public SubscribeFeedRequest(String id, String title, List<Categories> categories)
  {
    super();
    this.id = id;
    this.title = title;
    this.categories = categories;
  }
}
