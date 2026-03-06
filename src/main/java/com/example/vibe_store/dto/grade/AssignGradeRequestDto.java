package com.example.vibe_store.dto.grade;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AssignGradeRequestDto {

    @NotNull(message = "Grade ID mütləq göndərilməlidir")
    private Integer gradeId; //old garde ucun tarixi de servisde teyin et. ve false et
                             //eger aktiv olan versa xeta versin

    private Integer storeId;

    private List<Integer> employeeIds;

    @NotNull(message = "Başlanğıc tarixi qeyd olunmalıdır")
    private LocalDateTime startDate;

    @NotNull(message = "Son tarixi qeyd olunmalıdır")
    private LocalDateTime endDate;
}
