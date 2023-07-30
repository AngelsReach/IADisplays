package com.vergilprime.iadisplays.models;

public enum SnapTo {
	// enumeration of the possible snap to values
	TWO("2"), ONE("1"), HALF("1/2"), QUARTER("1/4"), EIGHTH("1/8"), SIXTEENTH("1/16");

	final private String snapTo;

	SnapTo(String snapTo) {
		this.snapTo = snapTo;
	}

	public String get() {
		return snapTo;
	}
}
