package com.example.case_viewer.entity;

/** Activity types used for secure-inbox messages. */
public final class ActivityType {

    /** A message sent by the customer. */
    public static final String INBOUND = "INBOUND";

    /** A reply sent by an agent. */
    public static final String OUTBOUND = "OUTBOUND";

    private ActivityType() {
    }
}
