package com.tong.rankrec;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

public class RankRecServiceTest {

	@Test(expected = IllegalArgumentException.class)
	public void testNullIllegalArgumentExceptionOfGetRankedRecommendations() {
		RankRecService rrSvc = new RankRecServiceImpl();
		rrSvc.getRankedRecommendations(null);
		
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testEmptyIllegalArgumentExceptionOfGetRankedRecommendations() {
		RankRecService rrSvc = new RankRecServiceImpl();
		rrSvc.getRankedRecommendations("  ");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testReturnValueWhenIllegalArgumentExceptionOfGetRankedRecommendations() {
		RankRecService rrSvc = new RankRecServiceImpl();
		List<ProductItemModel> modelList = rrSvc.getRankedRecommendations(null);
		assertEquals(modelList.size(), 0);
	}
	
	@Test
	public void testNoSearchItemReturnOfGetRankedRecommendations() {
		RankRecService rrSvc = new RankRecServiceImpl();
		List<ProductItemModel> modelList = rrSvc.getRankedRecommendations("asdfasdfasdf");
		assertEquals(modelList.size(), 0);
	}
	
	@Test
	public void testSearchReturnedItemsOfGetRankedRecommendations() {
		RankRecService rrSvc = new RankRecServiceImpl();
		List<ProductItemModel> modelList = rrSvc.getRankedRecommendations("ipod");
		assertEquals(modelList.size(), 10);
		assertEquals(isSortedDescending(modelList), true);
		//check special case
		assertEquals(30146246 == modelList.get(0).getItemId(), true);
	}
	
	@Test
	public void testMultiThreadRequestsOfGetRankedRecommendations() {
		RankRecRunnable rrr1 = new RankRecRunnable("ipod");
		RankRecRunnable rrr2 = new RankRecRunnable("iphone");
		ExecutorService pool = Executors.newCachedThreadPool();
		pool.submit(rrr1);
		pool.submit(rrr2);
		pool.shutdown();
	}
	
	private class RankRecRunnable implements Runnable {
		String query;
		
		public RankRecRunnable(String query) {
			this.query = query;
		}
		
	    public void run() {
		 	RankRecServiceImpl rrSvc = new RankRecServiceImpl();
		 	rrSvc.getRankedRecommendations(query);
	    }

	}
	
	private boolean isSortedDescending(List<ProductItemModel> a) {
        for (int i = 1; i < a.size(); i++)
            if (a.get(i-1).compareTo(a.get(i)) > 0) return false;
        return true;
    }
}
