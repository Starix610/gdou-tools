package com.starix.scorequery;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
        String path = "F:\\IdeaProjects\\project\\gdou-score-query\\src\\test\\java\\com\\starix\\scorequery\\test.txt";

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path),"GBK"));
        String line = null;
        StringBuffer html2 = new StringBuffer();
        while ((line = br.readLine())!=null){
            html2.append(line);
        }
        System.out.println(html2.toString().contains("您已完成评价"));
    }

}
