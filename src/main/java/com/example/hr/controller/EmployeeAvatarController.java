package com.example.hr.controller;

import com.example.hr.model.Employee;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.service.FaceService;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Employee avatar upload and retrieval.
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeAvatarController {
  private final EmployeeRepository employeeRepository;
  private final FaceService faceService;

  public EmployeeAvatarController(EmployeeRepository employeeRepository, FaceService faceService) {
    this.employeeRepository = employeeRepository;
    this.faceService = faceService;
  }

  @PostMapping("/{id}/avatar")
  public ResponseEntity<?> upload(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file)
      throws Exception {
    Employee employee =
        employeeRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Empty file");
    }
    Path dir = Paths.get("data", "avatars");
    Files.createDirectories(dir);
    Path target = dir.resolve("emp_" + id + ".jpg");
    Files.write(target, file.getBytes());

    String hash = faceService.computeHash(file.getInputStream());
    employee.setAvatarPath(target.toString());
    employee.setFaceHash(hash);
    employeeRepository.save(employee);
    return ResponseEntity.ok().body(java.util.Map.of("message", "Avatar uploaded"));
  }

  @GetMapping("/{id}/avatar")
  public ResponseEntity<byte[]> getAvatar(@PathVariable("id") Long id) throws Exception {
    Employee employee =
        employeeRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    if (employee.getAvatarPath() == null) {
      return ResponseEntity.notFound().build();
    }
    File file = new File(employee.getAvatarPath());
    if (!file.exists()) {
      return ResponseEntity.notFound().build();
    }
    byte[] bytes;
    try (FileInputStream in = new FileInputStream(file)) {
      bytes = in.readAllBytes();
    }
    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
  }
}
