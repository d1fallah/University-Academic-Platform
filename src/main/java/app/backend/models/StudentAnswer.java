package app.backend.models;

public class StudentAnswer {
    private int id;
    private int quizResultId;
    private int questionId;
    private Integer selectedAnswerId;
    private boolean isCorrect;
    
    // Constructors
    public StudentAnswer() {}
    
    public StudentAnswer(int id, int quizResultId, int questionId, Integer selectedAnswerId, boolean isCorrect) {
        this.id = id;
        this.quizResultId = quizResultId;
        this.questionId = questionId;
        this.selectedAnswerId = selectedAnswerId;
        this.isCorrect = isCorrect;
    }
    
    public StudentAnswer(int quizResultId, int questionId, Integer selectedAnswerId, boolean isCorrect) {
        this.quizResultId = quizResultId;
        this.questionId = questionId;
        this.selectedAnswerId = selectedAnswerId;
        this.isCorrect = isCorrect;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getQuizResultId() {
        return quizResultId;
    }
    
    public void setQuizResultId(int quizResultId) {
        this.quizResultId = quizResultId;
    }
    
    public int getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }
    
    public Integer getSelectedAnswerId() {
        return selectedAnswerId;
    }
    
    public void setSelectedAnswerId(Integer selectedAnswerId) {
        this.selectedAnswerId = selectedAnswerId;
    }
    
    public boolean isCorrect() {
        return isCorrect;
    }
    
    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
} 