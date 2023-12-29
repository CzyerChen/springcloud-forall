/**
 * Author:   claire Date:    2023/12/26 - 6:05 下午 Description: History:
 * <author>          <time>                   <version>          <desc>
 * claire          2023/12/26 - 6:05 下午          V1.0.0
 */

package com.learning.cloudforfeignokhttp3.service;

import com.learning.cloudforfeignokhttp3.model.PostInfo;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 功能简述 
 * 〈〉
 *
 * @author claire
 * @date 2023/12/26 - 6:05 下午
 * @since 1.0.0
 */
@FeignClient(value = "jplaceholder", url = "https://jsonplaceholder.typicode.com/")
public interface PostService {

    @RequestMapping(method = RequestMethod.GET, value = "/posts")
    List<PostInfo> getPosts();

    @RequestMapping(method = RequestMethod.GET, value = "/posts/{postId}", produces = "application/json")
    PostInfo getPostById(@PathVariable("postId") Long postId);

    @RequestMapping(method = RequestMethod.POST, value = "/posts", produces = "application/json")
    PostInfo savePost(@RequestBody PostInfo postInfo);

    @RequestMapping(method = RequestMethod.PATCH, value = "/posts", produces = "application/json")
    PostInfo updatePost(@RequestBody PostInfo postInfo);

    @RequestMapping(method = RequestMethod.DELETE, value = "/posts/{postId}", produces = "application/json")
    void deletePost(@PathVariable("postId") Long postId);
}
