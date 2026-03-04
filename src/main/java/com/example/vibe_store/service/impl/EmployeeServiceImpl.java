package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.employee.CreateEmployeeRequestDto;
import com.example.vibe_store.dto.employee.EmployeeResponseDto;
import com.example.vibe_store.dto.employee.TransferEmployeeRequestDto;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.service.EmployeeService;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.entity.employee.Position;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.EmployeeRepository;
import com.example.vibe_store.repository.EmployeeWorkHistoryRepository;
import com.example.vibe_store.repository.PositionRepository;
import com.example.vibe_store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkHistoryRepository employeeWorkHistoryRepository;
    private final StoreRepository storeRepository;
    private final PositionRepository positionRepository;
    private final ModelMapper modelMapper;
    
    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(CreateEmployeeRequestDto requestDto) {

        Store store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Verilen id ile magaza tapilmadi: " + requestDto.getStoreId()));
        
        Position position = positionRepository.findById(requestDto.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Verilen id ile pozisya tapilmadi: " + requestDto.getPositionId()));
        
        Employee employee = modelMapper.map(requestDto, Employee.class);
        employeeRepository.save(employee);

        EmployeeWorkHistory workHistory = new EmployeeWorkHistory();
        workHistory.setEmployee(employee);
        workHistory.setStore(store);
        workHistory.setPosition(position);
        workHistory.setSalary(requestDto.getSalary());
        workHistory.setIsActive(true);

        employeeWorkHistoryRepository.save(workHistory);

        return getEmployeeById(employee.getId());
    }

    private EmployeeResponseDto getEmployeeById(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Verilen id ile isci tapilmadi: " + employeeId));

        EmployeeWorkHistory activeWorkHistory = employeeWorkHistoryRepository.findByEmployeeIdAndIsActiveTrue(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("İşçinin aktiv fəaliyyət tarixçəsi tapılmadı!"));

        EmployeeResponseDto responseDto = new EmployeeResponseDto();

        responseDto.setFirstName(employee.getFirstName());
        responseDto.setLastName(employee.getLastName());
        responseDto.setHireDate(activeWorkHistory.getStartDate());
        responseDto.setTerminationDate(activeWorkHistory.getEndDate());
        responseDto.setAge(employee.getAge());
        responseDto.setEmail(employee.getEmail());
        responseDto.setCurrentSalary(activeWorkHistory.getSalary());
        responseDto.setCurrentStoreName(activeWorkHistory.getStore().getStoreName());
        responseDto.setCurrentPositionName(activeWorkHistory.getPosition().getPositionName());
        return responseDto;
    }

    @Override
    @Transactional
    public void transferEmployee(TransferEmployeeRequestDto requestDto) {
        EmployeeWorkHistory oldWorkHistory = employeeWorkHistoryRepository.findByEmployeeIdAndIsActiveTrue(requestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Transfer üçün verilən id-li işçinin aktiv fəaliyyəti tapılmadı: " +  requestDto.getEmployeeId()));

        boolean isStoreChanged = !oldWorkHistory.getStore().getId().equals(requestDto.getTargetStoreId());
        boolean isPositionChanged = !oldWorkHistory.getPosition().getId().equals(requestDto.getTargetPositionId());
        boolean isSalaryChanged = oldWorkHistory.getSalary().compareTo(requestDto.getNewSalary()) != 0;

        if (!isStoreChanged && !isPositionChanged && !isSalaryChanged) {
            throw new IllegalArgumentException("Dəyişiklik yoxdur! İşçi onsuz da eyni vəzifədə, eyni mağazada və eyni maaşla işləyir.");
        }

        oldWorkHistory.setEndDate(LocalDateTime.now());
        oldWorkHistory.setIsActive(false);
        employeeWorkHistoryRepository.save(oldWorkHistory);

        Employee employee = oldWorkHistory.getEmployee();
        Store newStore = oldWorkHistory.getStore();
        Position newPosition = oldWorkHistory.getPosition();

        EmployeeWorkHistory newWorkHistory = new EmployeeWorkHistory();
        newWorkHistory.setEmployee(employee);
        newWorkHistory.setStore(newStore);
        newWorkHistory.setPosition(newPosition);
        newWorkHistory.setSalary(requestDto.getNewSalary());
        newWorkHistory.setIsActive(true);

        employeeWorkHistoryRepository.save(newWorkHistory);
    }
}
