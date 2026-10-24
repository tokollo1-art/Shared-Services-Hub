package com.ssh.service;

import com.ssh.dto.response.ExperienceResponse;
import com.ssh.entity.Experience;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import com.ssh.exception.BusinessRuleException;
import com.ssh.exception.ResourceNotFoundException;
import com.ssh.repository.ExperienceRepository;
import com.ssh.repository.YouthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Experience Ledger records.
 *
 * A completion creates exactly one Experience per task. Revision cycles reuse
 * that same row, so a task can never accumulate duplicate experience records.
 */
@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final YouthRepository youthRepository;

    public ExperienceService(ExperienceRepository experienceRepository,
                             YouthRepository youthRepository) {
        this.experienceRepository = experienceRepository;
        this.youthRepository = youthRepository;
    }

    /**
     * Called inside the completion transaction. Creates the experience on the
     * first completion and re-opens the existing one after a revision.
     */
    @Transactional
    public Experience recordCompletion(Task task, Youth youth) {
        if (task == null || task.getId() == null) {
            throw new IllegalArgumentException("A persisted task is required");
        }
        if (youth == null) {
            throw new IllegalArgumentException("An assigned youth is required");
        }

        Experience experience = experienceRepository.findFirstByTask(task).orElse(null);
        if (experience != null) {
            if (experience.isVerified()) {
                throw new BusinessRuleException(
                    "This task already has a verified experience and cannot be submitted again");
            }
            experience.resubmit();
            return experienceRepository.save(experience);
        }

        Experience created = new Experience(youth, task);
        created.setStartDate(LocalDateTime.now());
        return experienceRepository.save(created);
    }

    /**
     * Marks the existing experience REJECTED with the SME's reason. A task
     * completed before this feature existed may not have an experience yet;
     * in that case there is nothing to reject.
     */
    @Transactional
    public void rejectForRevision(Task task, String reason) {
        experienceRepository.findFirstByTask(task).ifPresent(experience -> {
            experience.reject(reason);
            experienceRepository.save(experience);
        });
    }

    /**
     * The single experience for a completed task.
     */
    public Experience requireForTask(Task task) {
        return experienceRepository.findFirstByTask(task)
            .orElseThrow(() -> new BusinessRuleException(
                "No completion record exists for task " + task.getId()
                    + "; the youth must complete it first"));
    }

    public Experience requireExperience(Long experienceId) {
        return experienceRepository.findById(experienceId)
            .orElseThrow(() -> new ResourceNotFoundException("Experience", experienceId));
    }

    @Transactional
    public Experience save(Experience experience) {
        return experienceRepository.save(experience);
    }

    public List<ExperienceResponse> listExperiencesForYouth(Long youthId) {
        Youth youth = youthRepository.findById(youthId)
            .orElseThrow(() -> new ResourceNotFoundException("Youth", youthId));

        return experienceRepository.findByYouth(youth)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ExperienceResponse getExperienceById(Long id) {
        return toResponse(requireExperience(id));
    }

    public ExperienceResponse toResponse(Experience experience) {
        ExperienceResponse response = new ExperienceResponse();
        response.setId(experience.getId());
        response.setStatus(experience.getStatus());
        response.setVerified(experience.isVerified());
        response.setFeedback(experience.getFeedback());
        response.setRating(experience.getRating());
        response.setReference(experience.getReference());
        response.setRejectionReason(experience.getRejectionReason());
        response.setVerifiedAt(experience.getVerifiedAt());
        response.setCreatedAt(experience.getCreatedAt());
        response.setUpdatedAt(experience.getUpdatedAt());

        if (experience.getTask() != null) {
            response.setTaskId(experience.getTask().getId());
            response.setTaskTitle(experience.getTask().getTitle());
            if (experience.getTask().getSme() != null) {
                response.setSmeBusinessName(experience.getTask().getSme().getBusinessName());
            }
        }

        if (experience.getYouth() != null) {
            response.setYouthId(experience.getYouth().getId());
            response.setYouthName(experience.getYouth().getUser().getName());
        }

        return response;
    }
}
