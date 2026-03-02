package com.ticketing.reservation.api;

public final class ReservationEventBindings {
    private ReservationEventBindings() {}

    public static final String EXCHANGE = "reservation.events";

    public static final String RK_CREATED = "reservation.created";
    public static final String RK_CANCELLED = "reservation.cancelled";
    public static final String RK_EXPIRED = "reservation.expired";

    public static final String Q_NOTIFICATION_MAIN = "notification.reservation.events";
    public static final String Q_NOTIFICATION_RETRY = "notification.reservation.events.retry";
    public static final String Q_NOTIFICATION_DLQ = "notification.reservation.events.dlq";

    public static final int EVENT_VERSION = 1;
}
