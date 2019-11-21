package com.starix.scorequery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tobu
 * @date 2019-11-19 22:16
 */
public class RegexTest {

    public static void main(String[] args) {
        String html = "<script language='javascript' defer>alert('验证码不正确！！');document.getElementById('TextBox2').focus();</script>";
        String regex = "alert\\('(.*?)'\\);";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(html);
        while (m.find()){
            String errInfo = m.group(1);
            System.out.println(errInfo);
        }
    }

}
