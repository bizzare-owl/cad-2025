package ru.gitverse.bizzareowl.lab2;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByOrderByPriorityAsc();
    List<Task> findAllByOrderByPriorityDesc();
    List<Task> findAllByOrderByCreatedAtDesc();
    List<Task> findAllByOrderByTitleAsc();
    List<Task> findAll(Sort sort);
}
