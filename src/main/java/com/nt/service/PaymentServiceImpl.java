package com.nt.service;

import com.nt.domain.PaymentMethod;
import com.nt.domain.PaymentOrderStatus;
import com.nt.model.PaymentOrder;
import com.nt.model.User;
import com.nt.repository.PaymentOrderRepository;
import com.nt.response.PaymentResponse;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.math.BigDecimal;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    private PaymentOrderRepository paymentOrderRepository ;

    @Value("${stripe.api.key}")
    private String stripSecretKey ;

    @Value("${razorpay.api.key}")
    private String razorPaySecretKey ;

    @Value("${razorpay.api.secret}")
    private String apiSecretkey ;

    @Override
    public PaymentOrder createOrder(User user, Long amount, PaymentMethod paymentMethod) {
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setUser(user);
        BigDecimal bigDecimalAmount = BigDecimal.valueOf(amount);
        paymentOrder.setAmount(bigDecimalAmount);
        paymentOrder.setPaymentMethod(paymentMethod);
        paymentOrder.setStatus(PaymentOrderStatus.PENDING);
        return paymentOrderRepository.save(paymentOrder);
    }

    @Override
    public PaymentOrder getPaymentOrderById(Long id) {
        return paymentOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment Order Not Found"));
    }

    @Override
    public Boolean proceedPaymentOrder(PaymentOrder paymentOrder, String paymentId) throws RazorpayException {
    	if(paymentOrder.getStatus() == null) {
    		paymentOrder.setStatus(PaymentOrderStatus.PENDING);
    	}
        if(paymentOrder.getStatus().equals(PaymentOrderStatus.PENDING)){
            if(paymentOrder.getPaymentMethod().equals(PaymentMethod.RAZORPAY)){
                RazorpayClient razorpay = new RazorpayClient(razorPaySecretKey,apiSecretkey) ;
                Payment payment = razorpay.payments.fetch(paymentId);

                Integer amount = payment.get("amount") ;
                String status = payment.get("status") ;

                if(status.equals("captured")){
                    paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
                    return true ;
                }
                paymentOrder.setStatus(PaymentOrderStatus.FAILED);
                paymentOrderRepository.save(paymentOrder);
                return false ;
            }
            paymentOrder.setStatus(PaymentOrderStatus.SUCCESS);
            paymentOrderRepository.save(paymentOrder);
            return true ;
        }
        return false ;
    }

    @Override
    public PaymentResponse createRazorpayPaymentLink(User user, Long amount , Long orderId) throws RazorpayException {
        Long Amount = amount * 100 ;
        try{
            RazorpayClient razorpay = new RazorpayClient(razorPaySecretKey,apiSecretkey) ;
            JSONObject paymentLinkRequest = new JSONObject() ;
            paymentLinkRequest.put("amount",Amount);
            paymentLinkRequest.put("currency","INR") ;

            JSONObject customer = new JSONObject() ;
            customer.put("name",user.getFullName()) ;
            customer.put("email",user.getEmail()) ;
            paymentLinkRequest.put("customer",customer) ;

            JSONObject notify = new JSONObject() ;
            notify.put("email" , true) ;
            paymentLinkRequest.put("notify",notify) ;

            paymentLinkRequest.put("reminder_enable", true); // ✅ correct


            paymentLinkRequest.put("callback_url" ,"http://localhost:5173/wallet?order_id="+orderId) ;
            paymentLinkRequest.put("callback_method","get") ;

            PaymentLink payment = razorpay.paymentLink.create(paymentLinkRequest) ;
            String paymentLinkId = payment.get("id") ;
            String paymentLinkUrl = payment.get("short_url") ;
            PaymentResponse response = new PaymentResponse() ;
            response.setPaymentUrl(paymentLinkUrl); ;

            return response ;
        }
        catch(RazorpayException e) {
            System.out.println("Error creating payment Link :: " + e.getMessage());
            throw new RazorpayException(e.getMessage());
        }
    }
    @Override
    public PaymentResponse createStripePaymentLink(User user, Long amount, Long orderId) throws StripeException {
        Stripe.apiKey = stripSecretKey ;
        SessionCreateParams params = SessionCreateParams.builder()
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/wallet?order_id="+orderId)
                .setCancelUrl("http://localhost:8080/payment/cancel")
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("usd")
                                .setUnitAmount(amount*100)
                                .setProductData(SessionCreateParams
                                        .LineItem
                                        .PriceData
                                        .ProductData
                                        .builder()
                                        .setName("Top up Wallet")
                                        .build()
                                ).build()
                        ).build()
                ).build();
        Session session = Session.create(params) ;

        System.out.println("session _____ " + session);

        PaymentResponse res = new PaymentResponse() ;
        res.setPaymentUrl(session.getUrl()) ;

        return res ;
    }
}











