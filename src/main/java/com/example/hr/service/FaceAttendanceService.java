package com.example.hr.service;

import com.example.hr.exception.EmployeeNotActiveException;
import com.example.hr.exception.EmployeeNotFoundException;
import com.example.hr.exception.FaceVerificationFailedException;
import com.example.hr.exception.InvalidStateException;
import com.example.hr.model.Attendance;
import com.example.hr.model.Employee;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FaceAttendanceService {
    
    private static final double FACE_SIMILARITY_THRESHOLD = 80.0;

    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final FaceRecognitionService faceRecognitionService;

    public FaceAttendanceService(
            EmployeeRepository employeeRepository,
            AttendanceRepository attendanceRepository,
            FaceRecognitionService faceRecognitionService) {
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.faceRecognitionService = faceRecognitionService;
    }

    public VerificationResult verify(Long employeeId, InputStream image) throws Exception {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        if (!"在职".equals(employee.getStatus())) {
            throw new EmployeeNotActiveException(employeeId);
        }
        
        if (employee.getAvatarPath() == null) {
            throw new InvalidStateException("员工", "无头像", "有头像");
        }
        
        byte[] probeImage = image.readAllBytes();
        byte[] referenceImage = Files.readAllBytes(Path.of(employee.getAvatarPath()));
        
        FaceRecognitionService.FaceResult result = faceRecognitionService.verify(
            probeImage, 
            referenceImage
        );
        
        return new VerificationResult(
            result.matched(),
            result.similarity(),
            result.algorithm(),
            100.0 - result.similarity(),
            result.threshold()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance attendance(Long employeeId, InputStream image) throws Exception {
        VerificationResult result = verify(employeeId, image);
        ensureSimilarity(result);
        
        if (!result.matched()) {
            throw new FaceVerificationFailedException(result.similarity(), FACE_SIMILARITY_THRESHOLD);
        }
        
        LocalDate today = LocalDate.now();
        List<Attendance> todayRecords = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today);
        
        boolean hasCheckIn = todayRecords.stream().anyMatch(a -> a.getCheckIn() != null);
        boolean hasCheckOut = todayRecords.stream().anyMatch(a -> a.getCheckOut() != null);
        
        if (!hasCheckIn) {
            return doCheckIn(employeeId);
        } else if (!hasCheckOut) {
            return doCheckOut(employeeId);
        } else {
            throw new InvalidStateException("考勤", "今日已完成打卡", "次日再打卡");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance checkIn(Long employeeId, InputStream image) throws Exception {
        VerificationResult result = verify(employeeId, image);
        ensureSimilarity(result);
        
        if (!result.matched()) {
            throw new FaceVerificationFailedException(result.similarity(), FACE_SIMILARITY_THRESHOLD);
        }
        
        LocalDate today = LocalDate.now();
        attendanceRepository.findTopByEmployeeIdAndWorkDateAndCheckOutIsNullOrderByCheckInDesc(
            employeeId, today
        ).ifPresent(open -> {
            throw new InvalidStateException("考勤", "已签到未签退", "已签退");
        });
        
        return doCheckIn(employeeId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Attendance checkOut(Long employeeId, InputStream image) throws Exception {
        VerificationResult result = verify(employeeId, image);
        ensureSimilarity(result);
        
        if (!result.matched()) {
            throw new FaceVerificationFailedException(result.similarity(), FACE_SIMILARITY_THRESHOLD);
        }
        
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findTopByEmployeeIdAndWorkDateAndCheckOutIsNullOrderByCheckInDesc(
            employeeId, today
        ).orElseThrow(() -> new InvalidStateException("考勤", "无签到记录", "已签到"));
        
        LocalTime now = LocalTime.now();
        if (now.isBefore(attendance.getCheckIn())) {
            throw new InvalidStateException("考勤", "签退时间早于签到时间", "签退时间晚于签到时间");
        }
        
        attendance.setCheckOut(now);
        return attendanceRepository.save(attendance);
    }

    private Attendance doCheckIn(Long employeeId) {
        Attendance attendance = new Attendance();
        attendance.setEmployee(employeeRepository.findById(employeeId)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeId)));
        attendance.setWorkDate(LocalDate.now());
        attendance.setStatus("Normal");
        attendance.setCheckIn(LocalTime.now());
        return attendanceRepository.save(attendance);
    }

    private Attendance doCheckOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findTopByEmployeeIdAndWorkDateAndCheckOutIsNullOrderByCheckInDesc(
            employeeId, today
        ).orElseThrow(() -> new InvalidStateException("考勤", "无签到记录", "已签到"));
        
        LocalTime now = LocalTime.now();
        if (now.isBefore(attendance.getCheckIn())) {
            throw new InvalidStateException("考勤", "签退时间早于签到时间", "签退时间晚于签到时间");
        }
        
        attendance.setCheckOut(now);
        return attendanceRepository.save(attendance);
    }

    private void ensureSimilarity(VerificationResult result) {
        if (result.similarity() < FACE_SIMILARITY_THRESHOLD) {
            throw new FaceVerificationFailedException(result.similarity(), FACE_SIMILARITY_THRESHOLD);
        }
    }

    public record VerificationResult(
        boolean matched, 
        double similarity, 
        String algorithm, 
        double distance, 
        double threshold
    ) {}
}
