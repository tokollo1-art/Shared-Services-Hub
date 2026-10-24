package com.ssh.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssh.dto.agent.ActionDto;
import com.ssh.dto.agent.EligibleYouthDto;
import com.ssh.dto.agent.TaskDto;
import com.ssh.dto.agent.YouthStatusDto;
import com.ssh.entity.AuditLog;
import com.ssh.entity.Invitation;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import com.ssh.exception.YouthBusyException;
import com.ssh.repository.AuditLogRepository;
import com.ssh.repository.ExperienceRepository;
import com.ssh.repository.InvitationRepository;
import com.ssh.repository.TaskRepository;
import com.ssh.repository.TaskSkillRepository;
import com.ssh.repository.YouthRepository;
import com.ssh.repository.YouthSkillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AgentService
 *
 * Purpose: The business logic behind every /api/v1/agent/** endpoint.
 *
 * @author SSH Team
 * @version 1.1
 * @since 2026
 */
@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final TaskRepository taskRepository;
    private final YouthRepository youthRepository;
    private final TaskSkillRepository taskSkillRepository;
    private final YouthSkillRepository youthSkillRepository;
    private final ExperienceRepository experienceRepository;
    private final AuditLogRepository auditLogRepository;
    private final InvitationRepository invitationRepository;
    private final ObjectMapper objectMapper;

    public AgentService(
        TaskRepository taskRepository,
        YouthRepository youthRepository,
        TaskSkillRepository taskSkillRepository,
        YouthSkillRepository youthSkillRepository,
        ExperienceRepository experienceRepository,
        AuditLogRepository auditLogRepository,
        InvitationRepository invitationRepository,
        ObjectMapper objectMapper
    ) {
        this.taskRepository = taskRepository;
        this.youthRepository = youthRepository;
        this.taskSkillRepository = taskSkillRepository;
        this.youthSkillRepository = youthSkillRepository;
        this.experienceRepository = experienceRepository;
        this.auditLogRepository = auditLogRepository;
        this.invitationRepository = invitationRepository;
        this.objectMapper = objectMapper;
    }

    // ============================================
    // READ ENDPOINTS
    // ============================================

    @Transactional(readOnly = true)
    public List<EligibleYouthDto> getEligibleYouth(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        List<Youth> allYouth = youthRepository.findAll();
        List<EligibleYouthDto> result = new ArrayList<>();

        for (Youth youth : allYouth) {
            if (youth.isAlumni()) {
                continue;
            }
            String availability = deriveAvailability(youth);
            if ("BUSY".equals(availability)) {
                continue;
            }
            if (!isTierEligible(youth, task)) {
                continue;
            }

            List<String> skills = youthSkillRepository.findSkillSlugsByYouthId(youth.getId());
            int awaitingCount = (int) experienceRepository.countByYouthIdAndIsVerifiedFalse(youth.getId());

            result.add(new EligibleYouthDto(
                youth.getId(),
                skills,
                tierOf(youth),
                availability,
                awaitingCount,
                youth.getAvailability(),
                distanceBucket(task.getLocation(), youth.getLocation())
            ));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public YouthStatusDto getYouthStatus(Long youthId) {
        Youth youth = youthRepository.findById(youthId)
            .orElseThrow(() -> new IllegalArgumentException("Youth not found: " + youthId));

        Long activeTaskId = findActiveTaskId(youth);
        List<Long> awaitingTaskIds = findAwaitingTaskIds(youth);
        int verifiedCount = (int) experienceRepository.countByYouthIdAndIsVerifiedTrue(youthId);

        String availability;
        if (activeTaskId != null) {
            availability = "BUSY";
        } else if (!awaitingTaskIds.isEmpty()) {
            availability = "AWAITING_VERIFICATION";
        } else {
            availability = "FREE";
        }

        return new YouthStatusDto(
            availability,
            activeTaskId,
            awaitingTaskIds,
            tierOf(youth),
            verifiedCount
        );
    }

    @Transactional(readOnly = true)
    public TaskDto getTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        List<String> requiredSkills = taskSkillRepository.findSkillSlugsByTaskId(taskId);

        return new TaskDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getTier() != null ? task.getTier() : 0,
            requiredSkills,
            task.isRemote(),
            task.getEstHours() != null ? task.getEstHours() : 8.0,
            task.getSme() != null ? task.getSme().getId() : null,
            task.getReservedUntil()
        );
    }

    // ============================================
    // WRITE ENDPOINTS
    // ============================================

    @Transactional
    public AuditLog saveAction(ActionDto dto, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<AuditLog> existing =
                auditLogRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        AuditLog logEntry = new AuditLog();
        logEntry.setActorType("AGENT");
        logEntry.setAgentName(dto.getAgent());
        logEntry.setAction(dto.getDecision());
        logEntry.setEntityType("TASK");
        logEntry.setEntityId(extractEntityId(dto));
        logEntry.setTriggerEventId(
            dto.getTriggerEventId() != null ? dto.getTriggerEventId().toString() : null
        );
        logEntry.setInputs(dto.getInputsSummary());
        logEntry.setReasoning(dto.getReasoning());
        logEntry.setOutput(serializeOutputs(dto));
        logEntry.setUsedFallback(Boolean.TRUE.equals(dto.getUsedFallback()));
        logEntry.setModelVersion(dto.getModelVersion());
        logEntry.setIdempotencyKey(idempotencyKey);
        logEntry.setTaskId(extractTaskId(dto));
        logEntry.setYouthId(extractYouthId(dto));

        return auditLogRepository.save(logEntry);
    }

    @Transactional
    public AuditLog saveDraft(
        Long taskId,
        String text,
        List<Map<String, Object>> skills,
        String idempotencyKey
    ) {
        AuditLog logEntry = new AuditLog();
        logEntry.setActorType("AGENT");
        logEntry.setAgentName("evidence");
        logEntry.setAction("REFERENCE_DRAFT");
        logEntry.setEntityType("TASK");
        logEntry.setEntityId(taskId);
        logEntry.setTaskId(taskId);
        logEntry.setIdempotencyKey(idempotencyKey);

        Map<String, Object> payload = new HashMap<>();
        payload.put("referenceDraft", text);
        payload.put("skillTags", skills);

        try {
            logEntry.setOutput(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            logEntry.setOutput("{\"error\":\"serialization failed\"}");
        }

        return auditLogRepository.save(logEntry);
    }

    /**
     * POST /api/v1/agent/invitations
     *
     * Idempotent on (task, youth). If an invitation already exists,
     * returns it. If the youth is busy, returns a 409 conflict — the
     * matchmaker catches the exception and skips.
     */
    @Transactional
    public Invitation invite(Long taskId, Long youthId, String note) {
        Optional<Invitation> existing =
            invitationRepository.findByTaskIdAndYouthId(taskId, youthId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Youth youth = youthRepository.findById(youthId)
            .orElseThrow(() -> new IllegalArgumentException("Youth not found: " + youthId));

        String availability = deriveAvailability(youth);
        if (!"FREE".equals(availability) && !"AWAITING_VERIFICATION".equals(availability)) {
            throw new YouthBusyException(youthId, availability);
        }

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        Invitation invitation = new Invitation(task, youth, note);
        return invitationRepository.save(invitation);
    }

    /**
     * POST /api/v1/agent/invitations/withdraw
     *
     * Withdraws every pending invitation for this youth except the one
     * for the given task. Returns the number of rows withdrawn.
     */
    @Transactional
    public int withdraw(Long youthId, Long exceptTaskId) {
        if (exceptTaskId == null) {
            exceptTaskId = -1L;
        }
        return invitationRepository.withdrawPendingExcept(youthId, exceptTaskId);
    }

    // ============================================
    // HELPERS
    // ============================================

    private String deriveAvailability(Youth youth) {
        Long activeTaskId = findActiveTaskId(youth);
        if (activeTaskId != null) {
            return "BUSY";
        }
        List<Long> awaiting = findAwaitingTaskIds(youth);
        if (!awaiting.isEmpty()) {
            return "AWAITING_VERIFICATION";
        }
        return "FREE";
    }

    private Long findActiveTaskId(Youth youth) {
        return taskRepository
            .findFirstByAssignedToIdAndStatusIn(
                youth.getId(),
                List.of("IN_PROGRESS")
            )
            .map(Task::getId)
            .orElse(null);
    }

    private List<Long> findAwaitingTaskIds(Youth youth) {
        return taskRepository
            .findByAssignedToIdAndStatusIn(
                youth.getId(),
                List.of("COMPLETED")
            )
            .stream()
            .map(Task::getId)
            .toList();
    }

    private boolean isTierEligible(Youth youth, Task task) {
        int youthTier = tierOf(youth);
        int taskTier = task.getTier() != null ? task.getTier() : 0;
        return youthTier >= taskTier;
    }

    private int tierOf(Youth youth) {
        int verified = (int) experienceRepository.countByYouthIdAndIsVerifiedTrue(youth.getId());
        if (verified >= 10) return 2;
        if (verified >= 3) return 1;
        return 0;
    }

    private String distanceBucket(String taskLocation, String youthLocation) {
        if (taskLocation == null || youthLocation == null) {
            return null;
        }
        return taskLocation.equalsIgnoreCase(youthLocation) ? "near" : "far";
    }

    private String serializeOutputs(ActionDto dto) {
        if (dto.getOutputs() == null || dto.getOutputs().isNull()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(dto.getOutputs());
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Long extractEntityId(ActionDto dto) {
        return null;
    }

    private Long extractTaskId(ActionDto dto) {
        if (dto.getInputsSummary() == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("task=(\\d+)")
            .matcher(dto.getInputsSummary());
        return m.find() ? Long.parseLong(m.group(1)) : null;
    }

    private Long extractYouthId(ActionDto dto) {
        if (dto.getInputsSummary() == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("youth=(\\d+)")
            .matcher(dto.getInputsSummary());
        return m.find() ? Long.parseLong(m.group(1)) : null;
    }
}
