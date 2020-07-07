package com.starix.gdou.service.impl;

import com.starix.gdou.entity.Student;
import com.starix.gdou.exception.CustomException;
import com.starix.gdou.dto.LoginResult;
import com.starix.gdou.repository.StudentRepository;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.service.GdouJWService;
import com.starix.gdou.vo.ExamVO;
import com.starix.gdou.vo.ScoreVO;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 旧版教务系统服务service实现类
 * @author Starix
 * @date 2019-11-20 20:05
 */
@Service
@Slf4j
public class GdouJWServiceImpl implements GdouJWService {

    //python脚本绝对路径
    @Value("${python.path}")
    private String PYTHON_PATH;
    //教务系统地址
    @Value("${gdou.jw-url}")
    private String BASE_URL;

    // python脚本位置
    // private static final String PYTHON_PATH = "F:\\IdeaProjects\\project\\gdou-score-query\\src\\main\\resources\\python\\jw_captcha_ocr.py";
    // private static final String PYTHON_PATH = "/opt/server/gdou-jw-tools/pyhton/jw_captcha_ocr.py";

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public LoginResult login(String xh, String password) throws Exception {
        //使用python自动获取并识别验证码
        Process process = Runtime.getRuntime().exec("python " + PYTHON_PATH + "jw_captcha_ocr.py");
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
            throw new CustomException(CommonResult.failed("服务器异常"));
        }

        HttpClient httpclient = HttpClientBuilder.create().build();

        //执行登录
        HttpPost loginPost = new HttpPost(BASE_URL + "/default2.aspx");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        //__VIEWSTATE需要自己获取
        params.add(new BasicNameValuePair("__VIEWSTATE", "dDwxNTMxMDk5Mzc0Ozs+oHR0TeFaplX14wgfE2ZakJztUwk="));
        //学号
        params.add(new BasicNameValuePair("txtUserName", xh));
        //密码
        params.add(new BasicNameValuePair("TextBox2",password));
        //验证码
        params.add(new BasicNameValuePair("txtSecretCode", result.get(0)));
        //用户类型
        params.add(new BasicNameValuePair("RadioButtonList1", "学生"));
        //未知参数(应该是用于标识当前点击的按钮)，必填
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

            return new LoginResult(cookie, homePageHtml,BASE_URL + location, xh);

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
                log.error("学号[{}]登录失败，原因:{}", xh, "系统异常, errInfo为空");
                throw new CustomException(CommonResult.failed("系统异常，请稍后重试"));
            }else if (errInfo.contains("验证码不正确")){
                // 验证码不正确则递归重试至识别正确
                log.info("学号[{}]登录失败，验证码识别错误，正在重试", xh);
                return login(xh, password);
            }else {
                log.error("学号[{}]登录失败，原因:{}", xh, errInfo);
                throw new CustomException(CommonResult.failed(errInfo));
            }
        }
    }


    @Override
    public List<ScoreVO> getScore(LoginResult loginResult, String year, String semester) throws IOException {

        Document document = Jsoup.parse(loginResult.getHomePageHtml());

        HttpClient httpClient = HttpClientBuilder.create().build();

        //查成绩页面URL
        String scoreURL = document.getElementsByAttributeValue("onclick", "GetMc('成绩查询');").get(0).attr("href");

        //进入查成绩页面。这里需要动态获取__VIEWSTATE。
        HttpGet examPageGet = new HttpGet(BASE_URL + "/" + scoreURL);
        BasicHeader referer = new BasicHeader("Referer", loginResult.getRefererURL());
        examPageGet.setHeader(loginResult.getCookie());
        examPageGet.setHeader(referer);
        HttpResponse response = httpClient.execute(examPageGet);
        document = Jsoup.parse(EntityUtils.toString(response.getEntity(), Consts.UTF_8));
        String viewState = document.getElementById("Form1").getElementsByAttributeValue("name", "__VIEWSTATE").get(0).val();


        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("__VIEWSTATE", viewState));
        params.add(new BasicNameValuePair("ddlXN", year));
        params.add(new BasicNameValuePair("ddlXQ", semester));
        params.add(new BasicNameValuePair("Button1", "按学期查询"));
        HttpPost scorePagePost = new HttpPost(BASE_URL + "/" + scoreURL);
        scorePagePost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
        scorePagePost.setHeader(loginResult.getCookie());
        scorePagePost.setHeader(referer);
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

        //查考试页面URL
        String scoreURL;
        try {
            scoreURL = document.getElementsByAttributeValue("onclick", "GetMc('学生考试查询');").get(0).attr("href");
        } catch (Exception e){
            // 有些情况下教务系统没有查考试的入口链接，抛异常处理
            throw new CustomException(CommonResult.failed("教务系统暂时不能查询考试信息！"));
        }

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
        return login(student.getUsername(), student.getPassword());
    }


    @Override
    public void autoEvaluate(LoginResult loginResult, String content, Integer mode) throws Exception {

        Document document = Jsoup.parse(loginResult.getHomePageHtml());

        Elements navLis = document.getElementById("headDiv").getElementsByAttributeValue("class", "nav").get(0)
                .getElementsByAttributeValue("class", "top");
        Elements evaluationLis = findEvaluationLis(navLis);

        HttpClient httpClient = HttpClientBuilder.create().build();

        if (evaluationLis.size() <= 1){
            throw new CustomException(CommonResult.fail(300, "暂时没有需要执行的评教或者你已经完成了评教，请到官网查看"));
        }

        for (int k = 1; k < evaluationLis.size(); k++) {
            //进入评教页面
            String evaluationURL = evaluationLis.get(k).getElementsByTag("a").get(0).attr("href");
            String kcName = evaluationLis.get(k).getElementsByTag("a").get(0).text();
            HttpGet examPageGet = new HttpGet(BASE_URL + "/" + evaluationURL);
            BasicHeader referer = new BasicHeader("Referer", loginResult.getRefererURL());
            examPageGet.setHeader(loginResult.getCookie());
            examPageGet.setHeader(referer);
            HttpResponse response = httpClient.execute(examPageGet);
            document = Jsoup.parse(EntityUtils.toString(response.getEntity(), Consts.UTF_8));

            //处理jw系统服务器异常导致没有返回正常页面
            if(document.getElementById("Form1") == null){
                throw new CustomException(CommonResult.fail(300, "教务系统服务器异常，请稍后重试"));
            }

            //获取VIEWSTATE
            String viewState = document.getElementById("Form1").getElementsByAttributeValue("name", "__VIEWSTATE").get(0).val();

            //处理某些特殊异常情况(已完成评教但是lis.size()>1)
            try {
                if (document.getElementById("pjkc").children().size() == 0){
                    throw new CustomException(CommonResult.fail(300, "你已经完成评教！"));
                }
            } catch (Exception e){
                // TODO: 2020-06-20 临时代码，评教异常排查
                log.error(">>>>>>>>>>>>评教出现异常: {}", e.getMessage());
                System.out.println(evaluationLis.html());
                throw e;
            }

            //pjkc参数（年份和课程号）
            String pjkc = document.getElementById("pjkc").getElementsByAttributeValue("selected", "selected").get(0).val();

            //教师评价等级选项参数
            Elements selects = document.getElementById("DataGrid1").getElementsByTag("select");
            //课程有多位教师的话，删除除了第一列之外的其他教师列表，只提交一位教师的评价数据
            selects.removeIf(s -> !s.attr("name").contains("JS1"));
            List<String> paramNames1 = selects.eachAttr("name");

            //教材评价等级选项参数
            List<String> paramNames2 = null;
            if (document.getElementById("dgPjc") != null){
                paramNames2 = document.getElementById("dgPjc").getElementsByTag("select").eachAttr("name");
            }

            //评价内容
            String pjxx = content;

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("__VIEWSTATE", viewState));
            params.add(new BasicNameValuePair("pjxx", pjxx));

            //教师评价等级参数列表
            List<NameValuePair> paramPairs = new ArrayList<>();
            //取一个随机索引，使评价等级选项列表中随机来一个B（评价等级全相同会被禁止提交）
            int randomIndex = (int) (Math.random() * paramNames1.size());
            for (int i = 0; i < paramNames1.size(); i++) {
                if (i == randomIndex){
                    paramPairs.add(new BasicNameValuePair(paramNames1.get(i), "B"));
                    continue;
                }
                paramPairs.add(new BasicNameValuePair(paramNames1.get(i), "A"));
            }
            params.addAll(paramPairs);
            //教材评价等级参数列表
            if (paramNames2 != null){
                paramPairs = new ArrayList<>();
                randomIndex = (int) (Math.random() * paramNames2.size());
                for (int i = 0; i < paramNames2.size(); i++) {
                    if (i == randomIndex){
                        paramPairs.add(new BasicNameValuePair(paramNames2.get(i), "B"));
                        continue;
                    }
                    paramPairs.add(new BasicNameValuePair(paramNames2.get(i), "A"));
                }
            }
            params.addAll(paramPairs);

            params.add(new BasicNameValuePair("pjkc", pjkc));
            params.add(new BasicNameValuePair("Button1", ""));

            HttpPost evaluatePost = new HttpPost(BASE_URL + "/" + evaluationURL);
            evaluatePost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
            evaluatePost.setHeader(referer);
            evaluatePost.setHeader(loginResult.getCookie());
            HttpResponse saveEvaluationResp = httpClient.execute(evaluatePost);
            String respHtml = EntityUtils.toString(saveEvaluationResp.getEntity(), Consts.UTF_8);
                document = Jsoup.parse(respHtml);

            //若出现直接包含在<script>标签内的alert语句的说明有错误信息，提交失败
            if (document.toString().contains("<script>alert") && !document.toString().contains("所有评价已完成")){
                System.out.println(document.toString());
                log.error("[{}]评价失败=========>[{}]", loginResult.getXh(), kcName);
                //自定义300状态码
                throw new CustomException(CommonResult.fail(300,"评教失败，请稍后重试"));
            }else {
                log.info("[{}]评价成功=========>[{}]", loginResult.getXh(), kcName);
            }
        }

        if (mode == 0){
            //执行最终提交
            evalutionSubmit(loginResult, evaluationLis, document);
        }

    }


    /**
     * 执行最终提交
     */
    private void evalutionSubmit(LoginResult loginResult, Elements lis, Document document) throws Exception {
        //提交按钮可用说明已经评价完成，可以执行最后提交
        String submitDisabled = document.getElementById("Button2").attr("disabled");
        if (!StringUtils.isEmpty(submitDisabled)){
            log.error("[{}]评价未完成", loginResult.getXh());
            throw new CustomException(CommonResult.fail(300,"评教未完成，请稍后重试"));
        }else{
            log.info("[{}]所有评价已完成，正在提交", loginResult.getXh());
            HttpClient httpClient = HttpClientBuilder.create().build();
            //进入第一个评教页面
            String evaluationURL = lis.get(1).getElementsByTag("a").get(0).attr("href");
            HttpGet examPageGet = new HttpGet(BASE_URL + "/" + evaluationURL);
            BasicHeader referer = new BasicHeader("Referer", loginResult.getRefererURL());
            examPageGet.setHeader(loginResult.getCookie());
            examPageGet.setHeader(referer);
            HttpResponse response = httpClient.execute(examPageGet);
            document = Jsoup.parse(EntityUtils.toString(response.getEntity(), Consts.UTF_8));

            //获取VIEWSTATE
            String viewState = document.getElementById("Form1").getElementsByAttributeValue("name", "__VIEWSTATE").get(0).val();
            //pjkc参数（年份和课程号）
            String pjkc = document.getElementById("pjkc").getElementsByAttributeValue("selected", "selected").get(0).val();
            String pjxx = document.getElementById("pjxx").text();

            //教师评价等级选项参数
            Elements selects1 = document.getElementById("DataGrid1").getElementsByTag("select");
            //课程有多位教师的话，删除除了第一列之外的其他教师列表，只提交一位教师的评价数据
            selects1.removeIf(s -> !s.attr("name").contains("JS1"));

            List<NameValuePair> params = new ArrayList<>();

            for (Element select : selects1) {
                params.add(new BasicNameValuePair(select.attr("name"), select.getElementsByAttribute("selected").get(0).val()));
            }
            //教材评价等级选项参数
            if (document.getElementById("dgPjc") != null){
                Elements selects2 = document.getElementById("dgPjc").getElementsByTag("select");
                for (Element select : selects2) {
                    params.add(new BasicNameValuePair(select.attr("name"), select.getElementsByAttribute("selected").get(0).val()));
                }
            }

            params.add(new BasicNameValuePair("__EVENTTARGET", ""));
            params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
            params.add(new BasicNameValuePair("__VIEWSTATE", viewState));
            params.add(new BasicNameValuePair("pjkc", pjkc));
            params.add(new BasicNameValuePair("pjxx", pjxx));
            params.add(new BasicNameValuePair("txt1", ""));
            params.add(new BasicNameValuePair("TextBox1", "0"));
            params.add(new BasicNameValuePair("Button2", ""));
            HttpPost submitPost = new HttpPost(BASE_URL + "/" + evaluationURL);
            submitPost.setEntity(new UrlEncodedFormEntity(params, Consts.UTF_8));
            referer = new BasicHeader("Referer", loginResult.getRefererURL());
            submitPost.setHeader(referer);
            submitPost.setHeader(loginResult.getCookie());
            HttpResponse saveEvaluationResp = httpClient.execute(submitPost);
            String respHtml = EntityUtils.toString(saveEvaluationResp.getEntity(), Consts.UTF_8);
            if (respHtml.contains("您已完成评价")){
                log.info("[{}]评教成功", loginResult.getXh());
            }else {
                System.out.println(document.toString());
                throw new CustomException(CommonResult.fail(300, "评价提交失败，请稍后重试"));
            }
        }
    }


    private Elements findEvaluationLis(Elements navLis) {
        for (Element navLi : navLis) {
            if (navLi.text().contains("教学质量评价")){
                return navLi.getElementsByTag("li");
            }
        }
        throw new CustomException(CommonResult.failed("教务系统暂时未开放教学评价！"));
    }

}
