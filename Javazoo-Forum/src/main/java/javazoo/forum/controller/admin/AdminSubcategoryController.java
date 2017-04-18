package javazoo.forum.controller.admin;

import javazoo.forum.bindingModel.CategoryOrderEditBindingModel;
import javazoo.forum.bindingModel.SubcategoryBindingModel;
import javazoo.forum.entity.Category;
import javazoo.forum.entity.Question;
import javazoo.forum.entity.Subcategory;
import javazoo.forum.repository.CategoryRepository;
import javazoo.forum.repository.QuestionRepository;
import javazoo.forum.repository.SubcategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/subcategories")
public class AdminSubcategoryController {

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping("/")
    public String list(Model model){

        List<Category> categories = this.categoryRepository.findAllByOrderByOrderNoAsc();
        List<Subcategory> subcategories = this.subcategoryRepository.findAllByOrderByOrderNoAsc();

        model.addAttribute("categories", categories);
        model.addAttribute("subcategories", subcategories);
        model.addAttribute("view", "admin/subcategory/list");
        return "base-layout";
    }

    @PostMapping("/")
    public String changeOrderSubcategories(CategoryOrderEditBindingModel categoryOrderEditBindingModel){

        int[] order = Arrays.stream(categoryOrderEditBindingModel.getList().split(",")).mapToInt(Integer::parseInt).toArray();
        for(int i = 1; i<=order.length;i++){
            Subcategory subcategory = this.subcategoryRepository.findOne(order[i-1]);
            subcategory.setOrderNo(i);
            this.subcategoryRepository.saveAndFlush(subcategory);
        }

        return "redirect:/admin/subcategories/";
    }

    @GetMapping("/create")
    public String create(Model model){

        List<Category> categories = this.categoryRepository.findAll();

        model.addAttribute("categories", categories);
        model.addAttribute("view", "admin/subcategory/create");

        return "base-layout";
    }

    @PostMapping("/create")
    public String createProcess(SubcategoryBindingModel subcategoryBindingModel){
        if(StringUtils.isEmpty(subcategoryBindingModel.getName())){
            return "redirect:/admin/subcategories/create";
        }


        Category category = this.categoryRepository.findOne(subcategoryBindingModel.getCategoryId());
        Subcategory subcategory = new Subcategory(subcategoryBindingModel.getName(),category);

        this.subcategoryRepository.saveAndFlush(subcategory);

        return "redirect:/admin/subcategories/";
    }

    @GetMapping("/edit/{id}")
    public String edit(Model model, @PathVariable Integer id){
        if(!this.subcategoryRepository.exists(id)){
            return "redirect:/admin/subcategories/";
        }

        Subcategory subcategory = this.subcategoryRepository.findOne(id);
        List<Category> categories = this.categoryRepository.findAll();

        model.addAttribute("categories", categories);
        model.addAttribute("subcategory", subcategory);
        model.addAttribute("view", "admin/subcategory/edit");

        return "base-layout";
    }

    @PostMapping("/edit/{id}")
    public String editProcess(@PathVariable Integer id, SubcategoryBindingModel subcategoryBindingModel){
        if(!this.subcategoryRepository.exists(id)){
            return "redirect:/admin/subcategories/";
        }

        Subcategory subcategory = this.subcategoryRepository.findOne(id);
        Category category = this.categoryRepository.findOne(subcategoryBindingModel.getCategoryId());
        subcategory.setName(subcategoryBindingModel.getName());
        subcategory.setCategory(category);

        this.subcategoryRepository.saveAndFlush(subcategory);

        return "redirect:/admin/subcategories/";
    }

    @GetMapping("/delete/{id}")
    public String delete(Model model, @PathVariable Integer id){
        if(!this.subcategoryRepository.exists(id)){
            return "redirect:/admin/subcategories/";
        }

        Subcategory subcategory = this.subcategoryRepository.findOne(id);

        model.addAttribute("subcategory", subcategory);
        model.addAttribute("category", subcategory.getCategory());
        model.addAttribute("view", "admin/subcategory/delete");

        return "base-layout";
    }

    @PostMapping("/delete/{id}")
    public String deleteProcess(@PathVariable Integer id){
        if(!this.subcategoryRepository.exists(id)){
            return "redirect:/admin/subcategories/";
        }

        Subcategory subcategory = this.subcategoryRepository.findOne(id);

        for(Question question:subcategory.getQuestions()){
            this.questionRepository.delete(question);
        }

        this.subcategoryRepository.delete(subcategory);

        return "redirect:/admin/subcategories/";
    }

}
