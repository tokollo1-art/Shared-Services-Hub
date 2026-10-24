package com.ssh.repository;

import com.ssh.entity.Application;
import com.ssh.entity.Task;
import com.ssh.entity.Youth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByTaskAndYouth(Task task, Youth youth);

    List<Application> findByYouthOrderByCreatedAtDesc(Youth youth);

    List<Application> findByTaskOrderByCreatedAtAsc(Task task);

    List<Application> findByYouthAndStatusIn(Youth youth, Collection<String> statuses);

    List<Application> findByStatusAndExpiresAtBefore(String status, LocalDateTime cutoff);

    long countByTaskAndStatus(Task task, String status);

    long countByYouthAndStatus(Youth youth, String status);
}
