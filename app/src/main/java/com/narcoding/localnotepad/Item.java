package com.narcoding.localnotepad;

/**
 * Created by Naim on 3.11.2016.
 */
public class Item {

    private int id;
    private String title;

    public Item(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
