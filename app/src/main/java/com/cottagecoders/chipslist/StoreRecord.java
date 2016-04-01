package com.cottagecoders.chipslist;

public class StoreRecord {

	String storeName;
	int storeNumber;
	double taxRate;

	@Override
	public String toString(){
		return storeName + " ("+storeNumber+")";
	}

	public StoreRecord(String storeName, int storeNumber, double taxRate) {
		this.storeName = storeName;
		this.storeNumber = storeNumber;
		this.taxRate = taxRate;
	}

	public String getStoreName() {
		return storeName;
	}

	public int getStoreNumber() {
		return storeNumber;
	}
	
	public double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public void setStoreNumber(int storeNumber) {
		this.storeNumber = storeNumber;
	}
}
