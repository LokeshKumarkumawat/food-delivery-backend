package com.bytebyteboot.foodapp.exceptions;


public class PaymentProcessingException extends RuntimeException{

    public PaymentProcessingException(String message){
        super(message);
    }
}
