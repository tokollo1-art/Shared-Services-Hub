package com.ssh.exception;

import com.ssh.entity.TaskStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Invalid Task Transition Exception
 *
 * Purpose: Thrown when a task is asked to move to a status that the task
 *          state machine does not allow.
 *
 * WHY SSH needs this exception:
 *   - Repeat or out-of-order operations (assign an assigned task, complete an
 *     open task, verify twice) must fail loudly instead of overwriting data
 *   - Answers with 409 Conflict so the client can tell a stale or duplicate
 *     action apart from a missing resource (404) or an authorization failure
 *     (403)
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidTaskTransitionException extends RuntimeException {

    public InvalidTaskTransitionException(Long taskId, TaskStatus from, TaskStatus to) {
        super("Task " + taskId + " cannot move from " + from + " to " + to);
    }

    public InvalidTaskTransitionException(String message) {
        super(message);
    }
}
