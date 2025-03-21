package com.rafaelgalvezg.orders.exception;

public class OrderStateException extends RuntimeException {
  public OrderStateException(String message) {
    super(message);
  }
}