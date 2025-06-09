package com.nt.service;

import java.math.BigDecimal;

import com.nt.domain.PaymentMethod;
import com.nt.model.PaymentOrder;
import com.nt.model.User;
import com.nt.response.PaymentResponse;
import com.razorpay.RazorpayException;
import com.stripe.exception.StripeException;

public interface PaymentService {
    PaymentOrder createOrder(User user , Long amount, PaymentMethod paymentMethod) ;
    PaymentOrder getPaymentOrderById(Long orderId);
    Boolean proceedPaymentOrder(PaymentOrder paymentOrder , String paymentId) throws RazorpayException ;
    PaymentResponse createRazorpayPaymentLink(User user , Long amount , Long orderId) throws RazorpayException  ;
    PaymentResponse createStripePaymentLink(User user , Long amount , Long orderId) throws StripeException ;
}
