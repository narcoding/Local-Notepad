package com.narcoding.localnotepad;

/**
 * Created by Naim on 3.11.2016.
 */
public class Item {

    private int id;
    private String title;
    private String content;
    private String location;
    private byte[] image;
    private byte[] voice;

    public Item(int id, String title, String content, String location, byte[] image,byte[] voice) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.location = location;
        this.image = image;
        this.voice=voice;
    }

    public void setId(int id) {this.id = id;}
    public void setTitle(String title) {this.title = title;}
    public void setContent(String content) {this.content = content;}
    public void setLocation(String location) {this.location = location;}
    public void setImage(byte[] image) {this.image = image;}
    public void setVoice(byte[] voice) {this.voice = voice;}


    public int getId() {return id;}
    public String getTitle() {return title;}
    public String getContent() {return content;}
    public String getLocation() {return location;}
    public byte[] getImage() {return image;}
    public byte[] getVoice() {return voice;}

}
