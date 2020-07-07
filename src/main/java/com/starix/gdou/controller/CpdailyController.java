package com.starix.gdou.controller;

import com.starix.gdou.entity.CpdailyUser;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.service.CpdailyUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Starix
 * @date 2020-04-06 12:09
 */
@RestController
@RequestMapping("/cpdaily")
@Slf4j
@CrossOrigin
public class CpdailyController {

    @Autowired
    private CpdailyUserService cpdailyUserService;

    //新增签到用户
    @PostMapping(value = "/addUser")
    public CommonResult addCpdailyUser(@RequestBody @Valid CpdailyUser user, BindingResult bindingResult) throws Exception {
        // TODO: 2020-05-04 停止使用
        return CommonResult.failed("自动签到服务已停用！");
        // if (bindingResult.hasErrors()){
        //     List<ObjectError> allErrors = bindingResult.getAllErrors();
        //     //多个校验错误只返回第一个错误信息给前端
        //     log.info("用户参数校验错误:[{}]", allErrors.get(0).getDefaultMessage());
        //     return CommonResult.validateFailed(allErrors.get(0).getDefaultMessage());
        // }
        // cpdailyUserService.saveUser(user);
        // return CommonResult.success();
    }

    //取消签到
    @PostMapping(value = "/delUser")
    public CommonResult delCpdailyUser(String username, String password) throws Exception {
        // TODO: 2020-05-04 停止使用
        return CommonResult.failed("自动签到服务已停用，无须手动取消！");
        // if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
        //     return CommonResult.validateFailed();
        // }
        // cpdailyUserService.deleteUser(username, password);
        // return CommonResult.success();
    }

    //更新签到信息
    @PostMapping(value = "/updateUser")
    public CommonResult udpateCpdailyUser(@RequestBody @Valid CpdailyUser user, BindingResult bindingResult) throws Exception {
        // TODO: 2020-05-04 停止使用
        return CommonResult.failed("自动签到服务已停用！");
        // if (bindingResult.hasErrors()){
        //     List<ObjectError> allErrors = bindingResult.getAllErrors();
        //     //多个校验错误只返回第一个错误信息给前端
        //     log.info("用户参数校验错误:[{}]", allErrors.get(0).getDefaultMessage());
        //     return CommonResult.validateFailed(allErrors.get(0).getDefaultMessage());
        // }
        // cpdailyUserService.updateUser(user);
        // return CommonResult.success();
    }

}
