package com.grazerss.feedly;

import java.util.List;

public class SearchFeedsResponse
{
  public class Results
  {
    public String  title;
    public String  website;
    public String  feedId;
    public String  velocity;
    public Integer subscribers;
    public boolean curated;
    public boolean featured;
  }

  public String        hint;
  public List<String>  related;
  public List<Results> results;
}
