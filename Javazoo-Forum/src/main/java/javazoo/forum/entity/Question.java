package javazoo.forum.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name="questions")
public class Question {
    private Integer id;
    private String title;
    private String content;
    private User author;
    private Answer lastAnswer;
    private Date creationDate;
    private Set<Answer> answers;
    private Category category;
    private Subcategory subcategory;
    private List<Tag> tags;

    public Question(String title, String content, User author, Category category,
                    Subcategory subcategory, List<Tag>tags){
        this.title = title;
        this.content = content;
        this.author = author;
        this.creationDate = new Date();
        this.answers = new HashSet<>();
        this.category = category;
        this.subcategory = subcategory;
        this.lastAnswer= null;
        this.tags = tags;
    }

    public Question(){
        this.answers = new HashSet<>();
        this.tags = new ArrayList<>();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Column(nullable = false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(columnDefinition = "text", nullable = false)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name = "authorId")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @OneToOne()
    @JoinColumn(name="lastAnswerId")
    public Answer getLastAnswer() {
        return lastAnswer;
    }

    public void setLastAnswer(Answer lastAnswer) {
        this.lastAnswer = lastAnswer;
    }

    @Column
    @Type(type = "timestamp")
    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @OneToMany(mappedBy = "question",cascade = CascadeType.REMOVE)
    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name="categoryId")
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @ManyToOne()
    @JoinColumn(nullable = false, name="subcategoryId" )
    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
    }

    @ManyToMany()
    @JoinColumn(table = "questions_tags")
    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
