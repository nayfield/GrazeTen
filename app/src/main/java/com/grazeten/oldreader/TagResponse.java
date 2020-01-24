package com.grazeten.oldreader;

import java.util.List;

public class TagResponse extends ResponseBase
{
  public class Tag
  {
    String id;
    String sortid;
  }

  List<Tag> tags;
}
