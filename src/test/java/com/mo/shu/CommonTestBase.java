package com.mo.shu;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mo.shu.util.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.configuration.MockAnnotationProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = ShuApplication.class)
public class CommonTestBase {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Rule
    public TestName testName = new TestName();

    @ClassRule
    public static EnhanceTestName enhanceTestName = new EnhanceTestName();

    private static MockAnnotationProcessor mockAnnotationProcessor = new MockAnnotationProcessor();

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);

        resetThreadLocal();

        executeInitSql();

        mockInject();
    }

    @After
    public void clear() {
        Method currentMethod = getTestMethod();
        assertNotNull(currentMethod);

        DbProcess dbProcess = getDbProcessAnnotation(currentMethod);
        if (Objects.nonNull(dbProcess)) {
            executeSqlFile(dbProcess.clear());
        }
        MockHelper.recoveryIndependent();
    }

    private void resetThreadLocal() {
        MockHelper.clear();
    }

    private void mockInject() {
        Class clazz = enhanceTestName.getClazz();
        assertNotEquals("当前用例不能为空", clazz);

        Field[] fields = clazz.getDeclaredFields();
        List<Field> injectMocks = Lists.newArrayList();
        List<Field> mockList = Lists.newArrayList();
        for (Field field : fields) {
            if (field.isAnnotationPresent(InjectMockProcess.class)) {
                injectMocks.add(field);
            }
            if (field.isAnnotationPresent(Mock.class)) {
                mockList.add(field);
            }
        }
        if (CollectionUtils.isEmpty(injectMocks)) {
            return;
        }
        Map<Field, Object> mockObjMap = Maps.newHashMap();
        Map<Field, Object> recoveryObjMap = Maps.newHashMap();
        mockList.stream().forEach(field -> {
            Object reallyMock = mockAnnotationProcessor.process(field.getAnnotation(Mock.class), field);
            if (Objects.nonNull(reallyMock)) {
                mockObjMap.put(field, reallyMock);
            }
            Object reallyObj = SpringBeanUtil.getBean(field.getType());
            if (Objects.nonNull(reallyObj)) {
                recoveryObjMap.put(field, reallyObj);
            }
        });

        MockHelper.initMock(mockObjMap, recoveryObjMap, injectMocks);

        MockHelper.injectIndependent();

        mockMethodInvoke(mockObjMap);
    }

    private void mockMethodInvoke(Map<Field, Object> mockObjMap) {
        Method currentMethod = getTestMethod();
        assertNotNull(currentMethod);

        MockUnitsProcess mockUnitsProcess = getMockUnitsProcess(currentMethod);
        if (Objects.isNull(mockUnitsProcess)) {
            return;
        }
        Arrays.stream(mockUnitsProcess.value()).forEach(mockProcess -> {
            String mockMethod = mockProcess.methodName();
            Class mockClazz = mockProcess.clazz();
            String fieldName = mockProcess.objName();
            Method[] mockMethods = mockClazz.getMethods();

            Method targetMethod = Stream.of(mockMethods)
                    .filter(method -> StringUtils.equals(method.getName(), mockMethod))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("未找到@Mock注解对象的mock方法"));

            Map.Entry<Field, Object> entry = mockObjMap.entrySet()
                    .stream()
                    .filter(mockObj -> StringUtils.equals(fieldName, mockObj.getKey().getName()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("未找到mock方法对应的@Mock标记的对象"));

            try {
                Object mockObj = mockClazz.newInstance();
                targetMethod.invoke(mockObj, entry.getValue());
            } catch (Exception e) {
                fail("执行mock方法失败");
            }
        });
    }

    private MockUnitsProcess getMockUnitsProcess(Method method) {
        return method.getAnnotation(MockUnitsProcess.class);
    }

    /**
     * 执行初始化SQL文件
     */
    private void executeInitSql() {
        Method currentMethod = getTestMethod();
        assertNotNull(currentMethod);

        DbProcess dbProcess = getDbProcessAnnotation(currentMethod);
        if (Objects.nonNull(dbProcess)) {
            // 执行初始化SQL
            executeSqlFile(dbProcess.init());
        }
    }

    /**
     * 执行SQL文件
     *
     * @param sqlFile sql文件
     */
    private void executeSqlFile(String sqlFile) {
        String initSql;
        String path = this.getClass().getResource("/").getPath() + "db/" + sqlFile;
        File file = new File(path);
        assertTrue("DB文件不存在", file.exists());

        try {

            InputStreamReader in = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(in);
            StringBuffer sb = new StringBuffer();
            while ((initSql = br.readLine()) != null) {
                sb.append(initSql);
            }
            jdbcTemplate.execute(sb.toString());
        } catch (Exception e) {
            fail("用例DB文件执行失败");
        }
    }

    /**
     * 查找方法上的DbProcess注解
     *
     * @param method 方法
     * @return DbProcess
     */
    private DbProcess getDbProcessAnnotation(Method method) {
        return method.getAnnotation(DbProcess.class);
    }

    private Method getTestMethod() {
        String currentTestMethodName = getTestMethodName();
        try {

            return this.getClass().getMethod(currentTestMethodName, null);
        } catch (Exception e) {
            fail("获取当前执行用例方法失败");
        }
        return null;
    }

    /**
     * 获取用例方法名称
     *
     * @return 用例方法名称
     */
    private String getTestMethodName() {
        return testName.getMethodName();
    }
}
