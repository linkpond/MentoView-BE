package ce2team1.mentoview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MentoviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentoviewApplication.class, args);
    }

}
