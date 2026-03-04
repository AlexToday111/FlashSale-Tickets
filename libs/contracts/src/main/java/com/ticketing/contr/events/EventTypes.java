package com.ticketing.contr.events;

public class EventTypes {
    private EventTypes() {
    }
    public static final String PAYMENT_REQUESTED = "payment.requested";
    public static final String PAYMENT_SUCCEEDED = "payment.succeeded";
    public static final String PAYMENT_FAILED = "payment.failed";
}
