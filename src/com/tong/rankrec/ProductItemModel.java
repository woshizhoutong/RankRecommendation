package com.tong.rankrec;



public class ProductItemModel implements Comparable<ProductItemModel>{
	private Long itemId;
	private String name;
	private Double reviewRatingScore;
	
	public ProductItemModel() {}
	
	public ProductItemModel(Long itemId, String name, Double reviewRatingScore) {
		this.itemId = itemId;
		this.name = name;
		this.reviewRatingScore = reviewRatingScore;
	}
	
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getReviewRatingScore() {
		return reviewRatingScore;
	}
	public void setReviewRatingScore(Double reviewRatingScore) {
		this.reviewRatingScore = reviewRatingScore;
	}

	//define natural order as descending order:
	public int compareTo(ProductItemModel that) {
		if (that == null) throw new java.lang.NullPointerException("cannot compare to null!");
		if (this.reviewRatingScore > that.reviewRatingScore) return -1;
		if (this.reviewRatingScore < that.reviewRatingScore) return 1;
		return 0;
	}
	
}
