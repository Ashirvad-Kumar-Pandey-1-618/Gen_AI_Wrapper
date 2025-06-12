package com.example.GenAI.model;


public class RequestModel {
    private String text;

    public RequestModel() {}
    public RequestModel(String text) { this.text = text; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
