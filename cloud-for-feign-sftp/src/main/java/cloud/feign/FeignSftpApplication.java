package cloud.feign;

import cloud.feign.config.FeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * Created by claire on 2019-07-30 - 18:26
 **/
@EnableFeignClients(basePackages = {"cloud.feign"})
@SpringBootApplication
@Import({FeignConfig.class})
public class FeignSftpApplication {

    public static  void main(String[] args){
        SpringApplication.run(FeignSftpApplication.class,args);
    }
}
