package com.ssh.exception;

/**
 * YouthBusyException
 *
 * Thrown when an invite is attempted for a youth who is BUSY (has an
 * active task) or whose availability is UNKNOWN. The controller maps
 * this to HTTP 409 with a body of {"code":"YOUTH_BUSY"}.
 *
 * The matchmaker in Python catches this as SpringError with code
 * YOUTH_BUSY and skips the invite.
 */
public class YouthBusyException extends RuntimeException {

    private final Long youthId;
    private final String availability;

    public YouthBusyException(Long youthId, String availability) {
        super("Youth " + youthId + " is not invitable: " + availability);
        this.youthId = youthId;
        this.availability = availability;
    }

    public Long getYouthId() {
        return youthId;
    }

    public String getAvailability() {
        return availability;
    }
}
