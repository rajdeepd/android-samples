package com.android.example.contact.data;

public class Pair {
	public String key;
	public String value;
	public Pair(String key,String value){
		this.key = key;
		this.value = value;
	}
	public String toString() {
		return "{" +  key + ", " + value + "}";
	}
}
