package com.starix.gdou;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tobu
 * @date 2019-11-19 22:16
 */
public class RegexTest {

    public static void main(String[] args) throws IOException {
        String html = "<script language='javascript' defer>alert('验证码不正确！！');document.getElementById('TextBox2').focus();</script>";
        String regex = "alert\\('(.*?)'\\);";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(html);
        while (m.find()){
            String errInfo = m.group(1);
            System.out.println(errInfo);
        }

        System.out.println("=============================");
        String path = "F:\\IdeaProjects\\project\\gdou-jw-tools\\src\\test\\java\\com\\starix\\gdou\\test.txt";

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path),"GBK"));
        String line = null;
        StringBuffer html2 = new StringBuffer();
        while ((line = br.readLine())!=null){
            html2.append(line);
        }
        System.out.println(html2.toString().contains("您已完成评价"));

        String r = "^[0-9]+\\.[0-9]{6}$";
        pattern = Pattern.compile(r);
        Matcher matcher = pattern.matcher("111.123451");
        System.out.println("--->"+matcher.matches());
    }

}
