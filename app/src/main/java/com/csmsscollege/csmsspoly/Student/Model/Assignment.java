package com.csmsscollege.csmsspoly.Student.Model;

public class Assignment {
    private String id, title, description, pdfUrl, year, department;

    public Assignment() {}

    public Assignment(String id, String title, String description, String pdfUrl, String year, String department) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.pdfUrl = pdfUrl;
        this.year = year;
        this.department = department;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPdfUrl() { return pdfUrl; }
    public String getYear() { return year; }
    public String getDepartment() { return department; }

    public void setId(String key) {

    }
}
