package com.mo.shu.util;

import com.mo.shu.bean.MockInfo;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.*;

public class MockHelper {

    private static final ThreadLocal<MockInfo> LOCAL_MOCK = new ThreadLocal<>();

    public static void initMock(Map<Field, Object> mockObjMap, Map<Field, Object> recoveryObjMap, List<Field> injectMocks) {
        MockInfo mockInfo = new MockInfo();
        mockInfo.setInjectMocks(injectMocks);
        mockInfo.setMockObjMap(mockObjMap);
        mockInfo.setRecoveryObjMap(recoveryObjMap);
        LOCAL_MOCK.set(mockInfo);
    }

    public static void injectIndependent() {
        MockInfo mockInfo = LOCAL_MOCK.get();
        Optional.ofNullable(mockInfo).ifPresent(info -> {
            Map<Field, Object> mockObjMap = info.getMockObjMap();
            List<Field> injectMocks = info.getInjectMocks();
            batchSetField(injectMocks, mockObjMap);
        });

    }

    public static void recoveryIndependent() {
        MockInfo mockInfo = LOCAL_MOCK.get();
        if (Objects.isNull(mockInfo)) {
            return;
        }
        Map<Field, Object> recoveryObjMap = mockInfo.getRecoveryObjMap();
        List<Field> injectMocks = mockInfo.getInjectMocks();

        batchSetField(injectMocks, recoveryObjMap);
    }

    private static void batchSetField(List<Field> injectMocks, Map<Field, Object> mockObjMap) {
        injectMocks.stream().forEach(injectMock -> {
            Class injectClass = injectMock.getType();
            Object obj = SpringBeanUtil.getBean(injectClass);

            // 获取代理的真实对象
            Object proxyTarget = AopTestUtils.getTargetObject(obj);
            Field[] injectFields = proxyTarget.getClass().getDeclaredFields();

            // 用反射设置属性
            Arrays.stream(injectFields).forEach(injectField -> mockObjMap
                    .entrySet()
                    .stream()
                    .filter(mock -> mock.getKey().getType() == injectField.getType())
                    .forEach(mock -> ReflectionTestUtils.setField(obj, mock.getKey().getName(), mock.getValue())));
        });
    }

    public static void clear() {
        LOCAL_MOCK.remove();
    }
}
