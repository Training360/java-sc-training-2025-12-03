package employees;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
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
        var text = command.name();
        var antySamy = new AntiSamy();

        try (var file = AntiSamy.class.getResourceAsStream("/antisamy-tinymce.xml")) {
            var policy = Policy.getInstance(file);
            var result = antySamy.scan(text, policy);
            var cleanedCommand = new EmployeeModel(
                    command.id(),
                    result.getCleanHTML()
            );

            employeesService.createEmployee(cleanedCommand);
            return new ModelAndView("redirect:/");
        }
        catch (Exception e) {
            throw new IllegalStateException("Can not clean", e);
        }


    }

}