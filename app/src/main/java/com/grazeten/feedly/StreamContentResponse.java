package com.grazeten.feedly;

import java.util.List;

public class StreamContentResponse
{
  public class Item
  {
    public class Alternate
    {
      public String href;
      public String type;
    }

    public class Content
    {
      public String direction;
      public String content;
    }

    public class Origin
    {
      public String streamId;
      public String title;
      public String htmlUrl;
    }

    public String           id;
    public boolean          unread;
    public List<Categories> categories;
    public List<Categories> tags;
    public String           title;
    public Long             published;
    public Long             updated;
    public Long             crawled;
    public Content          summary;
    public Content          content;
    public String           author;
    public String           engagement;
    public Origin           origin;
    public List<Alternate>  alternate;
  }

  public String     direction;
  public String     id;
  public String     continuation;
  public Long       updated;
  public List<Item> items;
}
