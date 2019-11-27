package com.starix.scorequery.service.impl;

import com.starix.scorequery.entity.Student;
import com.starix.scorequery.exception.CustomException;
import com.starix.scorequery.pojo.LoginResult;
import com.starix.scorequery.repository.StudentRepository;
import com.starix.scorequery.response.CommonResult;
import com.starix.scorequery.service.SpiderService;
import com.starix.scorequery.vo.ExamVO;
import com.starix.scorequery.vo.ScoreVO;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tobu
 * @date 2019-11-20 20:05
 */
@Service
@Slf4j
public class SpiderServiceImpl implements SpiderService {

    private static final String BASE_URL = "http://210.38.137.126:8016";

    // private static final String PYTHON_PATH = "F:\\IdeaProjects\\project\\gdou-score-query\\src\\main\\resources\\python\\code_ocr.py";
    private static final String PYTHON_PATH = "/opt/server/gdou_score_query/pyhton/code_ocr.py";

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public LoginResult login(String xh, String password) throws Exception {
        //使用python自动获取并识别验证码
        Process process = Runtime.getRuntime().exec("python " + PYTHON_PATH);
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
            }
            buffReader.close();
            inputReader.close();
            in.close();
        } else {
            throw new CustomException(CommonResult.failed("脚本执行出错"));
        }

        HttpClient httpclient = HttpClientBuilder.create().build();

        //执行登录
        HttpPost loginPost = new HttpPost(BASE_URL + "/default2.aspx");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //__VIEWSTATE需要自己获取
        params.add(new BasicNameValuePair("__VIEWSTATE", "dDwxNTMxMDk5Mzc0Ozs+OBE730NQqeUlEYO76T3Qls4CiUo="));
        //学号
        params.add(new BasicNameValuePair("txtUserName", xh));
        //密码
        params.add(new BasicNameValuePair("TextBox2",password));
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
        if (loginResp.getStatusLine().getStatusCode() == 302) {
            log.info("学号[{}]登录成功", xh);
            //获取跳转的URL
            String location = loginResp.getFirstHeader("Location").getValue();
            HttpGet homePageGet = new HttpGet(BASE_URL + location);
            //将python中获取验证码的Cookie设置到当前请求头中(携带同一个Cookie才能验证和进行后面的登录)
            homePageGet.setHeader(cookie);

            HttpResponse homePageResp = httpclient.execute(homePageGet);
            String homePageHtml = EntityUtils.toString(homePageResp.getEntity(), Consts.UTF_8);

            return new LoginResult(cookie, homePageHtml,BASE_URL + location);

        }else {
            String html = EntityUtils.toString(loginResp.getEntity(), Consts.UTF_8);
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
            log.error("学号[{}]登录失败，原因:{}", xh, errInfo);
            throw new CustomException(CommonResult.failed(errInfo));
        }
    }

    @Override
    public List<ScoreVO> getScore(LoginResult loginResult, String year, String semester) throws IOException {

        Document document = Jsoup.parse(loginResult.getHomePageHtml());

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
        params.add(new BasicNameValuePair("ddlXN", year));
        params.add(new BasicNameValuePair("ddlXQ", semester));
        params.add(new BasicNameValuePair("Button1", "按学期查询"));
        HttpPost scorePagePost = new HttpPost(BASE_URL + "/" + scoreURL);
        scorePagePost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        BasicHeader referer = new BasicHeader("Referer", loginResult.getRefererURL());
        scorePagePost.setHeader(loginResult.getCookie());
        scorePagePost.setHeader(referer);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse resp = httpClient.execute(scorePagePost);
        document = Jsoup.parse(EntityUtils.toString(resp.getEntity(), Consts.UTF_8));


        //当前查询的学期标题
        // String title = document.getElementById("Label4").getElementsByTag("font").text();

        //成绩列表行
        Elements trs = document.getElementById("Datagrid1").getElementsByTag("tr");
        List<ScoreVO> scoreList = new ArrayList<>();
        ScoreVO scoreVO = null;
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
            scoreList.add(scoreVO);
        }
        return scoreList;
    }

    @Override
    public List<ExamVO> getExam(LoginResult loginResult, String year, String semester) throws Exception {
        Document document = Jsoup.parse(loginResult.getHomePageHtml());

        //考试查询页面URL
        String examURL = document.getElementsByAttributeValue("onclick", "GetMc('学生考试查询');").get(0).attr("href");


        // 进入考试查询页面。这里需要动态获取__VIEWSTATE。
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet examPageGet = new HttpGet(BASE_URL + "/" + examURL);
        BasicHeader referer = new BasicHeader("Referer", loginResult.getRefererURL());
        examPageGet.setHeader(loginResult.getCookie());
        examPageGet.setHeader(referer);
        HttpResponse response = httpClient.execute(examPageGet);
        document = Jsoup.parse(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
        //默认viewState，对应考试查询页面的默认最新年份的viewState
        String viewState = document.getElementById("form1").getElementsByAttributeValue("name", "__VIEWSTATE").get(0).val();


        // 获取新的__VIEWSTATE
        // 先将xnd参数(学年)设置为空再次发起请求（目的是切换学年下拉列表状态，获得新的__VIEWSTATE），
        // 因为教务系统这个页面默认是显示最新学年的数据，下次如果请求的还是当前默认学年的数据而且带着相同的__VIEWSTATE
        // 的话会得不到数据。因此这样能够获取到下一次正常请求需要使用的__VIEWSTATE
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("__EVENTTARGET", "xnd"));
        params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        params.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        params.add(new BasicNameValuePair("xnd", ""));
        params.add(new BasicNameValuePair("xqd", semester));
        HttpPost examPagePost = new HttpPost(BASE_URL + "/" + examURL);
        examPagePost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        examPagePost.setHeader(referer);
        examPagePost.setHeader(loginResult.getCookie());
        response = httpClient.execute(examPagePost);
        document = Jsoup.parse(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
        //对应空年份状态的__VIEWSTATE，带着这个__VIEWSTATE下次即可请求任意年份的数据
        viewState = document.getElementById("form1").getElementsByAttributeValue("name", "__VIEWSTATE").get(0).val();


        params = new ArrayList<>();
        params.add(new BasicNameValuePair("__EVENTTARGET", "xnd"));
        params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
        //__VIEWSTATE可根据需要自己获取
        params.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        params.add(new BasicNameValuePair("xnd", year));
        params.add(new BasicNameValuePair("xqd", semester));
        examPagePost = new HttpPost(BASE_URL + "/" + examURL);
        examPagePost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        examPagePost.setHeader(referer);
        examPagePost.setHeader(loginResult.getCookie());
        response = httpClient.execute(examPagePost);

        document = Jsoup.parse(EntityUtils.toString(response.getEntity(), Consts.UTF_8));


        //考试列表行
        Elements trs = document.getElementById("DataGrid1").getElementsByTag("tr");
        List<ExamVO> examList = new ArrayList<>();
        ExamVO examVO = null;
        for (int i = 1; i < trs.size(); i++) {
            examVO = new ExamVO();
            //课程名
            String courseName = trs.get(i).getElementsByTag("td").get(1).text();
            //学生姓名
            String stuName = trs.get(i).getElementsByTag("td").get(2).text();
            //考试时间
            String examTime = trs.get(i).getElementsByTag("td").get(3).text();
            //考试地点
            String examPlace = trs.get(i).getElementsByTag("td").get(4).text();

            examVO.setCourseName(courseName);
            examVO.setStuName(stuName);
            examVO.setExamTime(examTime);
            examVO.setExamPlace(examPlace);
            examList.add(examVO);
        }
        return examList;
    }

    @Override
    public List<String> getSocreYearOptionsList(LoginResult loginResult) throws Exception {

        Document document = Jsoup.parse(loginResult.getHomePageHtml());

        //查成绩页面URL
        String scoreURL = document.getElementsByAttributeValue("onclick", "GetMc('成绩查询');").get(0).attr("href");

        HttpPost scorePagePost = new HttpPost(BASE_URL + "/" + scoreURL);
        BasicHeader referer = new BasicHeader("Referer", loginResult.getRefererURL());
        scorePagePost.setHeader(loginResult.getCookie());
        scorePagePost.setHeader(referer);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse resp = httpClient.execute(scorePagePost);
        document = Jsoup.parse(EntityUtils.toString(resp.getEntity(), Consts.UTF_8));

        if (document.toString().contains("请先完成评价")){
            throw new CustomException(CommonResult.failed("请先完成教学评价，才能查看成绩！"));
        }

        //成绩查询年份下拉列表数据
        Elements yearOptions = document.getElementById("ddlXN").getElementsByTag("option");
        List<String> yearList = new ArrayList<>();
        for (int i = 1; i < yearOptions.size(); i++) {
            yearList.add(yearOptions.get(i).text());
        }

        return yearList;
    }

    @Override
    public List<String> getExamYearOptionsList(LoginResult loginResult) throws Exception {

        Document document = Jsoup.parse(loginResult.getHomePageHtml());

        //查成绩页面URL
        String scoreURL = document.getElementsByAttributeValue("onclick", "GetMc('学生考试查询');").get(0).attr("href");

        HttpPost scorePagePost = new HttpPost(BASE_URL + "/" + scoreURL);
        BasicHeader referer = new BasicHeader("Referer", loginResult.getRefererURL());
        scorePagePost.setHeader(loginResult.getCookie());
        scorePagePost.setHeader(referer);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpResponse resp = httpClient.execute(scorePagePost);
        document = Jsoup.parse(EntityUtils.toString(resp.getEntity(), Consts.UTF_8));

        //考试查询年份下拉列表数据
        Elements yearOptions = document.getElementById("xnd").getElementsByTag("option");
        List<String> yearList = new ArrayList<>();
        for (int i = 1; i < yearOptions.size(); i++) {
            yearList.add(yearOptions.get(i).text());
        }

        return yearList;
    }

    @Override
    public LoginResult loginByOpenid(String openid) throws Exception {
        Student student = studentRepository.findByOpenid(openid);
        if (student == null){
            throw new CustomException(CommonResult.failed("未绑定学号"));
        }
        return login(student.getXh(), student.getPassword());
    }
}
