package com.example.vibe_store.service.impl;

import com.example.vibe_store.dto.employee.*;
import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.User;
import com.example.vibe_store.enums.Role;
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
import com.example.vibe_store.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.vibe_store.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkHistoryRepository employeeWorkHistoryRepository;
    private final StoreRepository storeRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    @Transactional
    @Override
    public PositionResponseDTO createPosition(CreatePositionRequestDTO requestDto) {
        if (positionRepository.existsByPositionName(requestDto.positionName())) {
            log.warn("Position name {} exists", requestDto.positionName());
            throw new IllegalArgumentException("A position with this name already exists.");
        }

        Position position = employeeMapper.toEntity(requestDto);
        position = positionRepository.save(position);
        log.info("Position created: {}", position.getPositionName());
        return getPositionById(position.getId());
    }

    @Override
    @Transactional
    public HireEmployeeResponseDTO hireEmployee(HireEmployeeRequestDTO requestDto) {

        if (employeeRepository.existsByEmail(requestDto.email())) {
            log.warn("Email {} exists", requestDto.email());
            throw new IllegalArgumentException("An employee with this email already exists!");
        }

        Store store = storeRepository.findById(requestDto.storeId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + requestDto.storeId()));

        Position position = positionRepository.findById(requestDto.positionId())
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with ID: " + requestDto.positionId()));

        Employee employee = employeeMapper.toEntity(requestDto);
        employeeRepository.save(employee);

        EmployeeWorkHistory workHistory = new EmployeeWorkHistory();
        workHistory.setEmployee(employee);
        workHistory.setStore(store);
        workHistory.setPosition(position);
        workHistory.setSalary(requestDto.salary());
        workHistory.setIsActive(true);

        employeeWorkHistoryRepository.saveAndFlush(workHistory);

        String username = generateUsername(requestDto.email(), requestDto.firstName(), requestDto.lastName());
        String tempPassword = generateTempPassword();

        User user = new User();
        user.setUsername(username);
        user.setEmail(requestDto.email());
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setRole(Role.EMPLOYEE);
        user.setEmployee(employee);
        userRepository.save(user);

        log.info("Employee hired: {} {}, Position: {}, Store: {}, Salary: {}, Username: {}",
                employee.getFirstName(), employee.getLastName(),
                position.getPositionName(), store.getStoreName(), workHistory.getSalary(), username);

        HireEmployeeResponseDTO response = new HireEmployeeResponseDTO(
                getEmployeeById(employee.getId()),
                username,
                tempPassword
        );

        return response;
    }

    @Override
    @Transactional
    public AllEmployeeDetailsResponseDTO changeJobDetails(ChangeJobDetailsRequestDTO requestDto) {
        EmployeeWorkHistory oldWorkHistory = employeeWorkHistoryRepository
                .findByEmployeeIdAndIsActiveTrue(requestDto.employeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Active work history not found for employee: " + requestDto.employeeId()));

        Integer targetStoreId = requestDto.targetStoreId() != null
                ? requestDto.targetStoreId()
                : oldWorkHistory.getStore().getId();

        Integer targetPositionId = requestDto.targetPositionId() != null
                ? requestDto.targetPositionId()
                : oldWorkHistory.getPosition().getId();

        BigDecimal newSalary = requestDto.newSalary() != null
                ? requestDto.newSalary()
                : oldWorkHistory.getSalary();

        boolean isStoreChanged = !oldWorkHistory.getStore().getId().equals(targetStoreId);
        boolean isPositionChanged = !oldWorkHistory.getPosition().getId().equals(targetPositionId);
        boolean isSalaryChanged = oldWorkHistory.getSalary().compareTo(newSalary) != 0;

        if (!isStoreChanged && !isPositionChanged && !isSalaryChanged) {
            log.warn("No changes detected for employee ID {}. Store, position, and salary are the same.", requestDto.employeeId());
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
        return employeeMapper.toAllDetailsResponse(oldWorkHistory.getEmployee(), newWorkHistory);
    }

    @Transactional
    @Override
    public EmployeeProfileResponseDTO updateEmployeeProfile(Integer employeeId, UpdateEmployeeProfileRequestDTO requestDto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + employeeId));

        if (requestDto.email() != null
                && !requestDto.email().equals(employee.getEmail())
                && employeeRepository.existsByEmail(requestDto.email())) {
            log.warn("This email {} is already in use.", requestDto.email());;
            throw new AlreadyExistsException("This email is already in use: " + requestDto.email());
        }

        employeeMapper.updateEntityFromRequest(requestDto, employee);

        Employee saved = employeeRepository.save(employee);

        log.info("Employee {} has changed profile information", employee.getId());

        return employeeMapper.toProfileResponse(saved);
    }

    @Override
    public AllEmployeeDetailsResponseDTO getEmployeeById(Integer employeeId) {
        EmployeeWorkHistory activeWorkHistory = employeeWorkHistoryRepository
                .findByEmployeeIdAndIsActiveTrueWithDetails(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Active work history not found for employee: " + employeeId));

        return employeeMapper.toAllDetailsResponse(activeWorkHistory.getEmployee(), activeWorkHistory);
    }

    @Override
    public Page<AllEmployeeDetailsResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeWorkHistoryRepository.findAllActiveWithDetails(pageable)
                .map(wh -> employeeMapper.toAllDetailsResponse(wh.getEmployee(), wh));
    }

    @Override
    public PositionResponseDTO getPositionById(Integer positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with ID: " + positionId));
        return employeeMapper.toResponse(position);
    }

    @Override
    public Page<PositionResponseDTO> getAllPositions(Pageable pageable) {
        return positionRepository.findAll(pageable)
                .map(employeeMapper::toResponse);
    }

    //======= HELPER METHOD =======
    private String generateUsername(String email, String firstName, String lastName) {
        String baseUsername = email.split("@")[0].toLowerCase();

        if (!userRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }

        String altUsername = (firstName + "." + lastName).toLowerCase().replaceAll("\\s+", "");
        if (!userRepository.existsByUsername(altUsername)) {
            return altUsername;
        }

        int counter = 1;
        while (userRepository.existsByUsername(altUsername + counter)) {
            counter++;
        }
        return altUsername + counter;
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
