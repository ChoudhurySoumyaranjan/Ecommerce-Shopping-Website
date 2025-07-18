package com.shopify.main.contollers;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;

@ControllerAdvice
public class ExceptionController {

//    @ExceptionHandler(Throwable.class)
//    public String handleNullPointerException(Throwable exception,
//                                             Model model){
//        model.addAttribute("error","INTERNAL SERVER ERROR");
//        model.addAttribute("errorDetails",exception.getMessage());
//        return "exception-page";
//    }

//    @GetMapping("/error")
//    public String handleNullPointerException(Throwable exception,
//                                             Model model){
//        model.addAttribute("error","INTERNAL SERVER ERROR");
//        model.addAttribute("errorDetails",exception.getMessage());
//        return "exception-page";
//    }

}
