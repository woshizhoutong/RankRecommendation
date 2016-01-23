package com.tong.rankrec;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class RankRecServiceImpl implements RankRecService{
	
	private final String apiKey = "xd6vddppr8qjck8y4kqsvh4a";
	private final int maxRecommendationToReturn = 10;
	private static long[] requestsTimeArr= new long[5]; 
	
	@Override
	public List<ProductItemModel> getRankedRecommendations(String query) {
		if (query == null) throw new IllegalArgumentException("query string cannot be null");
		if (query.trim().length() == 0) throw new IllegalArgumentException("query string cannot be empty");
		
		String searchURL = "http://api.walmartlabs.com/v1/search?"
				+ "apiKey=" + apiKey + "&format=json&numItems=1&query=" + query;
		try {
			pauseHelper();
			String searchResJsonStr = IOUtils.toString(new URL(searchURL));
			
			JSONObject searchResJsonObj = (JSONObject) JSONValue.parseWithException(searchResJsonStr);
			
		    //check if there is any search result, if not return empty list
			Long numItems = (Long) searchResJsonObj.get("numItems");
			if (numItems == 0) return new ArrayList<ProductItemModel>();
			
			JSONArray searchResItems = (JSONArray) searchResJsonObj.get("items");
			JSONObject searchResFirstItem = (JSONObject) searchResItems.get(0);
			
			String recURL = "http://api.walmartlabs.com/v1/nbp?"
					+ "apiKey=" + apiKey + "&itemId=" + searchResFirstItem.get("itemId");
			
			pauseHelper();
			String	recResJsonStr = IOUtils.toString(new URL(recURL));
			
			JSONArray recResItems = (JSONArray) JSONValue.parseWithException(recResJsonStr);
			
			//get review for the recommendations (max number is 10). 
		
			Integer numOfRecommendation = getNumOfRecommendationToReturn(recResItems.size());
			ExecutorService pool = Executors.newCachedThreadPool();
			List<Future<ProductItemModel>> futures = new ArrayList<Future<ProductItemModel>>();
			
			//multithread for requesting reviews
			List<ProductItemModel> modelList = new ArrayList<ProductItemModel>();
			
			for (int i = 0; i < numOfRecommendation; i++) {
				JSONObject jsonObj = (JSONObject) recResItems.get(i);
				Callable<ProductItemModel> itemReviewGetter = new ItemReviewGetter(jsonObj);
				
				pauseHelper();
				futures.add(pool.submit(itemReviewGetter));
			}
			for (Future<ProductItemModel> future : futures) {
				modelList.add(future.get());
		    }
			pool.shutdown();
			
			Collections.sort(modelList);
			return modelList;
			
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} 
		return new ArrayList<ProductItemModel>();
	}
	
	//if the number of total recommendation is more than maxNumOfRecommendationsToReturn, 
	//which is 10, then we return 10 ranked recommendations
	//otherwise we return the all recommendations found by the server.
	private Integer getNumOfRecommendationToReturn(Integer totalRec) {
		if (totalRec < maxRecommendationToReturn) return totalRec;
		else return maxRecommendationToReturn;
	}
	
	private class ItemReviewGetter implements Callable<ProductItemModel>  {
		private JSONObject jsonObject;
		
		public ItemReviewGetter(JSONObject jsonObject) {
			this.jsonObject = jsonObject;
		}

		@Override
		public ProductItemModel call() throws MalformedURLException, IOException, ParseException {
			Long itemId = (Long) jsonObject.get("itemId");
			String name = (String) jsonObject.get("name");
			String reviewURL = "http://api.walmartlabs.com/v1/reviews/" + itemId
					+ "?apiKey=" + apiKey + "&format=json";
			String reviewResJsonStr = IOUtils.toString(new URL(reviewURL));
			JSONObject reviewResJsonObj = (JSONObject) JSONValue.parseWithException(reviewResJsonStr);
			JSONObject reviewStatResJsonObj = (JSONObject) reviewResJsonObj.get("reviewStatistics");
			
			Double reviewRatingAvg = Double.parseDouble((String) reviewStatResJsonObj.get("averageOverallRating"));
			Double reviewRatingRange = Double.parseDouble((String) reviewStatResJsonObj.get("overallRatingRange"));
			Double ratingScore = reviewRatingAvg / reviewRatingRange;
			return new ProductItemModel(itemId, name, ratingScore);
		}
	}
	
	// this method help check if the it needs to be pause before sending the following request
	// I use an array to store the request time. If the time difference between the oldest one 
	// and the newest one is less than 1 second, then pause until one second; I give the pause time up to 1.1 second
	// just in case of the network delay affects the calculation;
	private static synchronized void pauseHelper() throws InterruptedException {
		for (int i = 0; i < requestsTimeArr.length - 1; i++) {
			requestsTimeArr[i] = requestsTimeArr[i + 1];
		}
		requestsTimeArr[requestsTimeArr.length - 1] = System.currentTimeMillis();
		
		long timeDiff = requestsTimeArr[requestsTimeArr.length - 1] - requestsTimeArr[0];
		if (timeDiff < 1000 && !Arrays.asList(requestsTimeArr).contains(0)){
			TimeUnit.MILLISECONDS.sleep(1100 - timeDiff);
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		 	System.out.println("Start!");
		 	RankRecServiceImpl rrSvc = new RankRecServiceImpl();
		 	Stopwatch timer1 = new Stopwatch();
		 	List<ProductItemModel> modelList = rrSvc.getRankedRecommendations(args[0]);
		 	double time1 = timer1.elapsedTime();
		 	for(int i = 0; i < modelList.size(); i++) {
		 		ProductItemModel model = modelList.get(i);
		 		System.out.printf("Item" + i + ": " + " itemId: " + model.getItemId() 
		 				+ " name: " + model.getName() + " review score: %.2f %n",  model.getReviewRatingScore());
		 	}
		 	System.out.println("time used by the query: " + time1);
	        System.out.println("Done!");
	}

}