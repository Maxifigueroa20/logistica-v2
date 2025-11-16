package ar.edu.unju.fi.logistica;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebRedirectConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirige la raíz a la interfaz Swagger
        registry.addRedirectViewController("/", "/swagger-ui/index.html");
    }
}
