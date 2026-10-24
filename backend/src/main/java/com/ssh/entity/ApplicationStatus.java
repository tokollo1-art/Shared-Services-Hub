package com.ssh.entity;

import java.util.Set;

/**
 * Allowed application statuses.
 *
 * PENDING  - waiting for the SME to assign the task; holds a reservation
 * ACCEPTED - the SME assigned the task to this youth
 * REJECTED - the SME chose another applicant (or the task was filled)
 * WITHDRAWN - the youth withdrew before a decision
 * EXPIRED  - the reservation window passed without a decision
 */
public final class ApplicationStatus {

    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String REJECTED = "REJECTED";
    public static final String WITHDRAWN = "WITHDRAWN";
    public static final String EXPIRED = "EXPIRED";

    /**
     * Statuses that may be reactivated when a youth applies again.
     */
    public static final Set<String> REAPPLICABLE =
        Set.of(REJECTED, WITHDRAWN, EXPIRED);

    private ApplicationStatus() {
    }
}
