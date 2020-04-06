package com.starix.gdou;

import com.starix.gdou.vo.ScoreVO;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tobu
 * @date 2019-11-18 21:22
 */
public class LoginTest {

    private static final String BASE_URL = "http://210.38.137.126:8016";

    public static void main(String[] args) throws Exception {

        //使用python自动获取并识别验证码
        Process process = Runtime.getRuntime().exec("python G:\\Python\\PycharmProjects\\gdou-jw-code-ocr\\jw_captcha_ocr.py");
        int status = process.waitFor();
        List<String> result = new ArrayList<>();
        if (status == 0) {
            InputStream in = process.getInputStream();
            InputStreamReader inputReader = new InputStreamReader(in);
            BufferedReader buffReader = new BufferedReader(inputReader);
            String line;
            //脚本输出结果的第一行为验证码识别结果，第二行为cookie值
            while ((line = buffReader.readLine())!=null){
                result.add(line);
                System.out.println("======================================================>"+line);
            }
            buffReader.close();
            inputReader.close();
            in.close();
        } else {
            System.out.println("python脚本执行出错");
            return;
        }



        HttpClient httpclient = HttpClientBuilder.create().build();


        //执行登录
        HttpPost loginPost = new HttpPost(BASE_URL + "/default2.aspx");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //__VIEWSTATE需要自己获取
        params.add(new BasicNameValuePair("__VIEWSTATE", "dDwxNTMxMDk5Mzc0Ozs+OBE730NQqeUlEYO76T3Qls4CiUo="));
        //学号
        params.add(new BasicNameValuePair("txtUserName", "201711621429"));
        //密码
        params.add(new BasicNameValuePair("TextBox2","19990325xzf"));
        //验证码
        params.add(new BasicNameValuePair("txtSecretCode", result.get(0)));
        //用户类型
        params.add(new BasicNameValuePair("RadioButtonList1", "学生"));
        //未知参数，但是必填
        params.add(new BasicNameValuePair("Button1", ""));
        //设置字符
        loginPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));

        //将python中获取验证码的Cookie设置到当前请求头中(携带同一个Cookie才能验证和进行后面的登录)
        Header cookie = new BasicHeader("Cookie", "ASP.NET_SessionId="+result.get(1));
        loginPost.setHeader(cookie);

        //发送登录请求
        HttpResponse loginResp = httpclient.execute(loginPost);



        // 如果状态是302，表示登录成功。
        // 因为这里登录成功后是一个跳转，但是HttpClient并不能跳转，需要手动获取重定向地址发送请求
        System.out.println(loginResp.getStatusLine().toString());
        if (loginResp.getStatusLine().getStatusCode() == 302) {
            System.out.println("登录成功");
            //获取跳转的URL
            String location = loginResp.getFirstHeader("Location").getValue();
            System.out.println("主页URL是："+BASE_URL + location);
            HttpGet homePageGet = new HttpGet(BASE_URL + location);
            //将python中获取验证码的Cookie设置到当前请求头中(携带同一个Cookie才能验证和进行后面的登录)
            homePageGet.setHeader(cookie);

            HttpResponse homePageResp = httpclient.execute(homePageGet);
            String homePageHtml = EntityUtils.toString(homePageResp.getEntity(), Consts.UTF_8);
            //查询成绩时候必须带上此时主页的URL作为Referer头信息，否则查成绩时会出现“Object to here”
            getScore(homePageHtml, cookie, BASE_URL + location);

        }else {
            System.out.println("登录失败");
            String html = EntityUtils.toString(loginResp.getEntity(), Consts.UTF_8);
            System.out.println(html);
            String regex = "alert\\('(.*?)'\\);";
            Pattern pattern = Pattern.compile(regex);
            Matcher m = pattern.matcher(html);
            String errInfo = null;
            while (m.find()){
                errInfo = m.group(1);
            }
            if (errInfo == null){
                errInfo = "未知错误";
            }
            System.out.println(errInfo);
        }



        //使用已保存的已经登录成功的cookie可以直接请求主页
        // HttpGet homePageGet = new HttpGet("http://210.38.137.126:8016/xs_main.aspx?xh=201711621427");
        // Header cookie = new BasicHeader("Cookie", "ASP.NET_SessionId=nzgqvxjtuomqclacdrkbourm");
        // homePageGet.setHeader(cookie);
        // HttpResponse homePageResp = httpclient.execute(homePageGet);
        // System.out.println(EntityUtils.toString(homePageResp.getEntity(), Consts.UTF_8));


    }


    public static void getScore(String homePageHtml, Header cookie, String refererURL) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();

        Document document = Jsoup.parse(homePageHtml);

        //查成绩页面URL
        String scoreURL = document.getElementsByAttributeValue("onclick", "GetMc('成绩查询');").get(0).attr("href");


        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //__VIEWSTATE可根据需要自己获取
        params.add(new BasicNameValuePair("__VIEWSTATE", "dDwxODI2NTc3MzMwO3Q8cDxsPHhoOz47bDwyMDE3MTE2Mj" +
                "E0Mjc7Pj47bDxpPDE+Oz47bDx0PDtsPGk8MT47aTwzPjtpPDU+O2k8Nz47aTw5PjtpPDExPjtpPDEzPjtpPDE2PjtpPDI2PjtpPDI" +
                "3PjtpPDI4PjtpPDM1PjtpPDM3PjtpPDM5PjtpPDQxPjtpPDQ1Pjs+O2w8dDxwPHA8bDxUZXh0Oz47bDzlrablj7fvvJoyMDE3MTE" +
                "2MjE0Mjc7Pj47Pjs7Pjt0PHA8cDxsPFRleHQ7PjtsPOWnk+WQje+8muWPsuaWh+adsDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w" +
                "85a2m6Zmi77ya5pWw5a2m5LiO6K6h566X5py65a2m6ZmiOz4+Oz47Oz47dDxwPHA8bDxUZXh0Oz47bDzkuJPkuJrvvJo7Pj47Pjs" +
                "7Pjt0PHA8cDxsPFRleHQ7PjtsPOiuoeeul+acuuenkeWtpuS4juaKgOacrzs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w86KGM5pS" +
                "/54+t77ya6K6h56eRMTE3NDs+Pjs+Ozs+O3Q8cDxwPGw8VGV4dDs+O2w8MjAxNzE2MjE7Pj47Pjs7Pjt0PHQ8cDxwPGw8RGF0YVR" +
                "leHRGaWVsZDtEYXRhVmFsdWVGaWVsZDs+O2w8WE47WE47Pj47Pjt0PGk8Mz47QDxcZTsyMDE4LTIwMTk7MjAxNy0yMDE4Oz47QDx" +
                "cZTsyMDE4LTIwMTk7MjAxNy0yMDE4Oz4+Oz47Oz47dDxwPDtwPGw8b25jbGljazs+O2w8d2luZG93LnByaW50KClcOzs+Pj47Oz47" +
                "dDxwPDtwPGw8b25jbGljazs+O2w8d2luZG93LmNsb3NlKClcOzs+Pj47Oz47dDxwPHA8bDxWaXNpYmxlOz47bDxvPHQ+Oz4+Oz47O" +
                "z47dDxAMDw7Ozs7Ozs7Ozs7Pjs7Pjt0PEAwPDs7Ozs7Ozs7Ozs+Ozs+O3Q8QDA8Ozs7Ozs7Ozs7Oz47Oz47dDw7bDxpPDA+O2k8MT4" +
                "7aTwyPjtpPDQ+Oz47bDx0PDtsPGk8MD47aTwxPjs+O2w8dDw7bDxpPDA+O2k8MT47PjtsPHQ8QDA8Ozs7Ozs7Ozs7Oz47Oz47dDxAM" +
                "Dw7Ozs7Ozs7Ozs7Pjs7Pjs+Pjt0PDtsPGk8MD47aTwxPjs+O2w8dDxAMDw7Ozs7Ozs7Ozs7Pjs7Pjt0PEAwPDs7Ozs7Ozs7Ozs+Ozs" +
                "+Oz4+Oz4+O3Q8O2w8aTwwPjs+O2w8dDw7bDxpPDA+Oz47bDx0PEAwPDs7Ozs7Ozs7Ozs+Ozs+Oz4+Oz4+O3Q8O2w8aTwwPjtpPDE+O" +
                "z47bDx0PDtsPGk8MD47PjtsPHQ8QDA8cDxwPGw8VmlzaWJsZTs+O2w8bzxmPjs+Pjs+Ozs7Ozs7Ozs7Oz47Oz47Pj47dDw7bDxpPD" +
                "A+Oz47bDx0PEAwPHA8cDxsPFZpc2libGU7PjtsPG88Zj47Pj47Pjs7Ozs7Ozs7Ozs+Ozs+Oz4+Oz4+O3Q8O2w8aTwwPjs+O2w8dDw7" +
                "bDxpPDA+Oz47bDx0PHA8cDxsPFRleHQ7PjtsPEhIWFk7Pj47Pjs7Pjs+Pjs+Pjs+Pjt0PEAwPDs7Ozs7Ozs7Ozs+Ozs+Oz4+Oz4+O" +
                "z6y3xglOSssPBnZSlDHQ4Ani+9RzQ=="));
        params.add(new BasicNameValuePair("ddlXN", "2018-2019"));
        params.add(new BasicNameValuePair("ddlXQ", "2"));
        params.add(new BasicNameValuePair("Button1", "按学期查询"));
        HttpPost scorePagePost = new HttpPost(BASE_URL + "/" + scoreURL);
        scorePagePost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        BasicHeader referer = new BasicHeader("Referer", refererURL);
        scorePagePost.setHeader(cookie);
        scorePagePost.setHeader(referer);
        HttpResponse resp = httpClient.execute(scorePagePost);
        document = Jsoup.parse(EntityUtils.toString(resp.getEntity(), Consts.UTF_8));

        //当前查询的学期标题
        String title = document.getElementById("Label4").getElementsByTag("font").text();
        System.out.println(title);

        //成绩列表行
        Elements trs = document.getElementById("Datagrid1").getElementsByTag("tr");
        HashMap<String, String> map = new HashMap<>();
        List<ScoreVO> list = new ArrayList<>();
        ScoreVO scoreVO = null;
        System.out.println(trs.size());
        for (int i = 1; i < trs.size(); i++) {
            scoreVO = new ScoreVO();
            //课程名
            String courseName = trs.get(i).getElementsByTag("td").get(3).text();
            //学分
            String credit = trs.get(i).getElementsByTag("td").get(6).text();
            //绩点
            String gradePoint = trs.get(i).getElementsByTag("td").get(7).text();
            //成绩
            String score = trs.get(i).getElementsByTag("td").get(8).text();

            scoreVO.setCourseName(courseName);
            scoreVO.setCredit(credit);
            scoreVO.setGradePoint(gradePoint);
            scoreVO.setScore(score);
            list.add(scoreVO);
            System.out.println(courseName + "  " +credit+ "  "+gradePoint+"  "+score);
        }


    }

}
