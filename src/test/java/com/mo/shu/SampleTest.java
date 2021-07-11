package com.mo.shu;

import com.mo.shu.bean.User;
import com.mo.shu.service.UserService;
import com.mo.shu.util.DbProcess;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SampleTest extends CommonTestBase {

    @Autowired
    private UserService userService;

    @Test
    @DbProcess(init = "init.sql", clear = "clear.sql")
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<User> userList = userService.list();
        Assert.assertEquals(6, userList.size());
        userList.forEach(System.out::println);
    }
}
