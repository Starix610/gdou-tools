package com.starix.gdou.controller;

import com.starix.gdou.entity.CpdailyUser;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.service.CpdailyUserService;
import com.starix.gdou.task.CpdailySignInAsyncTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Tobu
 * @date 2020-04-06 12:09
 */
@RestController
@Slf4j
@CrossOrigin
public class CpdailyController {

    @Autowired
    private CpdailyUserService cpdailyUserService;

    @PostMapping(value = "/cpdaily/addUser")
    public CommonResult addCpdailyUser(@RequestBody @Valid CpdailyUser user, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()){
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            //多个校验错误只返回第一个错误信息给前端
            log.info("用户参数校验错误:[{}]", allErrors.get(0).getDefaultMessage());
            return CommonResult.validateFailed(allErrors.get(0).getDefaultMessage());
        }
        cpdailyUserService.saveUser(user);
        return CommonResult.success();
    }

}
