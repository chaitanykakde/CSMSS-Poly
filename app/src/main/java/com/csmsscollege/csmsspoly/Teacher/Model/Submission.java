package com.csmsscollege.csmsspoly.Teacher.Model;

public class Submission {
    private String studentEnrollment;
    private String studentEmail;
    private long submissionTime;
    private String answerUrl;
    private String assignmentDescription;

    // Required empty constructor for Firebase
    public Submission() {}

    public Submission(String studentEnrollment, String studentEmail, long submissionTime, String answerUrl, String assignmentDescription) {
        this.studentEnrollment = studentEnrollment;
        this.studentEmail = studentEmail;
        this.submissionTime = submissionTime;
        this.answerUrl = answerUrl;
        this.assignmentDescription = assignmentDescription;
    }

    public String getStudentEnrollment() {
        return studentEnrollment;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public long getSubmissionTime() {
        return submissionTime;
    }

    public String getAnswerUrl() {
        return answerUrl;
    }

    public String getAssignmentDescription() {
        return assignmentDescription;
    }
}
