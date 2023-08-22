package me.SingKwan.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.SingKwan.common.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @projectName: oa-parent
 * @package: me.SingKwan.common.utils
 * @className: ResponseUtil
 * @author: SingKwan
 * @description: TODO
 * @date: 2023/7/14 19:59
 * @version: 1.0
 */
public class ResponseUtil {
    public static void out(HttpServletResponse response, Result r) {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            mapper.writeValue(response.getWriter(), r);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
