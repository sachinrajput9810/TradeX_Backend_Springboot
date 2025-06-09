package com.nt.service;

import com.nt.domain.OrderType;
import com.nt.model.Order;
import com.nt.model.User;
import com.nt.model.Wallet;
import com.nt.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Override
    public Wallet getUserWallet(User user) {
        Wallet wallet = walletRepository.findByUserId(user.getId());
        if (wallet != null) return wallet;

        // Wallet does not exist, create one with 0 balance
        Wallet newWallet = new Wallet();
        newWallet.setUser(user);
        return walletRepository.save(newWallet);
    }

    @Override
    public Wallet addBalanceToWallet(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet findWalletById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    @Override
    public Wallet walletToWalletTransfer(User sender, Wallet receiverWallet, BigDecimal amount) {
    	System.out.println("Amount to be tranfered :: " + amount);
        Wallet senderWallet = getUserWallet(sender);

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        return senderWallet;
    }

    @Override
    public Wallet payOrderPayment(Order order, User user) {
        Wallet wallet = getUserWallet(user);

        if (order.getOrderType().equals(OrderType.BUY)) {
            BigDecimal newBalance = wallet.getBalance().subtract(order.getPrice());

            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Insufficient balance");
            }

            wallet.setBalance(newBalance);
        } else {
            wallet.setBalance(wallet.getBalance().add(order.getPrice()));
        }

        return walletRepository.save(wallet);
    }
}
