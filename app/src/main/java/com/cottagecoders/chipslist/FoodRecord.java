package com.cottagecoders.chipslist;

public class FoodRecord {
	String itemName;
	String location;
	String size;
	double price;
	int itemNum;
	int storeNum;
	double quantity;
	int sequence;
	boolean isChecked;
	boolean isTaxed;

	public FoodRecord(String item_name, String location_name, String size_str,
			double prix, int item_num, int store_num, int checked, int taxed,
			double qty, int sequence) {

		itemName = item_name.trim();
		location = location_name.trim();
		size = size_str.trim();
		price = prix;
		itemNum = item_num;
		storeNum = store_num;

		if (checked == 0) {
			isChecked = false;
		} else {
			isChecked = true;
		}

		if (taxed == 0) {
			isTaxed = false;
		} else {
			isTaxed = true;
		}

		quantity = qty;
		this.sequence = sequence;
	}

	public String toString() {
		return "foodRecord(): itemName: " + itemName + " location " + location + " size "
				+ size + " price " + price + " itemNum: " + itemNum
				+ " storeNum " + storeNum + " checked? " + isChecked
				+ " taxed? " + isTaxed + " sequence " + sequence ;
	}

	public int getItemNum() {
		return itemNum;
	}

	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
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

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public boolean isTaxed() {
		return isTaxed;
	}

	public void setTaxed(boolean isTaxed) {
		this.isTaxed = isTaxed;
	}
}
