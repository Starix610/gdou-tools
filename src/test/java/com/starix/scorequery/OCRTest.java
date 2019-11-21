package com.starix.scorequery;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * @author Tobu
 * @date 2019-11-18 18:58
 */
public class OCRTest {

    //设置APPID/AK/SK
    public static final String APP_ID = "15406467";
    public static final String API_KEY = "yMPgIDIROfYN61plarotMVGy";
    public static final String SECRET_KEY = "NTZKGe4LU0IUAszwVyfM3QS6GooVKNEm";

    public static void main(String[] args) throws Exception {
        // 初始化一个AipOcr
        AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);



        // 调用接口
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("language_type", "CHN_ENG");
        options.put("detect_direction", "true");
        options.put("detect_language", "true");
        options.put("probability", "true");


        // 参数为本地路径
        String image = "websocket.png";
        JSONObject res = client.basicGeneral(image, options);
        System.out.println(res.toString(2));

    }

}
