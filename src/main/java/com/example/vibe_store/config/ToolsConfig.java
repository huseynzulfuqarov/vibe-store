package com.example.vibe_store.config;

import com.example.vibe_store.entity.Store;
import com.example.vibe_store.entity.employee.Employee;
import com.example.vibe_store.entity.employee.EmployeeWorkHistory;
import com.example.vibe_store.repository.EmployeeRepository;
import com.example.vibe_store.repository.EmployeeWorkHistoryRepository;
import com.example.vibe_store.repository.SaleRepository;
import com.example.vibe_store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@Service
@RequiredArgsConstructor
public class ToolsConfig {

    private final EmployeeRepository employeeRepository;
    private final EmployeeWorkHistoryRepository workHistoryRepository;
    private final StoreRepository storeRepository;
    private final SaleRepository saleRepository;


    public record EmployeeRequest(
        @JsonProperty(required = true) 
        @JsonPropertyDescription("Axtarılan işçinin tam və ya yalançı adı") 
        String employeeName
    ) {}

    @Tool(description = "Verilən işçi adına görə cari maaşını, vəzifəsini və mağazasını məlumat bazasından gətirir")
    public String getEmployeeSalary(EmployeeRequest request) {
        String employeeName = request.employeeName();
        List<Employee> allEmployees = employeeRepository.findAll();
        Optional<Employee> employeeOpt = allEmployees.stream()
                .filter(e -> e.getFirstName().equalsIgnoreCase(employeeName)
                        || (e.getFirstName() + " " + e.getLastName()).equalsIgnoreCase(employeeName))
                .findFirst();

        if (employeeOpt.isEmpty()) {
            return "İşçi tapılmadı: " + employeeName;
        }

        Employee employee = employeeOpt.get();
        Optional<EmployeeWorkHistory> activeHistory =
                workHistoryRepository.findByEmployeeIdAndIsActiveTrue(employee.getId());

        if (activeHistory.isEmpty()) {
            return employee.getFirstName() + " " + employee.getLastName() + " — aktiv iş tarixçəsi yoxdur";
        }

        EmployeeWorkHistory h = activeHistory.get();
        return String.format("%s %s — Maaş: %s AZN, Vəzifə: %s, Mağaza: %s",
                employee.getFirstName(), employee.getLastName(),
                h.getSalary().toString(),
                h.getPosition().getPositionName(),
                h.getStore().getStoreName());
    }

    public record StoreRequest(
        @JsonProperty(required = true) 
        @JsonPropertyDescription("Axtarılan mağazanın adı") 
        String storeName
    ) {}

    @Tool(description = "Verilən mağaza adına görə mağaza haqqında məlumat və işçi sayını gətirir")
    public String getStoreInfo(StoreRequest request) {
        String storeName = request.storeName();
        List<Store> allStores = storeRepository.findAll();
        Optional<Store> storeOpt = allStores.stream()
                .filter(s -> s.getStoreName().toLowerCase().contains(storeName.toLowerCase()))
                .findFirst();

        if (storeOpt.isEmpty()) {
            return "Mağaza tapılmadı: " + storeName;
        }

        Store store = storeOpt.get();
        long employeeCount = workHistoryRepository.findAllActiveByStoreId(store.getId()).size();

        return String.format("Mağaza: %s, Ünvan: %s, Aktiv işçi sayı: %d",
                store.getStoreName(),
                store.getStoreAddress() != null ? store.getStoreAddress() : "Ünvan yoxdur",
                employeeCount);
    }

    @Tool(description = "Verilən mağaza adına görə bu ayki ümumi satış məbləğini gətirir")
    public String getStoreSales(StoreRequest request) {
        String storeName = request.storeName();
        List<Store> allStores = storeRepository.findAll();
        Optional<Store> storeOpt = allStores.stream()
                .filter(s -> s.getStoreName().toLowerCase().contains(storeName.toLowerCase()))
                .findFirst();

        if (storeOpt.isEmpty()) {
            return "Mağaza tapılmadı: " + storeName;
        }

        Store store = storeOpt.get();
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        BigDecimal totalSales = saleRepository.getTotalSalesByStoreAndDate(
                store.getId(), monthStart, monthEnd);

        return String.format("Mağaza: %s, %s %d satışları: %s AZN",
                store.getStoreName(),
                currentMonth.getMonth().name(),
                currentMonth.getYear(),
                totalSales.toString());
    }
}
