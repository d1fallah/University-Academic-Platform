package app.backend.models;

import java.util.ArrayList;
import java.util.List;

public class Question {

    private int id;
    private int quizId;
    private String questionText;
    private List<Answer> answers; // To store answers temporarily

    // Constructors
    public Question() {
        this.answers = new ArrayList<>();
    }

    public Question(int id, int quizId, String questionText) {
        this.id = id;
        this.quizId = quizId;
        this.questionText = questionText;
        this.answers = new ArrayList<>();
    }

    public Question(int quizId, String questionText) {
        this.quizId = quizId;
        this.questionText = questionText;
        this.answers = new ArrayList<>();
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

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
    
    // Methods to manage answers
    public List<Answer> getAnswers() {
        return answers;
    }
    
    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }
    
    public void addAnswer(Answer answer) {
        if (this.answers == null) {
            this.answers = new ArrayList<>();
        }
        this.answers.add(answer);
    }
}
