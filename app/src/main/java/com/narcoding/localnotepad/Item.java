package com.narcoding.localnotepad;

/**
 * Created by Naim on 3.11.2016.
 */
public class Item {

    private int id;
    private String title;
    private String location;
    private String content;

    public Item(int id, String title, String content, String location) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.location = location;
    }

    public int getId() {return id;}
    public String getTitle() {return title;}
    public String getContent() {return content;}
    public String getLocation() {return location;}
}
