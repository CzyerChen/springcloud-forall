/**
 * Author:   claire
 * Date:    2023/12/4 - 5:10 下午
 * Description:
 * History:
 * <author>          <time>                   <version>          <desc>
 * claire          2023/12/4 - 5:10 下午          V1.0.0
 */
package com.learning.cloudforfeignokhttp3.controller;

import com.learning.cloudforfeignokhttp3.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 功能简述
 * 〈〉
 *
 * @author claire
 * @date 2023/12/4 - 5:10 下午
 * @since 1.0.0
 */
@RestController
@RequestMapping("/v1")
public class TestController {
    @Autowired
    private PostService postService;

    @GetMapping("/feign/post")
    public String feignPostRequest(){
        return postService.getPostById(1L).toString();
    }
}
