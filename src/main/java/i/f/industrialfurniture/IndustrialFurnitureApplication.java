package i.f.industrialfurniture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IndustrialFurnitureApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndustrialFurnitureApplication.class, args);
    }

}
