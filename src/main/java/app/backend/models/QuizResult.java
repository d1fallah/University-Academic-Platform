package app.backend.models;

import java.sql.Timestamp;

public class QuizResult {

    private int id;
    private int quizId;
    private int studentId;
    private int score;
    private Timestamp submittedAt;
    private boolean isCompleted;

    // Constructors
    public QuizResult() {}

    public QuizResult(int id, int quizId, int studentId, int score, Timestamp submittedAt, boolean isCompleted) {
        this.id = id;
        this.quizId = quizId;
        this.studentId = studentId;
        this.score = score;
        this.submittedAt = submittedAt;
        this.isCompleted = isCompleted;
    }

    public QuizResult(int quizId, int studentId, int score) {
        this.quizId = quizId;
        this.studentId = studentId;
        this.score = score;
        this.isCompleted = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuizId() {
        return quizId;
    }

    public void setQuizId(int quizId) {
        this.quizId = quizId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}
