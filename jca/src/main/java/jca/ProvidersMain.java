package jca;

import java.security.Security;

public class ProvidersMain {

    public static void main(String[] args) {
        for (var provider: Security.getProviders()) {
            System.out.println(provider.getName());
            for (var service: provider.getServices()) {
                System.out.println("- " + service.getType() + " " + service.getAlgorithm());
            }
        }
    }
}
