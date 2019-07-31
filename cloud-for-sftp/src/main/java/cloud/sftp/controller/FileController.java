package cloud.sftp.controller;


import cloud.sftp.config.SftpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Created by claire on 2019-07-11 - 11:15
 **/
@Validated
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private SftpConfig sftpConfig;
    @Value("${file.upload.localPath}")
    private String localPath;


    @RequestMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE,method = RequestMethod.POST)
    public void uploadFile(@RequestPart MultipartFile applyFile/* @RequestParam("userId") String userId*/) throws IOException {
        String filePath = localPath+generateFileName(Long.valueOf(9));
        File fp = new File(filePath);
        if (!fp.exists()) {
            try {
                fp.mkdirs();// 目录不存在的情况下，创建目录。
            }catch (Exception e){
               e.printStackTrace();
            }
        }

        String newName = UUID.randomUUID().toString().replace("-","")+"."+applyFile.getOriginalFilename().split("\\.")[1];
        String fileName = filePath+newName;
        File file = new File(fileName);
        applyFile.transferTo(file);
    }

    private String generateFileName(Long userId){
        return "personal/"+userId+"/"+10+"/";
    }


    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String handleFileUpload(@RequestPart(value = "file") MultipartFile file,@RequestParam(value = "name")String name) {
        System.out.println(name);
        return file.getOriginalFilename();
    }


    @RequestMapping(value = "/testpost",method = RequestMethod.POST)
    public String handlePostNormal(@RequestParam(value = "name")String name){
        return name;
    }

    @GetMapping(value = "/testget")
    public Long handleGetNormal(@RequestParam(value = "name")String name){
        return (long) name.length();
    }


}
