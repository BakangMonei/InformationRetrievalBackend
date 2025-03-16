package com.moneibakang.informationretrievalbackend.exception;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

public class IndexException extends RuntimeException {
    public IndexException(String message) {
        super(message);
    }

    public IndexException(String message, Throwable cause) {
        super(message, cause);
    }
}