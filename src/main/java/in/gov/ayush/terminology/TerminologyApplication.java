package in.gov.ayush.terminology;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TerminologyApplication {
    public static void main(String[] args) {
        SpringApplication.run(TerminologyApplication.class, args);
    }
}
