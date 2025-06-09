package com.nt.controller;

import com.nt.model.Order;
import com.nt.model.PaymentOrder;
import com.nt.model.User;
import com.nt.model.Wallet;
import com.nt.model.WalletTransaction;
import com.nt.response.PaymentResponse;
import com.nt.service.OrderService;
import com.nt.service.PaymentService;
import com.nt.service.UserService;
import com.nt.service.WalletService;
import com.razorpay.RazorpayException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
public class WalletController {

    @Autowired
    private WalletService walletService ;

    @Autowired
    private UserService userService ;

    @Autowired
    private OrderService orderService ;

    @Autowired
    private PaymentService paymentService ;


    @GetMapping("/api/wallet")
    public ResponseEntity<Wallet> getUserWallet(@RequestHeader("Authorization") String token) {
        User user = userService.findUserProfileByJwt(token);
        Wallet wallet = walletService.getUserWallet(user);
        return ResponseEntity.ok(wallet);
    }

    @PutMapping("/api/wallet/{walletId}/transfer")
    public ResponseEntity<Wallet> walletToWalletTransfer(@RequestHeader("Authorization") String jwt,
                                                         @PathVariable Long walletId ,
                                                         @RequestBody WalletTransaction req)
    {
        User senderUser = userService.findUserProfileByJwt(jwt);
        Wallet recieverWallet = walletService.findWalletById(walletId);
        Wallet wallet = walletService.walletToWalletTransfer(senderUser,recieverWallet , BigDecimal.valueOf(req.getAmount()));
        
        
        return ResponseEntity.ok(wallet);

    }

    @PutMapping("/api/wallet/order/{orderId}/pay")
    public ResponseEntity<Wallet> payOrderPayment(@RequestHeader("Authorization") String jwt,
                                                         @PathVariable Long walletID ,
                                                         @RequestBody WalletTransaction req)
    {
        User user = userService.findUserProfileByJwt(jwt);
        Order order = orderService.getOrderById(walletID);
        Wallet wallet = walletService.payOrderPayment(order , user);
        return new ResponseEntity<>(wallet, HttpStatus.OK) ;
    }
    
    @PutMapping("/api/wallet/deposit")
    public ResponseEntity<Wallet> addBalanceToWallet(@RequestHeader("Authorization") String jwt,
                                                   @RequestParam(name = "order_id") Long orderId ,
                                                   @RequestParam(name = "payment_id" ) String paymentId 
                                                   ) throws RazorpayException
    {
        User user = userService.findUserProfileByJwt(jwt);
        Wallet wallet = walletService.getUserWallet(user) ;
        PaymentOrder order = paymentService.getPaymentOrderById(orderId) ;
        System.out.println("Payment Order " + order);
        Boolean status =paymentService.proceedPaymentOrder(order, paymentId) ;
        if(wallet.getBalance() == null) {
        	wallet.setBalance(BigDecimal.valueOf(0)) ;
        }
        if(status) {
        	wallet= walletService.addBalanceToWallet(wallet, order.getAmount()) ; 
        }
        return new ResponseEntity<>(wallet, HttpStatus.OK) ;
    }


}

