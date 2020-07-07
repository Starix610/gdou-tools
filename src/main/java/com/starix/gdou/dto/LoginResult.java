package com.starix.gdou.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.Header;

/**
 * v1版本：
 * 封装登录成功之后的cookie值等关键数据，用于维持登录（虽然HttpClient会自动管理cookie，
 * 但是这里的cookie是来自python中识别验证码时获得的cookie而非来自HttpClient请求获得，所以需要手动保存管理）
 *
 * @author Starix
 * @date 2019-11-20 20:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResult {

    private Header cookie;

    private String homePageHtml;

    private String refererURL;

    private String xh;

}
