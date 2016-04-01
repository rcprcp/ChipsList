package com.cottagecoders.chipslist;

public class PriceRecord {
	
	
	double price;
	double quantity;
	String size;
	int storeNum;
	int itemNum;
	
	public PriceRecord(int itemNum, int storeNum, double price, double quantity, String size){
		this.price = price ;
		this.itemNum = itemNum;
		this.storeNum = storeNum;
		this.quantity = quantity;
		this.size = size;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getStoreNum() {
		return storeNum;
	}

	public void setStoreNum(int storeNum) {
		this.storeNum = storeNum;
	}

	public int getItemNum() {
		return itemNum;
	}

	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

}
