package hr.srsveze.sredisnji_registar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Ova anotacija je KLJUČNA!
public class SredisnjiRegistarApplication {

    public static void main(String[] args) { // Standardna main metoda
        SpringApplication.run(SredisnjiRegistarApplication.class, args);
        System.out.println("******************************************************************");
        System.out.println("* Aplikacija Središnji Registar Stanovništva je pokrenuta!       *");
        // ... (ostatak poruka koje sam dodao)
        System.out.println("******************************************************************");
    }
}