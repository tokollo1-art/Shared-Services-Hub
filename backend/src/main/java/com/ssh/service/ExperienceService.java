package com.ssh.service;

import com.ssh.dto.response.ExperienceResponse;
import com.ssh.entity.Experience;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import com.ssh.exception.ResourceNotFoundException;
import com.ssh.repository.ExperienceRepository;
import com.ssh.repository.TaskRepository;
import com.ssh.repository.YouthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Experience Service
 *
 * Purpose: Business logic for the Experience Ledger.
 *
 * WHY SSH needs this service:
 *   - Every verified task completion becomes a permanent experience record
 *   - Youth can see their full verified work history
 *   - SMEs verify the work to make the record trusted
 *   - This is SSH's core differentiator from a job board
 *
 * @author SSH Team
 * @version 1.0
 * @since 2026
 */
@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final TaskRepository taskRepository;
    private final YouthRepository youthRepository;

    public ExperienceService(ExperienceRepository experienceRepository,
                             TaskRepository taskRepository,
                             YouthRepository youthRepository) {
        this.experienceRepository = experienceRepository;
        this.taskRepository = taskRepository;
        this.youthRepository = youthRepository;
    }

    /**
     * Create a new experience record when a task is completed.
     * Called by TaskService when a youth marks a task complete.
     */
    @Transactional
    public ExperienceResponse createExperience(Long taskId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        if (task.getAssignedTo() == null) {
            throw new IllegalStateException(
                "Cannot create an experience for a task with no assigned youth");
        }

        Youth youth = task.getAssignedTo();

        Experience experience = new Experience(youth, task);
        Experience saved = experienceRepository.save(experience);

        return toExperienceResponse(saved);
    }

    /**
     * Verify an experience. Called by the SME who owns the task.
     * Sets isVerified=true, records the timestamp, and stores feedback/rating.
     */
    @Transactional
    public ExperienceResponse verifyExperience(Long experienceId,
                                               String feedback,
                                               Double rating) {
        Experience experience = experienceRepository.findById(experienceId)
            .orElseThrow(() -> new ResourceNotFoundException("Experience", experienceId));

        if (experience.isVerified()) {
            throw new IllegalStateException("Experience already verified");
        }

        experience.verify(feedback, rating);
        Experience saved = experienceRepository.save(experience);

        return toExperienceResponse(saved);
    }

    /**
     * List all experiences for a given youth.
     * This is the Experience Ledger.
     */
    public List<ExperienceResponse> listExperiencesForYouth(Long youthId) {
        Youth youth = youthRepository.findById(youthId)
            .orElseThrow(() -> new ResourceNotFoundException("Youth", youthId));

        return experienceRepository.findByYouth(youth)
            .stream()
            .map(this::toExperienceResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a single experience by ID.
     */
    public ExperienceResponse getExperienceById(Long id) {
        Experience experience = experienceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Experience", id));
        return toExperienceResponse(experience);
    }

    /**
     * Convert an Experience entity to a safe DTO.
     */
    private ExperienceResponse toExperienceResponse(Experience experience) {
        ExperienceResponse response = new ExperienceResponse();
        response.setId(experience.getId());
        response.setStatus(experience.getStatus());
        response.setVerified(experience.isVerified());
        response.setFeedback(experience.getFeedback());
        response.setRating(experience.getRating());
        response.setVerifiedAt(experience.getVerifiedAt());
        response.setCreatedAt(experience.getCreatedAt());
        response.setUpdatedAt(experience.getUpdatedAt());

        // Task summary
        if (experience.getTask() != null) {
            response.setTaskId(experience.getTask().getId());
            response.setTaskTitle(experience.getTask().getTitle());
            if (experience.getTask().getSme() != null) {
                response.setSmeBusinessName(experience.getTask().getSme().getBusinessName());
            }
        }

        // Youth summary
        if (experience.getYouth() != null) {
            response.setYouthId(experience.getYouth().getId());
            response.setYouthName(experience.getYouth().getUser().getName());
        }

        return response;
    }
}
