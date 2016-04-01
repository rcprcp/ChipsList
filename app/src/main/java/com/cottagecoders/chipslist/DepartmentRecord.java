package com.cottagecoders.chipslist;

public class DepartmentRecord {
	int sequence;
	int serial;
	String location;
	
	public DepartmentRecord(int serial, int sequence, String location){
		this.serial = serial;
		this.sequence = sequence;
		this.location = location;
	}

	public String toString() {
		return "seq " + sequence + " location: " + location ;
	}
	
	public int getSequence() {
		return sequence;
	}

	public void OLDsetSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getLocation() {
		return location;
	}

	public void OLDsetLocation(String location) {
		this.location = location;
	}

	public int getSerial() {
		return serial;
	}

	public void OLDsetSerial(int serial) {
		this.serial = serial;
	}
}
