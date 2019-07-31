package cloud.feign.server;

import cloud.feign.config.FeignConfig;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by claire on 2019-07-30 - 18:27
 **/
@FeignClient(name = "fileserver",configuration = FileServer.MultipartSupportConfig.class,url = "http://127.0.0.1:7070/")
public interface FileServer {

    @RequestMapping(method = RequestMethod.POST, value = "/file/upload",
            produces = { MediaType.APPLICATION_JSON_UTF8_VALUE }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    void upload(@RequestPart MultipartFile file);

    @RequestMapping(value = "/file/uploadFile", method = RequestMethod.POST,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String handleFileUpload(@RequestPart(value = "file") MultipartFile file, @RequestParam(value = "name")String name);

    @RequestMapping(value = "/file/testpost",method = RequestMethod.POST)
    String handlePostNormal(@RequestParam(value = "name")String name);

    @RequestMapping(value = "/file/testget",method = RequestMethod.GET)
    Long handleGetNormal(@RequestParam(value = "name")String name);

    class MultipartSupportConfig {
        @Bean
        public Encoder feignFormEncoder() {
            return new SpringFormEncoder();
        }
    }

}
