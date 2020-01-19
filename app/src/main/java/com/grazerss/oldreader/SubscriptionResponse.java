package com.grazerss.oldreader;

import java.util.List;

public class SubscriptionResponse
{
  public class Subscriptions
  {
    public class Category
    {
      String id;
      String label;
    }

    List<Category> categories;
    String         htmlUrl;
    String         iconUrl;
    String         id;
    String         sortid;
    String         title;
    String         url;
  }

  List<Subscriptions> subscriptions;
}
