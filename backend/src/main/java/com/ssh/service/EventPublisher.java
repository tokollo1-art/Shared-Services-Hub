package com.ssh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssh.entity.Task;
import com.ssh.repository.TaskSkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * EventPublisher
 *
 * Purpose: Publishes webhook events to the AI service after the
 *          database transaction commits.
 *
 * How it works:
 *   1. A transactional service method calls publishTaskPosted(task).
 *   2. The event is wrapped in an EventEnvelope and published through
 *      Spring's ApplicationEventPublisher.
 *   3. Spring holds the event until the transaction commits.
 *   4. @TransactionalEventListener(phase = AFTER_COMMIT) fires and POSTs
 *      to the AI service's /events endpoint.
 *   5. If the transaction rolls back, the event is discarded and logged.
 *
 * Delivery is at-most-once by design. If the AI service is down, the
 * event is lost. For at-least-once, replace the POST with a Redis Stream
 * (XADD) and have the AI service run a consumer loop.
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Service
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ApplicationEventPublisher springPublisher;
    private final TaskSkillRepository taskSkillRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String aiServiceUrl;
    private final String webhookToken;
    private final boolean enabled;

    public EventPublisher(
        ApplicationEventPublisher springPublisher,
        TaskSkillRepository taskSkillRepository,
        ObjectMapper objectMapper,
        @Value("${ssh.ai-service.url:http://localhost:8000}") String aiServiceUrl,
        @Value("${ssh.ai-service.webhook-token:}") String webhookToken,
        @Value("${ssh.ai-service.enabled:true}") boolean enabled
    ) {
        this.springPublisher = springPublisher;
        this.taskSkillRepository = taskSkillRepository;
        this.objectMapper = objectMapper;
        this.aiServiceUrl = aiServiceUrl.endsWith("/")
            ? aiServiceUrl.substring(0, aiServiceUrl.length() - 1)
            : aiServiceUrl;
        this.webhookToken = webhookToken;
        this.enabled = enabled;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    // ============================================
    // PUBLIC API — call these from services
    // ============================================

    public void publishTaskPosted(Task task) {
        if (!enabled) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("task", taskPayload(task));
        springPublisher.publishEvent(new EventEnvelope(envelope("TASK_POSTED", payload)));
    }

    public void publishTaskAssigned(Long taskId, Long youthId, Long smeId) {
        if (!enabled) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("youthId", youthId);
        payload.put("smeId", smeId);
        springPublisher.publishEvent(new EventEnvelope(envelope("TASK_ASSIGNED", payload)));
    }

    public void publishTaskCompleted(Long taskId, Long youthId, Long smeId, String reflection) {
        if (!enabled) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("youthId", youthId);
        payload.put("smeId", smeId);
        payload.put("reflection", reflection != null ? reflection : "");
        springPublisher.publishEvent(new EventEnvelope(envelope("TASK_COMPLETED", payload)));
    }

    public void publishTaskCancelled(Long taskId, Long youthId) {
        if (!enabled) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("youthId", youthId);
        springPublisher.publishEvent(new EventEnvelope(envelope("TASK_CANCELLED", payload)));
    }

    public void publishExperienceVerified(
        Long taskId, Long youthId, Long smeId,
        int rating, String feedback, String reference, List<String> confirmedSkills
    ) {
        if (!enabled) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("youthId", youthId);
        payload.put("smeId", smeId);
        payload.put("rating", rating);
        payload.put("smeFeedback", feedback != null ? feedback : "");
        payload.put("reference", reference != null ? reference : "");
        payload.put("confirmedSkills", confirmedSkills != null ? confirmedSkills : List.of());
        springPublisher.publishEvent(new EventEnvelope(envelope("EXPERIENCE_VERIFIED", payload)));
    }

    public void publishApplicationSubmitted(Long taskId, Long youthId, Long applicationId) {
        if (!enabled) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", taskId);
        payload.put("youthId", youthId);
        payload.put("applicationId", applicationId);
        springPublisher.publishEvent(new EventEnvelope(envelope("APPLICATION_SUBMITTED", payload)));
    }

    public void publishYouthIdle(Long youthId, String reason) {
        if (!enabled) return;
        Map<String, Object> payload = new HashMap<>();
        payload.put("youthId", youthId);
        payload.put("reason", reason != null ? reason : "");
        springPublisher.publishEvent(new EventEnvelope(envelope("YOUTH_IDLE", payload)));
    }

    // ============================================
    // TRANSACTIONAL LISTENERS
    // ============================================

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onEventEnvelope(EventEnvelope envelope) {
        postToAiService(envelope);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void onRollback(EventEnvelope envelope) {
        log.info(
            "event.discarded type={} eventId={}",
            envelope.payload.get("type"),
            envelope.payload.get("eventId")
        );
    }

    // ============================================
    // INTERNALS
    // ============================================

    private void postToAiService(EventEnvelope envelope) {
        String json;
        try {
            json = objectMapper.writeValueAsString(envelope.payload);
        } catch (JsonProcessingException e) {
            log.error("event.serialize.failed type={}", envelope.payload.get("type"), e);
            return;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceUrl + "/events"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + webhookToken)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info(
                    "event.published type={} eventId={} status={}",
                    envelope.payload.get("type"),
                    envelope.payload.get("eventId"),
                    response.statusCode()
                );
            } else {
                log.warn(
                    "event.publish.rejected type={} eventId={} status={} body={}",
                    envelope.payload.get("type"),
                    envelope.payload.get("eventId"),
                    response.statusCode(),
                    response.body()
                );
            }
        } catch (Exception e) {
            log.warn(
                "event.publish.failed type={} eventId={} error={}",
                envelope.payload.get("type"),
                envelope.payload.get("eventId"),
                e.getMessage()
            );
        }
    }

    private Map<String, Object> envelope(String type, Map<String, Object> payload) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", UUID.randomUUID().toString());
        event.put("type", type);
        event.put("occurredAt", LocalDateTime.now().format(ISO));
        event.put("version", 1);
        event.putAll(payload);
        return event;
    }

    private Map<String, Object> taskPayload(Task task) {
        Map<String, Object> taskMap = new HashMap<>();
        taskMap.put("id", task.getId());
        taskMap.put("title", task.getTitle());
        taskMap.put("description", task.getDescription());
        taskMap.put("tier", task.getTier() != null ? task.getTier() : 0);
        taskMap.put("requiredSkills", taskSkillRepository.findSkillSlugsByTaskId(task.getId()));
        taskMap.put("remote", task.isRemote());
        taskMap.put("estHours", task.getEstHours() != null ? task.getEstHours() : 8.0);
        taskMap.put("smeId", task.getSme() != null ? task.getSme().getId() : null);
        taskMap.put("reservedUntil", task.getReservedUntil() != null
            ? task.getReservedUntil().format(ISO)
            : null);
        return taskMap;
    }

    public record EventEnvelope(Map<String, Object> payload) {}
}
