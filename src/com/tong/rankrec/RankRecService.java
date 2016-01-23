package com.tong.rankrec;


import java.util.List;

public interface RankRecService {
	List<ProductItemModel> getRankedRecommendations(String query);
}
