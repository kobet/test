package com.mo.shu;

import com.mo.shu.bean.Product;
import com.mo.shu.mapper.UserMapper;
import com.mo.shu.mock.UserMapperMock;
import com.mo.shu.service.ProductService;
import com.mo.shu.service.UserService;
import com.mo.shu.util.DbProcess;
import com.mo.shu.util.InjectMockProcess;
import com.mo.shu.util.MockProcess;
import com.mo.shu.util.MockUnitsProcess;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ProductServiceTest extends CommonTestBase {

    @Autowired
    private ProductService productService;

    @InjectMockProcess
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Test
    @DbProcess(init = "init.sql", clear = "clear.sql")
    @MockUnitsProcess({@MockProcess(clazz = UserMapperMock.class, methodName = "mockQueryNull", objName = "userMapper")})
    public void testSelectNull() {
        System.out.println(("----- selectAll method test ------"));
        List<Product> userList = productService.list();
        Assert.assertEquals(0, userList.size());
    }

    @Test
    @DbProcess(init = "init.sql", clear = "clear.sql")
    @MockUnitsProcess({@MockProcess(clazz = UserMapperMock.class, methodName = "mockQueryNotNull", objName = "userMapper")})
    public void testSelect() {
        System.out.println(("----- selectAll method test ------"));
        List<Product> userList = productService.list();
        Assert.assertEquals(1, userList.size());
    }
}
