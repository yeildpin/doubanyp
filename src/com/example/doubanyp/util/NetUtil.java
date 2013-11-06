package com.example.doubanyp.util;

import com.google.gdata.client.douban.DoubanService;

public class NetUtil {
	// APIKEY
	public static final String apiKey = "042cbf0c503dee720c863d24f0b9cb56";
	// secret
	public static final String secret = "4b08025f5bc7f1a2";
	public static final String callback = "app://mycollback";

	// 图片加载管理器
	public static AsyncImageLoader asyncImageLoader = new AsyncImageLoader();
	public static DoubanService doubanService;

}
