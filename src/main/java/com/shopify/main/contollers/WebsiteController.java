package com.shopify.main.contollers;

import com.shopify.main.entity.Cart;
import com.shopify.main.entity.Category;
import com.shopify.main.entity.Product;
import com.shopify.main.config.CustomUserDetails;
import com.shopify.main.entity.User;
import com.shopify.main.service.CartService;
import com.shopify.main.service.CategoryService;
import com.shopify.main.service.ProductService;
import com.shopify.main.service.UserService;
import com.shopify.main.utility.CommonUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@SessionAttributes({"loggedInUser","cartItemCount"})
public class WebsiteController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;


    @Autowired
    private CartService cartService;

    @Autowired
    private CommonUtils commonUtils;

    @GetMapping({"/","/index"})
    public String openIndexPage(Authentication authentication,Model model){
        List<Category> categoryList=categoryService.getAllCategories();
        model.addAttribute("categories",categoryList);
        return "index";
    }

    @ModelAttribute  //this method will be called when any request hit to end point of this application controller
    public void loggedInUserDetails(Authentication authentication,Model model){

        if(authentication!=null) {

            CustomUserDetails loggedInUser = (CustomUserDetails) authentication.getPrincipal();
            model.addAttribute("loggedInUser",loggedInUser);  //after user successfully logged in through spring security details stored in Spring Security Context

            Integer totalCartItems=cartService.getUserTotalCartItems(loggedInUser.getId());
            System.out.println(totalCartItems);
            if (totalCartItems!=null){
                model.addAttribute("cartItemCount",totalCartItems);
            }else {
                model.addAttribute("cartItemCount",0);
            }
        }

    }

    @GetMapping("/register")
    public String openSignUpPage(Model model) {
        model.addAttribute("userObj", new User());
        return "register-page";
    }

    @PostMapping("/handleRegistrationForm")
    public String handleRegistrationForm(
            @Valid @ModelAttribute("userObj") User user,
            BindingResult bindingResult,
            @RequestParam("img") MultipartFile multipartFile,
            Model model,
            RedirectAttributes redirectAttributes
    ) throws Exception {
        if (bindingResult.hasErrors()) {
            return "register-page";
        } else {
            User savedUser = userService.saveUserDetails(user, multipartFile);
            if (savedUser != null) {
                redirectAttributes.addFlashAttribute("successMsg", "You are Registered Successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Failed to Register User");
            }
        }

        return "redirect:/register";
    }

    @GetMapping("/signIn")
    public String openLoginPage() {
        return "login-page";

    }

    @GetMapping("/resetPassword")
    public  String openRestPasswordEmailPage(){
        return "reset-page-email";
    }

    @PostMapping("/passwordResetField")
    public String handlePasswordRequestEmail(@RequestParam("email")String email,
                                             RedirectAttributes redirectAttributes,
                                             Model model,
                                             HttpServletRequest request){

        Optional<User> optionalUser=userService.findUserByEmail(email);
        if (optionalUser.isEmpty()){
            redirectAttributes.addFlashAttribute("errorMsg","Please Enter Valid Email");
            return "redirect:/resetPassword";
        }else{
            String resetToken=CommonUtils.generateResetToken();
            User user=userService.setRestTokenForForgottenPassword(email,resetToken);
            if (user!= null) {
                String url = CommonUtils.generateUrl(request) + "/reset-password?token=" + resetToken;
                //generatedToken = http://localhost:1010/reset-password?token=generatedToken

                boolean success = commonUtils.sendResetEmail(email, url);

                if (success) {
                    redirectAttributes.addFlashAttribute("successMsg", "Reset Link sent Successfully");
                }else {
                    redirectAttributes.addFlashAttribute("errorMsg","Internal Server Error Occured During Email Sending");
                }
            }
        }
        return "redirect:/resetPassword";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(value = "token",required = false)String token,
                                Model model){
        model.addAttribute("token",token);
        return "reset-password-page";
    }

    @PostMapping("/resetPasswordForm")
    public String attemptResetPasswordUsingToken(@RequestParam("password")String password,
                                                 @RequestParam(value = "token",required = false)String token,
                                                 RedirectAttributes redirectAttributes){
        if (token!=null) {
            User user = userService.resetPasswordThroughToken(token, password);

            if (user!=null) {
                redirectAttributes.addFlashAttribute("successMsg", "Password Reset Successfully");
            }

        }else {
            redirectAttributes.addFlashAttribute("errorMsg","Failed to Reset Password Token Missing or invalid");
        }
        return "redirect:/reset-password";
    }

    @PostMapping("/logout")
    public String openLogoutPage(SessionStatus sessionStatus){
        sessionStatus.setComplete();
        return "redirect:/";
    }


    @GetMapping("/products")
    public String openProductPage(@RequestParam(value = "category", required = false) String category,
                                  Model model,
                                  RedirectAttributes redirectAttributes,
                                  Authentication authentication){

        List<Category> categoryList =categoryService.getAllCategories();
        model.addAttribute("categories",categoryList);

        if (category==null || category.isEmpty()){
            List<Product> productList=productService.getAllProducts();
            model.addAttribute("products",productList);
        }

        else {
            List<Product> productList=productService.getProductsByCategoryName(category);
            if (!productList.isEmpty()) {
                model.addAttribute("products", productService.getProductsByCategoryName(category));
            }else {
                model.addAttribute("errorMsg","No Product Found");
            }
        }

        return "products-page";
    }

    @GetMapping("/productSearch")
    public String handleProductSearch(@RequestParam("search")String keyword,
                                      Model model){

        List<Category> categoryList =categoryService.getAllCategories();
        model.addAttribute("categories",categoryList);

        List<Product> productList=productService.getProductsBySearch(keyword);
        if (!ObjectUtils.isEmpty(productList)){
            model.addAttribute("products",productList);
        }else {
            model.addAttribute("errorMsg","No Products Found");
        }
        return "products-page";
    }


    @GetMapping("/viewProduct")
    public String openViewProductPage(@RequestParam("id") Long id,
                                      Model model){
        Optional<Product> optionalProduct =productService.findProductById(id);

        if (optionalProduct.isPresent()){
            model.addAttribute("product",optionalProduct.get());
        }else {
            model.addAttribute("errorMsg","No Details Found");
        }

        return "view-product";
    }

    @GetMapping("/addToCart")
    public String addToCartProduct(@RequestParam("pid")Long pid,
                                   @RequestParam("uid")Long uid,
                                   RedirectAttributes redirectAttributes){

        Cart cart =cartService.saveCart(pid,uid);
        if (!ObjectUtils.isEmpty(cart)){
            redirectAttributes.addFlashAttribute("successMsg","Item Added to the Cart Successfully");
        }else {
            redirectAttributes.addFlashAttribute("errorMsg","Failed to Add Item in the Cart");
        }

        return "redirect:/viewProduct?id="+pid;
    }

    @GetMapping("/error")
    public String handleNullPointerException(Throwable exception,
                                             Model model){
        model.addAttribute("error","INTERNAL SERVER ERROR");
        model.addAttribute("errorDetails",exception.getMessage());
        return "exception-page";
    }

}
