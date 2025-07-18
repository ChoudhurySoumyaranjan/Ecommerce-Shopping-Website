package com.shopify.main.utility;

import com.shopify.main.entity.OrderedProductDetails;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Component
public class CommonUtils {

    @Autowired
    private JavaMailSender javaMailSender;

    public boolean sendResetEmail(String recipientEmail, String url){
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom("choudhurysoumya87@gmail.com");
            simpleMailMessage.setTo(recipientEmail);
            simpleMailMessage.setSubject("Password Reset Link Shopify");

            String content = "Hello, You have requested to reset your password"
                    + "Click the link below to change your password:" + "<a href=\"" + url
                    + "\">Change my password</a>";

            simpleMailMessage.setText(content);
            javaMailSender.send(simpleMailMessage);
            return  true;
        }catch (Exception exception){
            exception.printStackTrace();
            return true;
        }
    }

    public static String generateResetToken(){
        return UUID.randomUUID().toString();
    }

    public static String generateUrl(HttpServletRequest request){

        String siteUrl=request.getRequestURL().toString(); //http://localhost:1010/passwordResetField

        //before Servlet Path= /passwordRestFiled

        String url =siteUrl.replace(request.getServletPath(),"");

        //after path = /

        //now http://localhost:1010

        return url;
    }

    public void sendOrderProductMail(OrderedProductDetails orderedProductDetails) {

        try {
            String emailBody = "Dear " + orderedProductDetails.getOrderAddress().getFirstName() + "  " + orderedProductDetails.getOrderAddress().getLastName() + "Thank You For Ordering " +
                    "Your Order Id is " + orderedProductDetails.getOrderId() +
                    "<br><b>Product Details :- </b><br>" +
                    "Name : " + orderedProductDetails.getProduct().getTitle() + "<br>" +
                    "Quantity : " + orderedProductDetails.getQuantity() + "<br>" +
                    "Price : " + orderedProductDetails.getPrice() + "<br>" +
                    "Payment Type : " + orderedProductDetails.getPaymentType() + "<br>" +
                    "Status : " +orderedProductDetails.getStatus()+"<br>"+
                    "<b>Delivery Address :- </b><br>" + orderedProductDetails.getOrderAddress().getAddress();

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("choudhurysoumya87@gmail.com", "Shopify.com");
            helper.setTo(orderedProductDetails.getOrderAddress().getEmail());
            helper.setSubject("Order Success Mail");
            helper.setText(emailBody, true); // `true` enables HTML

            javaMailSender.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void sendCancellationMail(OrderedProductDetails orderedProductDetails){
        String htmlBody="<b>Order ID <b>"+orderedProductDetails.getOrderId()+"<br>" +
                "<b>Product : </b>"+orderedProductDetails.getProduct().getTitle()+"<br>" +
                "<b>Quantity : </b>"+orderedProductDetails.getQuantity()+" " +
                "has been Cancelled Successfully";

        try {

            MimeMessage message=javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message,true,"UTF-8");

            mimeMessageHelper.setFrom("choudhurysoumya87@gmaail.com","Shopify.com");
            mimeMessageHelper.setTo(orderedProductDetails.getOrderAddress().getEmail());

            mimeMessageHelper.setSubject("Order Cancellation Mail");

            mimeMessageHelper.setText(htmlBody,true);

            javaMailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendOrderedProductStatusMail(OrderedProductDetails orderedProductDetails){

        String htmlBody="<b>Order Id : <b>"+orderedProductDetails.getOrderId()+"<br>" +
                "<b>Product : </b>"+orderedProductDetails.getProduct().getTitle()+"<br>" +
                "<b>Quantity : </b>"+orderedProductDetails.getQuantity()+"<br><hr>" +
                "<b>Current Order Status : </b>"+orderedProductDetails.getStatus();

        try {

            MimeMessage message=javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message,true,"UTF-8");

            mimeMessageHelper.setFrom("choudhurysoumya87@gmaail.com","Shopify.com");
            mimeMessageHelper.setTo(orderedProductDetails.getOrderAddress().getEmail());

            mimeMessageHelper.setSubject("Order Status Email, Shopify");

            mimeMessageHelper.setText(htmlBody,true);

            javaMailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
