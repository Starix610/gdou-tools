package com.starix.gdou.service.impl;

import com.starix.gdou.entity.CpdailyUser;
import com.starix.gdou.exception.CustomException;
import com.starix.gdou.repository.CpdailyUserRepository;
import com.starix.gdou.response.CommonResult;
import com.starix.gdou.service.CpdailyUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Starix
 * @date 2020-04-06 13:28
 */
@Service
@Slf4j
public class CpdailyUserServiceImpl implements CpdailyUserService {

    //python脚本绝对路径
    @Value("${python.path}")
    private String PYTHON_PATH_AUTH;
    //python脚本命令行输出的编码
    @Value("${python.output-encoding}")
    private String OUTPUT_ENCODING;

    @Autowired
    private CpdailyUserRepository userRepository;

    @Override
    @Transactional
    public void saveUser(CpdailyUser user) throws Exception {
        log.info("[{}]正在添加用户", user.getUsername());
        CpdailyUser cpdailyUser = userRepository.findByUsername(user.getUsername());
        if (cpdailyUser != null){
            throw new CustomException(CommonResult.failed("该账号已经提交过了"));
        }
        boolean result = authentication(user.getUsername(), user.getPassword());
        if (!result){
            throw new CustomException(CommonResult.failed("用户名或密码错误，请注意查看推文中关于账号密码的说明"));
        }
        user.setCreateTime(new Date());
        userRepository.save(user);
        log.info("[{}]用户添加成功", user.getUsername());
    }

    @Override
    @Transactional
    public void deleteUser(String username, String password) throws Exception {
        log.info("[{}]正在取消自动签到", username);
        CpdailyUser cpdailyUser = userRepository.findByUsername(username);
        if (cpdailyUser == null){
            throw new CustomException(CommonResult.failed("用户记录不存在或已经取消自动签到"));
        }
        boolean result = authentication(username, password);
        if (!result){
            throw new CustomException(CommonResult.failed("用户名或密码错误"));
        }
        userRepository.deleteByUsername(username);
        log.info("[{}]取消自动签到成功", username);
    }

    @Override
    @Transactional
    public void updateUser(CpdailyUser user) throws Exception {
        log.info("[{}]正在更新签到信息", user.getUsername());
        CpdailyUser cpdailyUser = userRepository.findByUsername(user.getUsername());
        if (cpdailyUser == null){
            throw new CustomException(CommonResult.failed("修改失败，该账号未提交过"));
        }
        boolean result = authentication(user.getUsername(), user.getPassword());
        if (!result){
            throw new CustomException(CommonResult.failed("用户名或密码错误"));
        }
        BeanUtils.copyProperties(user, cpdailyUser, "id", "createTime");
        cpdailyUser.setUpdateTime(new Date());
        userRepository.save(cpdailyUser);
        log.info("[{}]签到信息更新成功", user.getUsername());
    }

    /**
     * 用户认证，确保用户名和密码正确
     * @param username
     * @param password
     */
    private boolean authentication(String username, String password) throws Exception {
        String[] cmd = new String[6];
        cmd[0] = "python";
        cmd[1] = "cpdaily_auth.py";
        cmd[2] = "--username";
        cmd[3] = username;
        cmd[4] = "--password";
        cmd[5] = password;
        Process process = Runtime.getRuntime().exec(cmd, null, new File(PYTHON_PATH_AUTH));
        int status = process.waitFor();
        if (status != 0){
            log.error("Python脚本执行出错,status:{}", status);
            throw new CustomException(CommonResult.failed("操作失败，请稍后重试或在公众号后台留言反馈"));
        }
        InputStream in = process.getInputStream();
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(in, OUTPUT_ENCODING));
        String line;
        List<String> resultLines = new ArrayList<>();
        while ((line = buffReader.readLine()) != null){
            resultLines.add(line);
        }
        //简单关闭流，暂时不考虑异常
        buffReader.close();
        in.close();

        if (resultLines.contains("result-认证失败")){
            log.info("[{}]认证失败", username);
            return false;
        }else if (resultLines.contains("result-认证成功")){
            log.info("[{}]认证成功", username);
            return true;
        }else {
            log.info("[{}]认证过程出现异常，Python输出内容：{}", username, resultLines);
            throw new CustomException(CommonResult.failed("操作失败，请稍后重试或在公众号后台留言反馈"));
        }
    }
}
