package com.grazeten.oldreader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit.mime.TypedOutput;

public class UpdateArticlesRequest implements TypedOutput
{
  public enum MarkType
  {
    READ, UNREAD;
  }

  private static final String         MARK_READ   = "user/-/state/com.google/read";
  private static final String         MARK_UNREAD = "user/-/state/com.google/read";
  private static final String         READ_ID     = "a";
  private static final String         UNREAD_ID   = "r";

  private final ByteArrayOutputStream content     = new ByteArrayOutputStream();
  private List<String>                ids         = new ArrayList<String>();

  public UpdateArticlesRequest(MarkType markType) throws IOException
  {
    super();

    if (markType == null)
    {
      throw new IOException("Mark type must be set");
    }

    switch (markType)
    {
      case READ:
        addField(READ_ID, MARK_READ);
        break;
      case UNREAD:
        addField(UNREAD_ID, MARK_UNREAD);
        break;
    }
  }

  public void addField(String name, String value)
  {
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    if (value == null)
    {
      throw new NullPointerException("value");
    }
    if (content.size() > 0)
    {
      content.write('&');
    }
    try
    {
      name = URLEncoder.encode(name, "UTF-8");
      value = URLEncoder.encode(value, "UTF-8");

      content.write(name.getBytes("UTF-8"));
      content.write('=');
      content.write(value.getBytes("UTF-8"));
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public void addId(String id)
  {
    ids.add(id);
    addField("i", id);
  }

  @Override
  public String fileName()
  {
    return null;
  }

  public List<String> getIds()
  {
    return ids;
  }

  @Override
  public long length()
  {
    return content.size();
  }

  @Override
  public String mimeType()
  {
    return "application/x-www-form-urlencoded; charset=UTF-8";
  }

  @Override
  public void writeTo(OutputStream out) throws IOException
  {
    out.write(content.toByteArray());
  }
}
