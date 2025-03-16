package com.csmsscollege.csmsspoly.Teacher.Model;

public class ParentChatModel {
    private String parentEmail;

    public ParentChatModel() {
        // Default constructor for Firebase
    }

    public ParentChatModel(String parentEmail) {
        this.parentEmail = parentEmail;
    }

    public String getParentEmail() {
        return parentEmail;
    }

    public void setParentEmail(String parentEmail) {
        this.parentEmail = parentEmail;
    }
}

