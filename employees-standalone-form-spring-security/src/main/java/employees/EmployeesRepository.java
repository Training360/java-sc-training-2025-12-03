package employees;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeesRepository extends JpaRepository<Employee, Long> {

    @Query("select new employees.EmployeeModel(e.id, e.name) from Employee e")
//@Query("select new employees.EmployeeModel(e.id, e.name) from Employee e where e.org = ?#{authentication.org}")
    List<EmployeeModel> findAllResources();
}
