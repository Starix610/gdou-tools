package com.starix.gdou;

import com.starix.gdou.utils.SslUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * @author Tobu
 * @date 2019-11-20 16:58
 */
public class PageTest {


    public static void main(String[] args) throws Exception {
        // String url = BASE_URL + ;
        SslUtils.ignoreSsl();
        // divs.get(i).getElementsByAttributeValueMatching(, )
        Document document = Jsoup.connect("https://blog.dairoot.cn/2019/02/20/zfxf-check-code/").get();
        // Elements element = document.select("div[class~=-left$]");
        Element element = document.getElementsMatchingText("正方教务系统 验证码识别").get(0);
        System.out.println(element);

    }




}
