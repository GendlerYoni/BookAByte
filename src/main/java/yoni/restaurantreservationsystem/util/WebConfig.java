/**
 * Web MVC configuration class for serving uploaded restaurant images.
 *
 * This configuration maps requests to /restImg/**
 * to the local static restaurant images directory in the project.
 */
package yoni.restaurantreservationsystem.util;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registers a custom resource handler for restaurant images.
     *
     * @param registry the Spring resource handler registry used to define static resource mappings
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectDir = System.getProperty("user.dir");
        String staticPath = Paths.get(projectDir, "src/main/resources/static/restImg/").toUri().toString();

        registry.addResourceHandler("/restImg/**").addResourceLocations(staticPath)
                .setCachePeriod(0);
    }
}
