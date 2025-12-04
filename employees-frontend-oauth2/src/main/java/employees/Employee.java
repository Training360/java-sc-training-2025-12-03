package employees;

public record Employee(Long id, String name) {

    static Employee empty() {
        return new Employee(null, "");
    }
}
