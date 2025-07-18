package com.shopify.main.contollers;

import com.shopify.main.config.CustomUserDetails;
import com.shopify.main.entity.Category;
import com.shopify.main.entity.OrderedProductDetails;
import com.shopify.main.entity.Product;
import com.shopify.main.entity.User;
import com.shopify.main.repository.CategoryRepository;
import com.shopify.main.repository.ProductRepository;
import com.shopify.main.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderedProductDetailsService orderedProductDetailsService;

    @Autowired
    private FileService fileService;

    @Value("${aws.s3.bucket.category}")
    private String categoryBucket;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.s3.bucket.product}")
    private String productBucket;


    @ModelAttribute
    public void authenticateAdmin(Authentication authentication,
                                  Model model){

    }

//    @GetMapping("/adminLogin")
//    public String openAdminLoginPage(){
//        return "admin-login";
//    }

    @GetMapping("/adminDashboard")
    public String openAdminDashBoard(){
        return "admin/admin-dashboard";
    }

    @GetMapping("/addProduct")
    public String openAddProductPage(Model model){
        List<Category> listCategory=categoryService.getAllCategories();
        model.addAttribute("categoryList",listCategory);
        model.addAttribute("productObj",new Product());
        return "admin/add-product";
    }

    @PostMapping("/handleAddProductForm")
    public String handleAddProductForm(@Valid @ModelAttribute("productObj") Product product,
                                       BindingResult bindingResult,
                                       Model model,
                                       @RequestParam("img")MultipartFile multipartFile,
                                       RedirectAttributes redirectAttributes){

        if (bindingResult.hasErrors()){
            List<Category> listCategory=categoryService.getAllCategories();
            model.addAttribute("categoryList",listCategory);
            return "admin/add-product";
        }
        boolean success=productService.addProduct(product,multipartFile);
        if (success){
            redirectAttributes.addFlashAttribute("successMsg","Product Added Successfully");
        }else{
            redirectAttributes.addFlashAttribute("errorMsg","failed to Add Product due to Internal Server Error");
        }

        return "redirect:/admin/addProduct";
    }

    @GetMapping("/openCategory")
    public String openAddCategoryPage(Model model,
                                      @RequestParam(value = "pageNo",
                                              defaultValue = "1",
                                              required = false)
                                      int pageNo){

        int pageNum = pageNo-1;
        int pageSize=4;

        Pageable pageable = PageRequest.of(pageNum,pageSize);
        model.addAttribute("categoryObj",new Category());

        Page<Category> categoryList =categoryService.getAllCategoriesByPageable(pageable);
        if (categoryList!=null){
            model.addAttribute("categoryList",categoryList.getContent());
            model.addAttribute("totalPages",categoryList.getTotalPages());
            model.addAttribute("currentPage",pageNo);
        }
        return "admin/category-page";
    }

    @PostMapping("/handleAddCategory")
    public String handleAddCategoryRequest(@ModelAttribute("categoryObj")Category category,
                                           @RequestParam("img")MultipartFile multipartFile,
                                           RedirectAttributes redirectAttributes){

        if (! categoryService.checkDataExistOrNotByName(category)){
            categoryService.addNewCategory(category,multipartFile);
            redirectAttributes.addFlashAttribute("successMsg","Category Added Successfully");
        }else {
            redirectAttributes.addFlashAttribute("errorMsg","This Category All Ready Exist");
        }
        return "redirect:/admin/openCategory";
    }

    @GetMapping("/editCategory")
    public String openEditCategoryPage(@RequestParam("id") Long id,RedirectAttributes redirectAttributes){
        Optional<Category> optionalCategory =categoryRepository.findById(id);
        if (!optionalCategory.isEmpty()){
            redirectAttributes.addFlashAttribute("category",optionalCategory.get());
            redirectAttributes.addFlashAttribute("available","Editable Form Field"); //small logic
        }
        return "redirect:/admin/openCategory";
    }


    @PostMapping("/handleEditCategory")
    public String handleEditCategoryForm(@ModelAttribute("category")Category newCategory,
                                         @RequestParam("img") MultipartFile multipartFile,
                                         RedirectAttributes redirectAttributes) throws Exception{
//
//            String UPLOAD_DIR = "src/main/resources/static/courseCategoryImages/";
//            String url = "http://localhost:1010/courseCategoryImages/";


            Optional<Category> optionalOldCategory = categoryRepository.findCategoryById(newCategory.getId());

            if (optionalOldCategory.isPresent()) {

                Category oldCategory = optionalOldCategory.get();

                if (! multipartFile.isEmpty()) {

                    //updating category image on project directory
//                    String mediaFileName = multipartFile.getOriginalFilename();
//                    Path filePath = Paths.get(UPLOAD_DIR + mediaFileName);
//                    Files.write(filePath, multipartFile.getBytes());

//                    newCategory.setImage(url + mediaFileName);

                    //code for uploading file on s3 bucket

                    Boolean isSuccess=fileService.uploadFileToAwsS3(multipartFile,1);  //bucketType 1 is Category Bucket

                    if (isSuccess){

                        //https://course-category-images.s3.eu-north-1.amazonaws.com/icecream.avif  , example image url saved on s3 bucket

                        String url="https://"+categoryBucket+".s3."+region+".amazonaws.com/"+multipartFile.getOriginalFilename();

                        newCategory.setImage(url);
                    }



                }else {
                    newCategory.setImage(oldCategory.getImage());
                }
                    boolean success = categoryService.updateCategory(newCategory);
                    if (success) {
                        redirectAttributes.addFlashAttribute("successMsg", "Category Edited Successfully");
                    }

            } else {
                redirectAttributes.addFlashAttribute("errorMsg", "Unable to Edit Category");
            }
        return "redirect:/admin/openCategory";
    }

    @GetMapping("/deleteCategory")
    public String deleteCategoryRequestHandle(@RequestParam("id")Long id,
                                              RedirectAttributes redirectAttributes){
        boolean success=categoryService.deleteCategoryById(id);
        if (success){
            redirectAttributes.addFlashAttribute("successMsg","Category Deleted Successfully");
        }else {
            redirectAttributes.addFlashAttribute("errorMsg","Failed to Delete category");
        }
        return "redirect:/admin/openCategory";
    }

    @GetMapping("/viewAllProducts")
    public String openViewAllProductsPage(Model model,
                                          @RequestParam(value = "pageNo",
                                                        defaultValue = "1",
                                                        required = false)
                                          int pageNo){
        int pageNum = pageNo-1;
        int pageSize = 4;
        Pageable pageable = PageRequest.of(pageNum,pageSize);

        Page<Product> productPage=productService.getAllProductsByPageable(pageable);

        if (productPage!=null){

            model.addAttribute("products",productPage.getContent());
            model.addAttribute("currentPage",pageNo);
            model.addAttribute("totalPages",productPage.getTotalPages());

        }

        return "admin/view-all-products";
    }

    @GetMapping("/searchProd")
    public String getSearchedProduct(@RequestParam("search")String keyword,
                                     Model model){
        List<Product> productList=productService.getProductsBySearch(keyword);
        if (productList!=null){
            model.addAttribute("products",productList);
        }else {
            model.addAttribute("errorMsg","Product Not Found !!!!!!");
        }
        return "admin/view-all-products";
    }

    @GetMapping("/editProduct")
    public String editProduct(Model model,
                              @RequestParam Long id){
        Optional<Product> optionalProduct=productService.findProductById(id);

        if (optionalProduct.isPresent()){
            model.addAttribute("product",optionalProduct.get());
            model.addAttribute("categoryList",categoryService.getAllCategories());
        }else {
            model.addAttribute("errorMsg","No Product found");
        }
        return "edit-product";
    }

    @PostMapping("/editProductForm")
    public  String handleEditProductForm(@ModelAttribute("product")Product newProduct,
                                         @RequestParam("img")MultipartFile multipartFile,
                                         RedirectAttributes redirectAttributes) throws Exception{


        if (newProduct.getDiscount()<0 || newProduct.getDiscount()>100){
            redirectAttributes.addFlashAttribute("errorMsg","Can't Apply the Discount");
        }
        else
        {

            Product oldProduct =productService.getProductByTitle(newProduct.getTitle()).get();

            newProduct.setId(oldProduct.getId());

            if (!multipartFile.isEmpty()){

                //code for storing file on system

//                String UPLOAD_DIR = "src/main/resources/static/productImages/";
//                String url = "http://localhost:1010/productImages/";
//                Path path =Paths.get(UPLOAD_DIR+multipartFile.getOriginalFilename());
//                Files.write(path,multipartFile.getBytes());
//                newProduct.setImage(url+multipartFile.getOriginalFilename());


                //code for uploading file on s3 bucket

                Boolean isSuccess=fileService.uploadFileToAwsS3(multipartFile,2);  //bucketType 2 is Product Bucket

                if (isSuccess){

                    //https://shopifyy-product-image.s3.eu-north-1.amazonaws.com/motherdairy.webp  , example image url saved on s3 bucket

                    String url="https://"+productBucket+".s3."+region+".amazonaws.com/"+multipartFile.getOriginalFilename();

                    newProduct.setImage(url);
                }

            }else {
                newProduct.setImage(oldProduct.getImage());
            }


            //logic For Discounted Price
            Double discountedAmount = newProduct.getPrice()*(newProduct.getDiscount()/100.00);
            newProduct.setDiscountedPrice(newProduct.getPrice()-discountedAmount);


            newProduct.setCategory(oldProduct.getCategory());
            Product p =productRepository.save(newProduct);
            if (p!=null){
                redirectAttributes.addFlashAttribute("successMsg","Product Edited Successfully");
            }else {
                redirectAttributes.addFlashAttribute("errorMsg","Failed to Edit Product");
            }
        }
        return "redirect:/admin/viewAllProducts";
    }

    @GetMapping("/deleteProduct")
    public String deleteProduct(@RequestParam("id")Long id,
                                RedirectAttributes redirectAttributes){
        boolean success=productService.deleteProduct(id);
        if (success){
            redirectAttributes.addFlashAttribute("successMsg","Product Deleted Successfully");
        }else {
            redirectAttributes.addFlashAttribute("errorMsg","Deletion Failed");
        }
        return "redirect:/admin/viewAllProducts";
    }

    @GetMapping("/users")
    public String allUsers(@RequestParam(value = "pageNo",
                                       defaultValue = "1",
                                       required = false)
                               int pageNo,
                           Model model){

        int pageNumber = pageNo-1;
        int pageSize = 5;

        Pageable pageable =PageRequest.of(pageNumber,pageSize);

        Page<User> pageList=userService.getAllUserInfo(pageable);

        if (!pageList.isEmpty()){

            model.addAttribute("userList",pageList.getContent());
            model.addAttribute("totalPages",pageList.getTotalPages());
            model.addAttribute("currentPage",pageNo);
        }else{
            model.addAttribute("noUserFound","No User Found");
        }

        return "admin/user-management";
    }

    @PostMapping("/user/search/byName")
    public String getUserDetailsByName(@RequestParam("search") String name,
                                        Model model){
        Pageable pageable = PageRequest.of(0,5);
        Page<User> pageList=userService.getCustomersBySearching(name,pageable);

        if (!pageList.isEmpty()){

            model.addAttribute("userList",pageList.getContent());
            model.addAttribute("totalPages",pageList.getTotalPages());
            model.addAttribute("currentPage",1);
        }else{
            model.addAttribute("noUserFound","No new Admins Found");
        }

        return "admin/user-management";
    }

    @GetMapping("/user/block")
    public String blockUser(@RequestParam("id")Long id,
                            RedirectAttributes redirectAttributes){

        boolean success=userService.blockUserFromLogin(id);
        if (success){
            redirectAttributes.addFlashAttribute("message","User Blocked");
        }else {
            redirectAttributes.addFlashAttribute("message","Failed to Block User, User Not Available");
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/user/unblock")
    public String unBlockUser(@RequestParam("id")Long id,
                              RedirectAttributes redirectAttributes){
        boolean success=userService.unBlockUser(id);
        if (success){
            redirectAttributes.addFlashAttribute("message","User Unblocked");
        }else {
            redirectAttributes.addFlashAttribute("message","Failed to Unblock User");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/allOrders")
    public String openOrderPage(@RequestParam(value = "pageNumber",
                                            defaultValue = "1",
                                            required = false)int pageNumber,
                                Model model){

        int pageNo = pageNumber-1;

        int pageSize=3;

        Page<OrderedProductDetails> pageList=orderedProductDetailsService.getAllOrders(PageRequest.of(pageNo,pageSize));
        if (pageList!=null){
            model.addAttribute("totalPages",pageList.getTotalPages());

            model.addAttribute("orders",pageList.getContent());

            model.addAttribute("currentPage",pageNumber);
        }else {
            model.addAttribute("errorMsg","No Order Available");
        }

        return "admin/all-orders";
    }

    @PostMapping("/updateStatus")
    public String updateOrderStatus(@RequestParam("orderId") String orderId,
                                    @RequestParam("status") String status,
                                    RedirectAttributes redirectAttributes){

        System.out.println(orderId + status);

        boolean isUpdated=orderedProductDetailsService.updateOrderStatus(orderId,status);

        if (isUpdated){
            redirectAttributes.addFlashAttribute("successMsg",orderId+" Status Updated");
        }else {
            redirectAttributes.addFlashAttribute("errorMsg","Failed to Update Status of "+orderId);
        }

        return "redirect:/admin/allOrders";
    }

    @PostMapping("/search")
    public String searchOrdersById(@RequestParam("orderId") String orderId,
                                   Model model){

        OrderedProductDetails productDetails=orderedProductDetailsService.getOrderedProductByOrderId(orderId);

        if (productDetails!=null){
            List<OrderedProductDetails> productDetailsList= new ArrayList<>();
            productDetailsList.add(productDetails);
            model.addAttribute("orders",productDetailsList);
        }else {
            model.addAttribute("errorMsg","No Results Found");
        }

        return "admin/all-orders";
    }

    @PostMapping("/filter")
    public String filterOrdersByStatus(@RequestParam("status") String orderStatus,
                                       Model model){

        List<OrderedProductDetails> orderedProductDetailsList=orderedProductDetailsService.getOrderedProductsByStatus(orderStatus);
        if (orderedProductDetailsList!=null){
            model.addAttribute("orders",orderedProductDetailsList);
        }else {
            model.addAttribute("errorMsg","No Results Found");
        }
        return "admin/all-orders";
    }

    @GetMapping("/register")
    public String openAdminRegistrationPage(Model model){
        model.addAttribute("admin",new User());
        return "admin/admin-signup";
    }

    @PostMapping("/registerAdmin")
    public String handleAddAdminRequest(@Valid @ModelAttribute("admin")User user,
                                        BindingResult bindingResult,
                                        Model model,
                                        RedirectAttributes redirectAttributes,
                                        @RequestParam("img") MultipartFile multipartFile
                                        ){

        if (bindingResult.hasErrors()){
            return "admin/admin-signup";
        }else {
            User savedAdmin=userService.addNewAdmin(user,multipartFile);
            if (! ObjectUtils.isEmpty(savedAdmin)){
                redirectAttributes.addFlashAttribute("successMsg","New Admin Added Successfully");
            }else {
                redirectAttributes.addFlashAttribute("errorMsg","Failed to Add New Admin");
            }
        }
        return "redirect:/admin/register";
    }

    @GetMapping("/editAdmin")
    public String openEditAdminPage(@RequestParam(value = "pageNo",
                                   defaultValue = "1",
                                   required = false)
                           int pageNo,
                           Model model){

        int pageNumber = pageNo-1;
        int pageSize = 5;

        Pageable pageable =PageRequest.of(pageNumber,pageSize);

        Page<User> pageList=userService.getAllAdmin(pageable);

        if (!pageList.isEmpty()){

            model.addAttribute("adminList",pageList.getContent());
            model.addAttribute("totalPages",pageList.getTotalPages());
            model.addAttribute("currentPage",pageNo);
        }else{
            model.addAttribute("noUserFound","No new Admins Found");
        }

        return "admin/edit-admin";
    }

    @GetMapping("/admin/block")
    public String blockAdmin(@RequestParam("id")Long id,
                            RedirectAttributes redirectAttributes){

        boolean success=userService.blockUserFromLogin(id);
        if (success){
            redirectAttributes.addFlashAttribute("message","Admin Blocked");
        }else {
            redirectAttributes.addFlashAttribute("message","Failed to Block Admin, User Not Available");
        }

        return "redirect:/admin/editAdmin";
    }

    @GetMapping("/admin/unblock")
    public String unBlockAdmin(@RequestParam("id")Long id,
                              RedirectAttributes redirectAttributes){
        boolean success=userService.unBlockUser(id);
        if (success){
            redirectAttributes.addFlashAttribute("message","Admin Unblocked");
        }else {
            redirectAttributes.addFlashAttribute("message","Failed to Unblock Admin");
        }
        return "redirect:/admin/editAdmin";
    }

    @PostMapping("/search/byName")
    public String getAdminDetailsByName(@RequestParam("search") String name,
                                        Model model){
        Pageable pageable = PageRequest.of(0,5);
        Page<User> pageList=userService.getAdminBySearchedName(name,pageable);

        if (!pageList.isEmpty()){

            model.addAttribute("adminList",pageList.getContent());
            model.addAttribute("totalPages",pageList.getTotalPages());
            model.addAttribute("currentPage",1);
        }else{
            model.addAttribute("noUserFound","No new Admins Found");
        }

        return "admin/edit-admin";
    }

    @GetMapping("/admin/profile")
    public String  openAdminProfile(Authentication authentication,
                                    Model model){
        if (authentication!=null){
            CustomUserDetails customUserDetails=(CustomUserDetails) authentication.getPrincipal();
            Optional<User> optionalUser=userService.getUserInfo(customUserDetails.getId());
            if (!ObjectUtils.isEmpty(optionalUser)){
                model.addAttribute("admin",optionalUser.get());
            }
        }
        return "admin/admin-profile";
    }

}
