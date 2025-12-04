package employees;

import jakarta.validation.constraints.NotBlank;

public record EmployeeModel(Long id, @NotBlank String name) {

}
