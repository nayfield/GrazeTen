package com.grazerss.oldreader;

import java.util.List;

public class ItemContentResponse extends ResponseBase
{
  public class Item
  {
    public class Link
    {
      String href;
      String type;
    }

    public class Origin
    {
      String htmlUrl;
      String streamId;
      String title;
    }

    public class Summary
    {
      String content;
      String direction;
    }

    List<Link>   alternate;
    List<Link>   canonical;
    List<String> categories;
    Long         crawlTimeMsec;
    String       id;
    Origin       origin;
    Long         published;
    Summary      summary;
    String       title;
  }

  String     description;
  String     direction;
  String     id;
  List<Item> items;
  String     title;
}
