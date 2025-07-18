package com.shopify.main.contollers;

import com.shopify.main.config.CustomUserDetails;
import com.shopify.main.entity.*;
import com.shopify.main.handler.OrderRequest;
import com.shopify.main.repository.CartRepository;
import com.shopify.main.repository.OrderedProductDetailsRepository;
import com.shopify.main.service.*;
import com.shopify.main.utility.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@SessionAttributes({"loggedInUser","cartItemCount"})
@RequestMapping("/user")
public class UserController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderedProductDetailsService orderedProductDetailsService;

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private OrderedProductDetailsRepository orderedProductDetailsRepository;

    @ModelAttribute
    public void getLoggedInUserDetails(@ModelAttribute("loggedInUser")CustomUserDetails customUserDetails,
                                       Authentication authentication,
//                                       @SessionAttribute("cartItemCount") int totalItemInCart,
                                       Model model){
        if (!ObjectUtils.isEmpty(customUserDetails)) {

            Integer totalCartItems=cartService.getUserTotalCartItems(customUserDetails.getId());

            if (totalCartItems!=null){
                model.addAttribute("cartItemCount",totalCartItems);
            }else {
                model.addAttribute("cartItemCount",0);
            }

            model.addAttribute("loggedInUser", customUserDetails);
        }else if (!ObjectUtils.isEmpty(authentication)){
            CustomUserDetails customUserDetails1=(CustomUserDetails) authentication.getPrincipal();

            model.addAttribute("loggedInUser",customUserDetails1);

        }

    }


    @GetMapping("/cart")
    public String openCartPage(@RequestParam(value = "uid",required = false)Long userId,
                               Model model){


        List<Cart> cartList =cartService.getAllCartProducts(userId);

        if (!ObjectUtils.isEmpty(cartList)){

            Double subTotalPrice=0.0;

            for (Cart c : cartList){

                int quantity=1;

                quantity=c.getQuantity();

                Double totalAmount= quantity * c.getProduct().getDiscountedPrice();
                subTotalPrice+=totalAmount;
                c.setTotalPrice(totalAmount);
            }

            model.addAttribute("cartList",cartList);
            model.addAttribute("subTotalPrice",subTotalPrice);
        }else {
            model.addAttribute("msg","No item Found in the Cart");
        }

        return "user/cart-page";
    }

    @GetMapping("/decreaseQuantity")
    public String handleDecreaseProductQuantityInCart(@RequestParam("uid")Long userId,
                                                      @RequestParam("pid")Long productId,
                                                      RedirectAttributes redirectAttributes){
        Optional<Cart> optionalCart=cartRepository.findByProductIdAndUserId(productId,userId);
        if (optionalCart.isPresent()){
            Cart oldCart=optionalCart.get();

            int quantity=oldCart.getQuantity();
            if (quantity<=1){
                cartService.deleteCartItem(oldCart);
            }else {
                cartService.decreaseCartItemQuantity(oldCart);
            }
        }
        //cartService

        return "redirect:/user/cart?uid="+userId;
    }

    @GetMapping("/increaseQuantity")
    public String handleIncreaseProductQuantityInCartRequest(@RequestParam("uid")Long userId,
                                                             @RequestParam("pid")Long productId,
                                                             RedirectAttributes redirectAttributes){

        Cart cart=cartRepository.findByProductIdAndUserId(productId,userId).get();

        cartService.increaseCartItemQuantity(cart);

        return "redirect:/user/cart?uid="+userId;
    }


    @GetMapping("/deleteCartItem")
    public String deleteCartItems(@RequestParam("uid")Long userId,
                                  @RequestParam("pid")Long productId,
                                  RedirectAttributes redirectAttributes){

        Cart cart=cartRepository.findByProductIdAndUserId(productId,userId).get();
        cartService.deleteCartItem(cart);

        return "redirect:/user/cart?uid="+userId;
    }


    @GetMapping("/checkout")
    public String openCheckoutPage(@RequestParam("userId")Long userId,
                                   @RequestParam("price")Double price,
                                   Model model){
        model.addAttribute("loggedInUserId",userId);
        model.addAttribute("subTotalPrice",price);
        model.addAttribute("orderRequest",new OrderRequest());
        return "user/checkout-order";
    }

    @PostMapping("/submitCheckOutOrderForm")
    public String handleCheckoutOrderForm(@ModelAttribute("orderRequest")OrderRequest orderRequest,
                                          @RequestParam("loggedInUserId")Long customerId,
                                          Model model){

        List<OrderedProductDetails> orderedProductDetails =orderedProductDetailsService.saveOrderedProduct(orderRequest,customerId);


        if(! ObjectUtils.isEmpty(orderedProductDetails)){

            String orderId =null;

            OrderAddress orderAddress=null;

            for (OrderedProductDetails productDetails : orderedProductDetails){
                orderId += productDetails.getOrderId();

                orderAddress=productDetails.getOrderAddress();

                commonUtils.sendOrderProductMail(productDetails);
            }

            model.addAttribute("orderId",orderId);
            model.addAttribute("orderAddress",orderAddress);
            return  "/user/success-page";
        }
        model.addAttribute("errorMsg","Unable to Place Order, Please Contact Customer Care if any Money Deducted !!");
        return "redirect:/user/checkout";
    }

    @GetMapping("/orders")
    public String openUserOrdersPage(Authentication authentication,
                                     Model model,
                                     @RequestParam(value = "pageNo",defaultValue = "1",required = false) int pageNo){

        CustomUserDetails customUserDetails=(CustomUserDetails) authentication.getPrincipal();

        if (!ObjectUtils.isEmpty(customUserDetails)){
            Long userId = customUserDetails.getId();

            int pageNum = pageNo-1;
            int pageSize=3;

            Pageable pageable = PageRequest.of(pageNum,pageSize);

            Page<OrderedProductDetails> orderedProductList =orderedProductDetailsService.getOrderedProductsOfUser(userId,pageable);
            if (orderedProductList!=null){
                model.addAttribute("orders",orderedProductList.getContent());
                 model.addAttribute("currentPage",pageNo);
                 model.addAttribute("totalPages",orderedProductList.getTotalPages());
            }else {
                model.addAttribute("errorMsg","None of the Product Ordered Yet");
            }
        }

        return "user/user-orders";
    }

    @GetMapping("/cancel")
    public String handleCancelOrderRequest(@RequestParam("orderId")String orderId,
                                           RedirectAttributes redirectAttributes){

        OrderedProductDetails updatedOrderedDetails=orderedProductDetailsService.cancelOrder(orderId);

        if (updatedOrderedDetails!=null){
            commonUtils.sendCancellationMail(updatedOrderedDetails);
            redirectAttributes.addFlashAttribute("successMsg","Order Cancelled");
        }else {
            redirectAttributes.addFlashAttribute("errorMsg","Unable to Cancel Order");
        }

        return "redirect:/user/orders";
    }


    @GetMapping("/userprofile")
    public String openUserProfilePage(@ModelAttribute("loggedInUser")CustomUserDetails loggedInUser,
                                      Model model,
                                      @RequestParam(value = "pageNum",
                                              defaultValue = "1",
                                              required = false)
                                          int pageNum){

        if (loggedInUser!=null){
            Optional<User> optionalUser=userService.getUserInfo(loggedInUser.getId());

            int pageNo = pageNum-1;
            int pageSize = 5;
            Pageable pageable = PageRequest.of(pageNo,pageSize);

            Page<OrderedProductDetails> orderedPage =orderedProductDetailsRepository.findByUserId(loggedInUser.getId(),pageable);

            if (optionalUser.isPresent()){
                User user=optionalUser.get();

                model.addAttribute("user",user);
                model.addAttribute("orders",orderedPage.getContent());
                model.addAttribute("totalPage",orderedPage.getTotalPages());
                model.addAttribute("currentPage",pageNum);
            }
        }

        return "user/user-profile";
    }

    @PostMapping("/update/info")
    public String handleUpdatePersonalInfoRequest(@ModelAttribute User user,
                                                  @RequestParam("img")MultipartFile file,
                                                  RedirectAttributes redirectAttributes) throws Exception{

        User updatedUser=userService.updateUserPersonalInfo(user,file);
        if (updatedUser!=null){
            redirectAttributes.addFlashAttribute("successMsg","Information Updated Successfully");
        }else {
            redirectAttributes.addFlashAttribute("errorMsg","Failed to Update Information");
        }
        return "redirect:/user/userprofile";
    }

    @GetMapping("/order/details")
    public String getProductInfo(@RequestParam String id,Model model){
        OrderedProductDetails orderedProductDetails=orderedProductDetailsService.getProductInformationByOrderId(id);
        if (orderedProductDetails!=null){
            model.addAttribute("order",orderedProductDetails);
        }else {
            model.addAttribute("errorMsg","No Product Details Found, Pls Contact Customer Care");
        }
        return "user/product-details";
    }

    @PostMapping("/profile/password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword")String newPassword,
                                 @RequestParam("confirmNewPassword")String confirmPassword,
                                 @RequestParam("uid")Long userId,
                                 RedirectAttributes redirectAttributes){

        boolean isUpdated=userService.changeAccountPassword(currentPassword,confirmPassword,userId);

        if (isUpdated){
            redirectAttributes.addFlashAttribute("successPass","Password Updated Successfully");
        }

        redirectAttributes.addFlashAttribute("failPass","Failed to Update Password");

        return "redirect:/user/userprofile";
    }
}
