package com.nt.model;


import java.math.BigDecimal;

import com.nt.domain.PaymentMethod;
import com.nt.domain.PaymentOrderStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class PaymentOrder {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id ;
	
	private BigDecimal amount ;
	
	private PaymentOrderStatus status ;
	
	private PaymentMethod paymentMethod ;
	
	@ManyToOne
	private User user ;
}
