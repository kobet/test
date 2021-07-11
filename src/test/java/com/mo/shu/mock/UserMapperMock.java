package com.mo.shu.mock;

import com.google.common.collect.Lists;
import com.mo.shu.bean.User;
import com.mo.shu.mapper.UserMapper;
import org.mockito.Mockito;

public class UserMapperMock {

    public void mockQueryNull(UserMapper userMapper) {
        Mockito.when(userMapper.selectList(Mockito.any())).thenReturn(null);
    }

    public void mockQueryNotNull(UserMapper userMapper) {
        User user = new User();
        user.setAge(99);
        user.setName("mockUser1");
        user.setId(99L);
        Mockito.when(userMapper.selectList(Mockito.any())).thenReturn(Lists.newArrayList(user));
    }
}
