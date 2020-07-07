package com.starix.gdou.controller;

/**
 * v1版本controller
 * @author Starix
 * @date 2019-11-18 18:56
 */
// @RestController
// @CrossOrigin
// @Slf4j
// public class GdouJWController {
//
//     @Autowired
//     private GdouJWService gdouJWService;
//     @Autowired
//     private UserBindService userBindService;
//
//     @PostMapping("/login")
//     public CommonResult doLogin(String xh, String password, HttpSession httpSession) throws Exception {
//         if (StringUtils.isEmpty(xh) || StringUtils.isEmpty(password)){
//             return CommonResult.failed(ResultCode.VALIDATE_FAILED);
//         }
//         LoginResult loginResult = gdouJWService.login(xh, password);
//
//         httpSession.setAttribute("studentLoginInfo", loginResult);
//
//         return CommonResult.success();
//     }
//
//
//     @PostMapping("/autoLogin")
//     public CommonResult doAuoLogin(String openid, HttpSession httpSession) throws Exception {
//         if (StringUtils.isEmpty(openid)){
//             return CommonResult.failed(ResultCode.VALIDATE_FAILED);
//         }
//
//         LoginResult loginResult = gdouJWService.loginByOpenid(openid);
//
//         httpSession.setAttribute("studentLoginInfo", loginResult);
//
//         return CommonResult.success();
//     }
//
//
//
//     @PostMapping("/bind")
//     public CommonResult doBind(String openid, String xh, String password) throws Exception {
//         if (StringUtils.isEmpty(openid) || StringUtils.isEmpty(xh) || StringUtils.isEmpty(password)){
//             return CommonResult.failed(ResultCode.VALIDATE_FAILED);
//         }
//
//         log.info("[{}]正在绑定学号",xh);
//
//         // 绑定前先验证账号密码是否正确
//         gdouJWService.login(xh, password);
//
//         userBindService.bind(openid, xh, password);
//
//         return CommonResult.success();
//     }
//
//
//     @GetMapping("/queryScore")
//     public CommonResult doQueryScore(String year, String semester, HttpSession httpSession) throws Exception {
//         if (StringUtils.isEmpty(year) || StringUtils.isEmpty(semester)){
//             return CommonResult.failed(ResultCode.VALIDATE_FAILED);
//         }
//
//         LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");
//
//         if (loginResult == null){
//             return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
//         }
//
//         log.info("[{}]正在查询成绩",loginResult.getXh());
//         List<ScoreVO> scoreList = gdouJWService.getScore(loginResult, year, semester);
//
//         return CommonResult.success(scoreList);
//     }
//
//
//     @GetMapping("/queryExam")
//     public CommonResult doQueryExam(String year, String semester, HttpSession httpSession) throws Exception {
//
//         if (StringUtils.isEmpty(year) || StringUtils.isEmpty(semester)){
//             return CommonResult.failed(ResultCode.VALIDATE_FAILED);
//         }
//
//         LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");
//
//         if (loginResult == null){
//             return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
//         }
//
//         log.info("[{}]正在查询考试", loginResult.getXh());
//         List<ExamVO> scoreList = gdouJWService.getExam(loginResult, year, semester);
//
//         return CommonResult.success(scoreList);
//     }
//
//
//
//
//     //获得查成绩页面年份下拉列表数据
//     @GetMapping("/getScoreYearOptionsList")
//     public CommonResult doGetScoreYearOptionsList(HttpSession httpSession) throws Exception {
//         LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");
//         if (loginResult == null){
//             return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
//         }
//         List<String> yearOptionsList = gdouJWService.getSocreYearOptionsList(loginResult);
//         return CommonResult.success(yearOptionsList);
//     }
//
//
//     //获得查考试页面年份下拉列表数据
//     @GetMapping("/getExamYearOptionsList")
//     public CommonResult doGetExamYearOptionsList(HttpSession httpSession) throws Exception {
//         LoginResult loginResult = (LoginResult) httpSession.getAttribute("studentLoginInfo");
//         if (loginResult == null){
//             return CommonResult.failed(ResultCode.UNAUTHORIZED,"你还没有登录或者登录信息已经过期");
//         }
//         List<String> yearOptionsList = gdouJWService.getExamYearOptionsList(loginResult);
//         return CommonResult.success(yearOptionsList);
//     }
//
//
//     //自动评教
//     @PostMapping("/autoEvaluate")
//     public CommonResult doAutoEval(String xh, String password, String content, Integer mode) throws Exception {
//         LoginResult loginResult = gdouJWService.login(xh, password);
//         log.info("[{}]正在自动评教，参数-->[password]:{},[content]:{},[mode]:{}",xh,password,content,mode);
//         gdouJWService.autoEvaluate(loginResult,content, mode);
//         return CommonResult.success();
//     }
//
// }
