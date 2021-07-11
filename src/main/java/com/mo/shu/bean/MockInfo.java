package com.mo.shu.bean;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

@Data
public class MockInfo {

    private Map<Field, Object> mockObjMap;

    private Map<Field, Object> recoveryObjMap;

    private List<Field> injectMocks;
}
