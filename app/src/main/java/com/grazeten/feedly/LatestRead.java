package com.grazeten.feedly;

import java.util.List;

public class LatestRead
{
  public class Feeds
  {
    Long   asOf;
    String id;
  }

  public List<String> entries;
  public List<Feeds>  feeds;
}
