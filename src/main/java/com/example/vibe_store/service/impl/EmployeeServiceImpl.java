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
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    public PositionResponseDTO createPosition(CreatePositionRequestDTO requestDto) {
        if (positionRepository.existsByPositionName(requestDto.getPositionName())) {
            throw new IllegalArgumentException("Bu adda vəzifə artıq mövcuddur.");
        }
            Position position = modelMapper.map(requestDto, Position.class);
            position = positionRepository.save(position);
            return getPositionById(position.getId());
    }

    @Override
    @Transactional
    public AllEmployeeDetailsResponseDTO hireEmployee(HireEmployeeRequestDTO requestDto) {

        if (employeeRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("Bu e-mail ünvanı ilə artıq işçi mövcuddur!");
        }

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

        employeeWorkHistoryRepository.saveAndFlush(workHistory);

        return getEmployeeById(employee.getId());
    }

    @Override
    @Transactional
    public void changeJobDetails(ChangeJobDetailsRequestDTO requestDto) {
        EmployeeWorkHistory oldWorkHistory = employeeWorkHistoryRepository.findByEmployeeIdAndIsActiveTrue(requestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("İşçinin aktiv fəaliyyəti tapılmadı: " +  requestDto.getEmployeeId()));

        Integer targetStoreId = requestDto.getTargetStoreId() != null ? requestDto.getTargetStoreId() : oldWorkHistory.getStore().getId();
        Integer targetPositionId = requestDto.getTargetPositionId() != null ? requestDto.getTargetPositionId() : oldWorkHistory.getPosition().getId();
        BigDecimal newSalary = requestDto.getNewSalary() != null ? requestDto.getNewSalary() : oldWorkHistory.getSalary();

        boolean isStoreChanged = !oldWorkHistory.getStore().getId().equals(targetStoreId);
        boolean isPositionChanged = !oldWorkHistory.getPosition().getId().equals(targetPositionId);
        boolean isSalaryChanged = oldWorkHistory.getSalary().compareTo(newSalary) != 0;

        if (!isStoreChanged && !isPositionChanged && !isSalaryChanged) {
            throw new IllegalArgumentException("Dəyişiklik yoxdur! İşçi onsuz da qeyd edilən şərtlərlə işləyir.");
        }

        oldWorkHistory.setEndDate(LocalDateTime.now());
        oldWorkHistory.setIsActive(false);
        employeeWorkHistoryRepository.save(oldWorkHistory);

        Store newStore = isStoreChanged ?
                storeRepository.findById(targetStoreId).orElseThrow(() -> new ResourceNotFoundException("Yeni mağaza tapılmadı"))
                : oldWorkHistory.getStore();

        Position newPosition = isPositionChanged ?
                positionRepository.findById(targetPositionId).orElseThrow(() -> new ResourceNotFoundException("Yeni vəzifə tapılmadı"))
                : oldWorkHistory.getPosition();

        EmployeeWorkHistory newWorkHistory = new EmployeeWorkHistory();
        newWorkHistory.setEmployee(oldWorkHistory.getEmployee());
        newWorkHistory.setStore(newStore);
        newWorkHistory.setPosition(newPosition);
        newWorkHistory.setSalary(newSalary);
        newWorkHistory.setIsActive(true);

        employeeWorkHistoryRepository.save(newWorkHistory);
    }

    @Transactional
    @Override
    public EmployeeProfileResponseDTO updateEmployeeProfile(Integer employeeId, UpdateEmployeeProfileRequestDTO requestDto) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("İşçi tapılmadı: " + employeeId));

        if (requestDto.getEmail() != null && !requestDto.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(requestDto.getEmail())) {
                throw new AlreadyExistsException("Bu e-mail artıq istifadə olunur: " + requestDto.getEmail());
            }
        }
        // Null gələnlər köhnə dəyəri saxlayacaq, model mapper hell edir
        modelMapper.map(requestDto, employee);

        Employee saved = employeeRepository.save(employee);

        return modelMapper.map(saved, EmployeeProfileResponseDTO.class);
    }

    public AllEmployeeDetailsResponseDTO getEmployeeById(Integer employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Verilen id ile isci tapilmadi: " + employeeId));

        EmployeeWorkHistory activeWorkHistory = employeeWorkHistoryRepository.findByEmployeeIdAndIsActiveTrue(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("İşçinin aktiv fəaliyyət tarixçəsi tapılmadı!"));

        return getAllEmployeeDetailsResponseDto(employee, activeWorkHistory);
    }

    @Override
    public PositionResponseDTO getPositionById(Integer positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new ResourceNotFoundException("Axtarilan id li posisya tapilmadi: " + positionId));
        PositionResponseDTO responseDTO = new PositionResponseDTO();
        responseDTO.setPositionId(position.getId());
        responseDTO.setPositionName(position.getPositionName());
        return responseDTO;
    }

    //======= HELPER METHOD =======
    private AllEmployeeDetailsResponseDTO getAllEmployeeDetailsResponseDto(Employee employee, EmployeeWorkHistory activeWorkHistory) {
        AllEmployeeDetailsResponseDTO responseDto = new AllEmployeeDetailsResponseDTO();

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
