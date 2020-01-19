package com.grazerss.feedly;

import java.util.List;

public class UnreadCountResponse
{
  public class UnreadCount
  {
    public String  id;
    public Integer count;
    public Long    updated;
  }

  List<UnreadCount> unreadcounts;
}
