package com.example.hr.controller;

import com.example.hr.dto.EmployeeResponse;
import com.example.hr.service.EmployeeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
public class EmployeeAvatarController {
  private final EmployeeService employeeService;

  public EmployeeAvatarController(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  @PostMapping("/{id}/avatar")
  public ResponseEntity<EmployeeResponse> upload(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file)
      throws Exception {
    EmployeeResponse response = employeeService.uploadAvatar(id, file.getBytes(), file.getOriginalFilename());
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/avatar")
  public ResponseEntity<byte[]> getAvatar(@PathVariable("id") Long id) {
    byte[] bytes = employeeService.getAvatarBytes(id);
    if (bytes == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes);
  }
}
