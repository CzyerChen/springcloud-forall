package cloud.feign.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by claire on 2019-07-30 - 18:27
 **/
@Configuration
public class FeignConfig implements WebMvcConfigurer {
        @Bean
        public Encoder feignFormEncoder(ObjectFactory<HttpMessageConverters> messageConverters) {
            return new SpringFormEncoder(new SpringEncoder(messageConverters));
        }
}
