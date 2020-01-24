package com.grazeten.feedly;

import java.util.List;

public class TagRequest
{
  List<String> entryIds;

  public TagRequest(List<String> entryIds)
  {
    super();
    this.entryIds = entryIds;
  }
}
