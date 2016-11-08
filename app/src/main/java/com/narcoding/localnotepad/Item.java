package com.narcoding.localnotepad;

/**
 * Created by Naim on 3.11.2016.
 */
public class Item {

    private int id;
    private String title;
    private String location;

    public Item(int id, String title,String location) {
        this.id = id;
        this.title = title;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }
}
