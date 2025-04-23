package app.backend.models;

import java.sql.Timestamp;

public class ExerciseSubmission {

    private int id;
    private int exerciseId;
    private int studentId;
    private String submissionText;
    private Timestamp submittedAt;

    // Constructors
    public ExerciseSubmission() {}

    public ExerciseSubmission(int id, int exerciseId, int studentId, String submissionText, Timestamp submittedAt) {
        this.id = id;
        this.exerciseId = exerciseId;
        this.studentId = studentId;
        this.submissionText = submissionText;
        this.submittedAt = submittedAt;
    }

    public ExerciseSubmission(int exerciseId, int studentId, String submissionText) {
        this.exerciseId = exerciseId;
        this.studentId = studentId;
        this.submissionText = submissionText;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(int exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSubmissionText() {
        return submissionText;
    }

    public void setSubmissionText(String submissionText) {
        this.submissionText = submissionText;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }
}
