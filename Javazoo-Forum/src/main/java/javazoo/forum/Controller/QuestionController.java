package javazoo.forum.controller;

import javazoo.forum.bindingModel.QuestionBindingModel;
import javazoo.forum.entity.*;
import javazoo.forum.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class QuestionController {

    @Autowired
    private AnswersRepository answersRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping("/question/create")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model){

        List<Category> categories = this.categoryRepository.findAllByOrderByOrderNoAsc();
        List<Subcategory> subcategories = this.subcategoryRepository.findAllByOrderByOrderNoAsc();

        model.addAttribute("view", "question/create");
        model.addAttribute("categories", categories);
        model.addAttribute("subcategories", subcategories);

        return "base-layout";
    }

    @PostMapping("/question/create")
    @PreAuthorize("isAuthenticated()")
    public String createProcess(QuestionBindingModel questionBindingModel,Model model){

        List<String> errors = validateQuestionFields(questionBindingModel);
        if(!errors.isEmpty()){

            List<Category> categories = this.categoryRepository.findAllByOrderByOrderNoAsc();
            List<Subcategory> subcategories = this.subcategoryRepository.findAllByOrderByOrderNoAsc();

            model.addAttribute("errors", errors);
            model.addAttribute("title", questionBindingModel.getTitle());
            model.addAttribute("content", questionBindingModel.getContent());
            model.addAttribute("categories", categories);
            model.addAttribute("subcategories", subcategories);
            model.addAttribute("view", "/question/create");
            return "base-layout";
        }



        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByUsername(user.getUsername());
        Category category = this.categoryRepository.findOne(questionBindingModel.getCategoryId());
        Subcategory subcategory = this.subcategoryRepository.findOne(questionBindingModel.getSubcategoryId());
        List<Tag> tags= this.findTagsFromString(questionBindingModel.getTagString());

        Question questionEntity = new Question(
                        questionBindingModel.getTitle(),
                        questionBindingModel.getContent(),
                        userEntity,
                        category,
                        subcategory,
                        tags
        );

        this.questionRepository.saveAndFlush(questionEntity);

        return "redirect:/";
    }

    @GetMapping("/question/{id}")
    public String details(Model model, @PathVariable Integer id, @PageableDefault(value = 6) Pageable pageable){
        if (!this.questionRepository.exists(id)){
            return "redirect:/";
        }

        if(!(SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)){

            UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            User entityUser = this.userRepository.findByUsername(principal.getUsername());

            model.addAttribute("user", entityUser);
        }

        Question question = this.questionRepository.findOne(id);

        Page<Answer> answers = this.answersRepository.findByQuestionOrderByCreationDateAsc(question, pageable);
        List<Category> categories = this.categoryRepository.findAllByOrderByOrderNoAsc();
        List<Subcategory> subcategories = this.subcategoryRepository.findAllByOrderByOrderNoAsc();

        Subcategory subcategory = question.getSubcategory();
        Category category = subcategory.getCategory();

        List<Tag> allTags = this.tagRepository.findAll();
        allTags.sort((Tag t1,Tag t2)-> t2.getQuestions().size()-t1.getQuestions().size());
        List<Tag> tags = allTags.stream().limit(20).collect(Collectors.toList());

        model.addAttribute("question", question);
        model.addAttribute("answers", answers);
        model.addAttribute("subcategoryId", subcategory.getId());
        model.addAttribute("subcategories", subcategories);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryId", category.getId());
        model.addAttribute("tags",  tags);
        model.addAttribute("view", "question/details");
        model.addAttribute("size", 6);

        return "base-layout";
    }

    @GetMapping("/question/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.questionRepository.exists(id)){
            return "redirect:/";
        }

        Question question = this.questionRepository.findOne(id);

        if(!isUserAuthorOrAdmin(question)){
            return "redirect:/question/"+id;
        }

        List<Category> categories = this.categoryRepository.findAllByOrderByOrderNoAsc();
        List<Subcategory> subcategories = this.subcategoryRepository.findAllByOrderByOrderNoAsc();

        String tagString = question.getTags().stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));

        model.addAttribute("view", "question/edit");
        model.addAttribute("question", question);
        model.addAttribute("tags", tagString);
        model.addAttribute("categories", categories);
        model.addAttribute("subcategories", subcategories);

        return "base-layout";
    }

    @PostMapping("question/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, QuestionBindingModel questionBindingModel, RedirectAttributes redirectAttributes){
        if (!this.questionRepository.exists(id)){
            return "redirect:/";
        }

        List<String> errors = validateQuestionFields(questionBindingModel);
        if(!errors.isEmpty()){

            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:/question/edit/"+id;
        }

        Question question = this.questionRepository.findOne(id);

        if(!isUserAuthorOrAdmin(question)){
            return "redirect:/question/"+id;
        }

        Category category = this.categoryRepository.findOne(questionBindingModel.getCategoryId());
        Subcategory subcategory = this.subcategoryRepository.findOne(questionBindingModel.getSubcategoryId());

        List<Tag> tags = this.findTagsFromString(questionBindingModel.getTagString());

        question.setContent(questionBindingModel.getContent());
        question.setTitle(questionBindingModel.getTitle());
        question.setCategory(category);
        question.setSubcategory(subcategory);
        question.setTags(tags);

        this.questionRepository.saveAndFlush(question);

        return "redirect:/question/" + question.getId();
    }

    @GetMapping("/question/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(Model model, @PathVariable Integer id){
        if(!this.questionRepository.exists(id)){
            return "redirect:/";
        }

        Question question = this.questionRepository.findOne(id);

        if (!isUserAuthorOrAdmin(question)){
            return "redirect:/question/" + id;
        }

        model.addAttribute("question", question);
        model.addAttribute("view", "question/delete");

        return "base-layout";
    }

    @PostMapping("/question/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id){
        if(!this.questionRepository.exists(id)){
            return "redirect:/";
        }
        Question question = this.questionRepository.findOne(id);

        if (!isUserAuthorOrAdmin(question)){
            return "redirect:/question/" + id;
        }

        question.getTags().removeAll(question.getTags());

        this.questionRepository.delete(question);

        List<Tag> tags = this.tagRepository.findAll();
        for(Tag tag:tags){
            if(tag.getQuestions().isEmpty()){
                this.tagRepository.delete(tag);
            }
        }
        return "redirect:/";
    }


    private boolean isUserAuthorOrAdmin(Question question) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByUsername(user.getUsername());

        return userEntity.isAdmin() || userEntity.isAuthor(question);
    }

    private List<Tag> findTagsFromString(String tagString){

        if ("".equals(tagString)) return null;

        List<Tag> tags = new ArrayList<>();

        String[] tagNames = tagString.split("[,\\s]+");

        for (String tagName:tagNames) {
            Tag currentTag = this.tagRepository.findByName(tagName);

            if (currentTag==null){
                currentTag = new Tag(tagName);
                this.tagRepository.saveAndFlush(currentTag);
            }

            tags.add(currentTag);
        }
        return tags;
    }

    private List<String> validateQuestionFields(QuestionBindingModel bindingModel)
    {
        List<String> errors = new ArrayList<>();
        if(bindingModel.getTitle().equals("")){

            errors.add("Please enter a valid title!");
            if(bindingModel.getContent().equals("")){
                errors.add("Please enter a valid content!");
            }
            return errors;
        }

        if(bindingModel.getContent().equals("")){

           errors.add("Please enter a valid content!");
           return errors;
        }
        return errors;
    }
}
