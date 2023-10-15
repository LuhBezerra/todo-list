package br.com.luhbezerra.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.luhbezerra.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  @Autowired
  private ITaskRepository taskRepository;

  @PostMapping("/")
  public ResponseEntity<TaskModel> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
    var idUser = (UUID) request.getAttribute("idUser");
    taskModel.setIdUser(idUser);

    var currentDate = LocalDateTime.now();

    if (currentDate.isAfter(taskModel.getStartAt())) {
      throw new RuntimeException("StartAt is before current date");
    }

    if (currentDate.isAfter(taskModel.getEndAt())) {
      throw new RuntimeException("EndAt is before current date");
    }

    if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
      throw new RuntimeException("EndAt is before StartAt");
    }

    var task = this.taskRepository.save(taskModel);

    return ResponseEntity.status(HttpStatus.OK).body(task);
  }

  @GetMapping("/")
  public List<TaskModel> list(HttpServletRequest request) {
    var idUser = (UUID) request.getAttribute("idUser");
    var tasks = this.taskRepository.findByIdUser(idUser);
    return tasks;
  }

  @PutMapping("/{id}")
  public ResponseEntity<TaskModel> update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
    var task = this.taskRepository.findById(id).orElse(null);

    if (task == null) {
      throw new RuntimeException("Task not found");
    }
  
    var idUser = (UUID) request.getAttribute("idUser");

    if (!task.getIdUser().equals(idUser)) {
      throw new RuntimeException("User not allowed");
    }

    Utils.copyNonNullProperties(taskModel, task);

    var taskUpdated = this.taskRepository.save(task);

    return ResponseEntity.ok().body(taskUpdated);
  }
}
