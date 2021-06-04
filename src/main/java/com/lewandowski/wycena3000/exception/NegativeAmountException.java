package com.lewandowski.wycena3000.exception;

public class NegativeAmountException extends IllegalStateException {
    public NegativeAmountException(String s) {
        super(s);
    }
}
