package jca;

import java.security.SecureRandom;
import java.util.Random;
import java.util.random.RandomGeneratorFactory;

public class RandomMain {

    public static void main(String[] args) throws Exception {
//        var random = new Random(); - Crypto célra nem használható
//        var random = new SecureRandom();
//        var random = SecureRandom.getInstance("Windows-PRNG");
        var random = SecureRandom.getInstanceStrong();
        System.out.println(random.nextInt());


        RandomGeneratorFactory.all()
                .map(fac -> fac.group() + ":" + fac.name()
                        + " {"
                        + (fac.isSplittable() ? " splittable" : "")
                        + (fac.isStreamable() ? " streamable" : "")
                        + (fac.isJumpable() ? " jumpable" : "")
                        + (fac.isArbitrarilyJumpable() ? " arbitrarily-jumpable" : "")
                        + (fac.isLeapable() ? " leapable" : "")
                        + (fac.isHardware() ? " hardware" : "")
                        + (fac.isStatistical() ? " statistical" : "")
                        + (fac.isStochastic() ? " stochastic" : "")
                        + " stateBits: " + fac.stateBits()
                        + " }"
                )
                .sorted().forEach(System.out::println);


    }
}
