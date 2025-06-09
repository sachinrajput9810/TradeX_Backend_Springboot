package com.nt.controller;

import com.nt.domain.PaymentMethod;
import com.nt.model.PaymentOrder;
import com.nt.model.User;
import com.nt.response.PaymentResponse;
import com.nt.service.PaymentService;
import com.nt.service.UserService;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;
import okhttp3.Response;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {

    @Autowired
    private UserService userService ;

    @Autowired
    private PaymentService paymentService ;

    @PostMapping("/api/payment/{paymentMethod}/amount/{amount}")
    public ResponseEntity<PaymentResponse> paymentHandler(
                                                    @PathVariable PaymentMethod paymentMethod,
                                                    @PathVariable Long amount ,
                                                    @RequestHeader("Authorization") String jwt
                                                    ) throws Exception , RazorpayException , StripeException
    {
        User user = userService.findUserProfileByJwt(jwt) ;
        PaymentResponse paymentResponse ;

        PaymentOrder order = paymentService.createOrder(user, amount, paymentMethod) ;
        System.out.println("Order ==  "  + order);
        if(paymentMethod.equals(PaymentMethod.RAZORPAY)){
            paymentResponse = paymentService.createRazorpayPaymentLink(user, amount , order.getId()) ;
        }
        else {
            paymentResponse = paymentService.createStripePaymentLink(user, amount , order.getId()) ;
        }
        return new ResponseEntity<>(paymentResponse, HttpStatus.OK) ;
    }
}
