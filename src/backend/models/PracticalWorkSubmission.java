package backend.models;

import java.sql.Timestamp;

public class PracticalWorkSubmission {

    private int id;
    private int practicalWorkId;
    private int studentId;
    private String filePath;
    private Timestamp submittedAt;

    // Constructors
    public PracticalWorkSubmission() {}

    public PracticalWorkSubmission(int id, int practicalWorkId, int studentId, String filePath, Timestamp submittedAt) {
        this.id = id;
        this.practicalWorkId = practicalWorkId;
        this.studentId = studentId;
        this.filePath = filePath;
        this.submittedAt = submittedAt;
    }

    public PracticalWorkSubmission(int practicalWorkId, int studentId, String filePath) {
        this.practicalWorkId = practicalWorkId;
        this.studentId = studentId;
        this.filePath = filePath;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPracticalWorkId() {
        return practicalWorkId;
    }

    public void setPracticalWorkId(int practicalWorkId) {
        this.practicalWorkId = practicalWorkId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }
}
