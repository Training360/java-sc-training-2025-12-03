package employees;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EmployeesController {

    private final EmployeesService employeesService;

    @GetMapping("/")
    public ModelAndView listEmployees() {
        var model = new HashMap<String, Object>();
        model.put("employees", employeesService.listEmployees());
        model.put("command", new Employee());

        return new ModelAndView("employees", model);
    }

    @GetMapping("/employees")
    public ModelAndView findEmployeeById(@RequestParam long id) {
        var employee = employeesService.findEmployeeById(id);
        return new ModelAndView("employee", "employee", employee);
    }


    @GetMapping("/create-employee")
    public ModelAndView createEmployee() {
        var model = Map.of(
                "command", new Employee()
        );
        return new ModelAndView("create-employee", model);
    }

    @PostMapping("/create-employee")
    public ModelAndView createEmployeePost(@ModelAttribute EmployeeModel command) {
        employeesService.createEmployee(command);
        return new ModelAndView("redirect:/");
    }

}