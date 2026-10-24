package com.ssh.controller;

import com.ssh.dto.agent.ActionDto;
import com.ssh.dto.agent.EligibleYouthDto;
import com.ssh.dto.agent.TaskDto;
import com.ssh.dto.agent.YouthStatusDto;
import com.ssh.entity.AuditLog;
import com.ssh.entity.Invitation;
import com.ssh.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AgentController
 *
 * Purpose: The AI service's API. All endpoints under /api/v1/agent/**.
 *
 * Security: restricted to ROLE_AGENT in SecurityConfig. Every other
 * role — youth, SME, admin, corporate — gets 403.
 *
 * Guardrail: the agent can read and write only here. It cannot assign,
 * verify, or pay. Those operations live in Slot 1's controllers.
 *
 * @author SSH Team
 * @version 1.1
 * @since 2026
 */
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    // ============================================
    // READ ENDPOINTS
    // ============================================

    /**
     * GET /api/v1/agent/tasks/{id}/eligible-youth
     *
     * The matchmaker's candidate list. Busy youth excluded by the server.
     */
    @GetMapping("/tasks/{id}/eligible-youth")
    public ResponseEntity<List<EligibleYouthDto>> getEligibleYouth(@PathVariable Long id) {
        List<EligibleYouthDto> youth = agentService.getEligibleYouth(id);
        return ResponseEntity.ok(youth);
    }

    /**
     * GET /api/v1/agent/tasks/{id}
     *
     * The evidence agent's task lookup. Populated from task_skills so
     * requiredSkills is a slug list, not an empty array.
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskDto> getTask(@PathVariable Long id) {
        TaskDto task = agentService.getTask(id);
        return ResponseEntity.ok(task);
    }

    /**
     * GET /api/v1/agent/youth/{id}/status
     *
     * The availability guard's state read. Called immediately before
     * every invite to catch a youth who became busy mid-flow.
     */
    @GetMapping("/youth/{id}/status")
    public ResponseEntity<YouthStatusDto> getYouthStatus(@PathVariable Long id) {
        YouthStatusDto status = agentService.getYouthStatus(id);
        return ResponseEntity.ok(status);
    }

    // ============================================
    // WRITE ENDPOINTS
    // ============================================

    /**
     * POST /api/v1/agent/actions
     *
     * Idempotent. A repeat with the same Idempotency-Key returns the
     * original row with 201, not a new row. The response body is the
     * AuditLog, which the caller can ignore.
     */
    @PostMapping("/actions")
    public ResponseEntity<AuditLog> saveAction(
        @RequestBody ActionDto action,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        log.info(
            "agent.action agent={} decision={} key={}",
            action.getAgent(),
            action.getDecision(),
            idempotencyKey
        );
        AuditLog saved = agentService.saveAction(action, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * POST /api/v1/agent/drafts/reference
     *
     * Stores the reference draft as a REFERENCE_DRAFT activity record.
     * The SME reads it from the activity feed, edits, and signs it via
     * Slot 1's verify call.
     */
    @PostMapping("/drafts/reference")
    public ResponseEntity<AuditLog> saveDraft(
        @RequestBody Map<String, Object> body,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        Long taskId = toLong(body.get("taskId"));
        String text = String.valueOf(body.getOrDefault("text", ""));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> skills =
            (List<Map<String, Object>>) body.getOrDefault("skills", List.of());

        AuditLog saved = agentService.saveDraft(taskId, text, skills, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * POST /api/v1/agent/invitations
     *
     * Creates an invitation for a youth to a task.
     *
     * Idempotent on (task, youth): if an invitation already exists, it
     * returns 200 with that row instead of 201 with a new one.
     *
     * Returns 409 YOUTH_BUSY if the youth has an active task or an
     * unknown availability state. The matchmaker catches this and skips.
     */
    @PostMapping("/invitations")
    public ResponseEntity<Invitation> invite(
        @RequestBody Map<String, Object> body,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        Long taskId = toLong(body.get("taskId"));
        Long youthId = toLong(body.get("youthId"));
        String note = String.valueOf(body.getOrDefault("note", ""));

        log.info(
            "agent.invite task={} youth={} key={}",
            taskId,
            youthId,
            idempotencyKey
        );

        Invitation invitation = agentService.invite(taskId, youthId, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(invitation);
    }

    /**
     * POST /api/v1/agent/invitations/withdraw
     *
     * Withdraws every pending invitation for a youth except the one
     * for the given task. Returns 204 No Content.
     */
    @PostMapping("/invitations/withdraw")
    public ResponseEntity<Void> withdraw(
        @RequestBody Map<String, Object> body,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        Long youthId = toLong(body.get("youthId"));
        Long exceptTaskId = toLong(body.get("exceptTaskId"));

        log.info(
            "agent.withdraw youth={} exceptTask={} key={}",
            youthId,
            exceptTaskId,
            idempotencyKey
        );

        int withdrawn = agentService.withdraw(youthId, exceptTaskId);
        log.info("agent.withdraw.youth={} withdrawn={}", youthId, withdrawn);
        return ResponseEntity.noContent().build();
    }

    // ============================================
    // HELPER
    // ============================================

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
