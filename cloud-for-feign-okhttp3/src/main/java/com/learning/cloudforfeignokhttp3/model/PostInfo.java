/**
 * Author:   claire Date:    2023/12/26 - 6:06 下午 Description: History:
 * <author>          <time>                   <version>          <desc>
 * claire          2023/12/26 - 6:06 下午          V1.0.0
 */

package com.learning.cloudforfeignokhttp3.model;

/**
 * 功能简述 
 * 〈〉
 *
 * @author claire
 * @date 2023/12/26 - 6:06 下午
 * @since 1.0.0
 */
public class PostInfo {
    private Integer id;
    private Integer userId;
    private String title;
    private String body;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(final Integer userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "PostInfo{" +
            "id=" + id +
            ", userId=" + userId +
            ", title='" + title + '\'' +
            ", body='" + body + '\'' +
            '}';
    }
}
