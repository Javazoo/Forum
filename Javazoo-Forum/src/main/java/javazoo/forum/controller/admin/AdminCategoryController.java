package javazoo.forum.controller.admin;

import javazoo.forum.bindingModel.CategoryBindingModel;
import javazoo.forum.entity.Category;
import javazoo.forum.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/")
    public String list(Model model){
        List<Category> categories = this.categoryRepository.findAllByOrderByOrderNoAsc();

        model.addAttribute("categories", categories);
        model.addAttribute("view", "admin/category/list");

        return "base-layout";
    }

    @GetMapping("/create")
    public String create(Model model){

        model.addAttribute("view", "admin/category/create");

        return "base-layout";
    }
    @PostMapping("/create")
    public String createProcess(CategoryBindingModel categoryBindingModel){
        if(StringUtils.isEmpty(categoryBindingModel.getName())){
            return "redirect:/admin/categories/create";
        }
        if(categoryBindingModel.getOrderNo()== null){
            return "redirect:/admin/categories/create";
        }

        Category category = new Category(categoryBindingModel.getName(),
                categoryBindingModel.getOrderNo());

        this.categoryRepository.saveAndFlush(category);

        return "redirect:/admin/categories/";


    }
}
