package com.grazerss.oldreader;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.EncodedQuery;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Query;

public interface OldReaderApi
{
  @GET("/reader/api/0/stream/items/contents?output=json")
  public ItemContentResponse getItemContents(@Header("Authorization") String authToken, @EncodedQuery("i") String idList);

  @GET("/reader/api/0/subscription/list?output=json")
  public SubscriptionResponse getSubscriptionList(@Header("Authorization") String authToken);

  @GET("/reader/api/0/tag/list?output=json")
  public TagResponse getTagList(@Header("Authorization") String authToken);

  @GET("/reader/api/0/unread-count?output=json")
  public UnreadCountResponse getUnreadCounts(@Header("Authorization") String authToken);

  @GET("/reader/api/0/stream/items/ids?output=json;s=user/-/state/com.google/reading-list;xt=user/-/state/com.google/read")
  public ItemsResponse getUnreadItems(@Header("Authorization") String authToken, @Query("c") String continuation, @Query("r") String direction,
      @Query("nt") String newer, @Query("ot") String older, @Query("n") Integer maxItems);

  @GET("/reader/api/0/stream/items/ids?output=json;xt=user/-/state/com.google/read")
  public ItemsResponse getUnreadItemsInFolder(@Header("Authorization") String authToken, @Query("c") String continuation,
      @Query("s") String labelSearchString, @Query("r") String direction, @Query("nt") String newer, @Query("ot") String older,
      @Query("n") Integer maxItems);

  @FormUrlEncoded
  @POST("/accounts/ClientLogin")
  public LoginResp login(@Field("client") String client, @Field("accountType") String type, @Field("service") String service,
      @Field("Email") String email, @Field("Passwd") String password, @Field("output") String output);

  @POST("/reader/api/0/edit-tag?output=json")
  public Response updateArticles(@Header("Authorization") String authToken, @Body UpdateArticlesRequest update);
}
