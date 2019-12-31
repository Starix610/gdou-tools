package com.starix.scorequery;

import com.starix.scorequery.service.SpiderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * @author Tobu
 * @date 2019-12-31 11:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpiderServiceTest {
    @Autowired
    private SpiderService spiderService;

    @Test
    public void testLogin() throws Exception {

        String xh = "201711621427";
        String password = "shiwenjie2019";
        spiderService.login(xh, password);

    }
}
