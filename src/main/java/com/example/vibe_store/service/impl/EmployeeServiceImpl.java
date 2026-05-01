package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.employee.*;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.exception.AlreadyExistsException;
import com.example.vibe_store.service.EmployeeService;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.entity.employee.Position;
import com.example.vibe_store.exception.ResourceNotFoundException;
import com.example.vibe_store.repository.EmployeeRepository;
import com.example.vibe_store.repository.EmployeeWorkHistoryRepository;
import com.example.vibe_store.repository.PositionRepository;
import com.example.vibe_store.repository.StoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkHistoryRepository employeeWorkHistoryRepository;
    private final StoreRepository storeRepository;
    private final PositionRepository positionRepository;
    private final ModelMapper modelMapper;

    @Override
    public PositionResponseDTO createPosition(CreatePositionRequestDTO requestDto) {
        if (positionRepository.existsByPositionName(requestDto.getPositionName())) {
            log.warn("Position name {} exists", requestDto.getPositionName());
            throw new IllegalArgumentException("A position with this name already exists.");
        }

        Position position = modelMapper.map(requestDto, Position.class);
        position = positionRepository.save(position);
        log.info("Position created: {}", position.getPositionName());
        return getPositionById(position.getId());
    }

    @Override
    @Transactional
    public AllEmployeeDetailsResponseDTO hireEmployee(HireEmployeeRequestDTO requestDto) {

        if (employeeRepository.existsByEmail(requestDto.getEmail())) {
            log.warn("Email {} exists", requestDto.getEmail());
            throw new IllegalArgumentException("An employee with this email already exists!");
        }

        Store store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + requestDto.getStoreId()));

        Position position = positionRepository.findById(requestDto.getPositionId())
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with ID: " + requestDto.getPositionId()));

        Employee employee = modelMapper.map(requestDto, Employee.class);
        employeeRepository.save(employee);

        EmployeeWorkHistory workHistory = new EmployeeWorkHistory();
        workHistory.setEmployee(employee);
        workHistory.setStore(store);
        workHistory.setPosition(position);
        workHistory.setSalary(requestDto.getSalary());
        workHistory.setIsActive(true);

        employeeWorkHistoryRepository.saveAndFlush(workHistory);

        log.info("Employee hired: {} {}, Position: {}, Store: {}, Salary: {}",
                employee.getFirstName(), employee.getLastName(),
                position.getPositionName(), store.getStoreName(), workHistory.getSalary());

        return getEmployeeById(employee.getId());
    }

    @Override
    @Transactional
    public AllEmployeeDetailsResponseDTO changeJobDetails(ChangeJobDetailsRequestDTO requestDto) {
        EmployeeWorkHistory oldWorkHistory = employeeWorkHistoryRepository
                .findByEmployeeIdAndIsActiveTrue(requestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active work history not found for employee: " + requestDto.getEmployeeId()));

        Integer targetStoreId = requestDto.getTargetStoreId() != null
                ? requestDto.getTargetStoreId()
                : oldWorkHistory.getStore().getId();

        Integer targetPositionId = requestDto.getTargetPositionId() != null
                ? requestDto.getTargetPositionId()
                : oldWorkHistory.getPosition().getId();

        BigDecimal newSalary = requestDto.getNewSalary() != null
                ? requestDto.getNewSalary()
                : oldWorkHistory.getSalary();

        boolean isStoreChanged = !oldWorkHistory.getStore().getId().equals(targetStoreId);
        boolean isPositionChanged = !oldWorkHistory.getPosition().getId().equals(targetPositionId);
        boolean isSalaryChanged = oldWorkHistory.getSalary().compareTo(newSalary) != 0;

        if (!isStoreChanged && !isPositionChanged && !isSalaryChanged) {
            log.warn("No changes detected for employee ID {}. Store, position, and salary are the same.", requestDto.getEmployeeId());
            throw new IllegalArgumentException("No changes detected! Employee already has these details.");
        }

        oldWorkHistory.setEndDate(LocalDateTime.now());
        oldWorkHistory.setIsActive(false);
        employeeWorkHistoryRepository.save(oldWorkHistory);

        Store newStore = isStoreChanged
                ? storeRepository.findById(targetStoreId)
                .orElseThrow(() -> new ResourceNotFoundException("New store not found"))
                : oldWorkHistory.getStore();

        Position newPosition = isPositionChanged
                ? positionRepository.findById(targetPositionId)
                .orElseThrow(() -> new ResourceNotFoundException("New position not found"))
                : oldWorkHistory.getPosition();

        EmployeeWorkHistory newWorkHistory = new EmployeeWorkHistory();
        newWorkHistory.setEmployee(oldWorkHistory.getEmployee());
        newWorkHistory.setStore(newStore);
        newWorkHistory.setPosition(newPosition);
        newWorkHistory.setSalary(newSalary);
        newWorkHistory.setIsActive(true);

        employeeWorkHistoryRepository.save(newWorkHistory);
        log.info("Employee {} has changed: {}", oldWorkHistory.getEmployee().getId(), newWorkHistory.getEmployee().getId());
        return getAllEmployeeDetailsResponseDto(oldWorkHistory.getEmployee(), newWorkHistory);
    }

    @Transactional
    @Override
    public EmployeeProfileResponseDTO updateEmployeeProfile(Integer employeeId, UpdateEmployeeProfileRequestDTO requestDto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        if (requestDto.getEmail() != null
                && !requestDto.getEmail().equals(employee.getEmail())
                && employeeRepository.existsByEmail(requestDto.getEmail())) {
            log.warn("This email {} is already in use.", requestDto.getEmail());;
            throw new AlreadyExistsException("This email is already in use: " + requestDto.getEmail());
        }

        modelMapper.map(requestDto, employee);

        Employee saved = employeeRepository.save(employee);

        log.info("Employee {} has changed profile information", employee.getId());

        return modelMapper.map(saved, EmployeeProfileResponseDTO.class);
    }

    @Override
    public AllEmployeeDetailsResponseDTO getEmployeeById(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        EmployeeWorkHistory activeWorkHistory = employeeWorkHistoryRepository
                .findByEmployeeIdAndIsActiveTrue(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Active work history not found for employee!"));

        return getAllEmployeeDetailsResponseDto(employee, activeWorkHistory);
    }

    @Override
    public PositionResponseDTO getPositionById(Integer positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with ID: " + positionId));

        PositionResponseDTO responseDTO = new PositionResponseDTO();
        responseDTO.setPositionId(position.getId());
        responseDTO.setPositionName(position.getPositionName());
        return responseDTO;
    }

    @Override
    public List<AllEmployeeDetailsResponseDTO> getAllEmployees() {
        return employeeWorkHistoryRepository.findAllByIsActiveTrue().stream()
                .map(wh -> getAllEmployeeDetailsResponseDto(wh.getEmployee(), wh))
                .toList();
    }

    @Override
    public List<PositionResponseDTO> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(position -> {
                    PositionResponseDTO dto = new PositionResponseDTO();
                    dto.setPositionId(position.getId());
                    dto.setPositionName(position.getPositionName());
                    return dto;
                })
                .toList();
    }

    //======= HELPER METHOD =======
    private AllEmployeeDetailsResponseDTO getAllEmployeeDetailsResponseDto(Employee employee, EmployeeWorkHistory activeWorkHistory) {
        AllEmployeeDetailsResponseDTO responseDto = new AllEmployeeDetailsResponseDTO();

        responseDto.setEmployeeId(employee.getId());
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
}