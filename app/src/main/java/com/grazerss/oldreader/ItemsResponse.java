package com.grazerss.oldreader;

import java.util.List;

public class ItemsResponse extends ResponseBase
{
  public class Item
  {
    String id;
    Long   timestampUsec;
  }

  String     continuation;
  List<Item> itemRefs;
}
