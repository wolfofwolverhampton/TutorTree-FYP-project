package com.javainternal.MCQ.Model;

import java.io.Serializable;
import java.util.List;

public class QuestionSetModel implements Serializable {
    private String id;
    private String title;
    private String description;
    private List<String> questionIds;

    public QuestionSetModel() {}

    public QuestionSetModel(String id, String title, String description, List<String> questionIds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.questionIds = questionIds;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getQuestionIds() { return questionIds; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setQuestionIds(List<String> questionIds) { this.questionIds = questionIds; }
}
