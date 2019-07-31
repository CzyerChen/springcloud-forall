package cloud.feign.controller;

import cloud.feign.server.FileServer;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by claire on 2019-07-30 - 18:40
 **/
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private FileServer fileServer;

    @PostMapping
    public void test(@RequestParam("file")MultipartFile file) {
        String handleFileUpload = fileServer.handleFileUpload(file,"claire");
        System.out.println(handleFileUpload);
    }


    @GetMapping("testpost")
    public String test1(){
        return fileServer.handlePostNormal("claire");
    }

    @GetMapping("/testget")
    public Long test2(){
        return fileServer.handleGetNormal("claire");
    }
}
