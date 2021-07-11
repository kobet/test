package com.mo.shu.service.impl;

import com.google.common.collect.Lists;
import com.mo.shu.bean.Product;
import com.mo.shu.bean.User;
import com.mo.shu.service.ProductService;
import com.mo.shu.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private UserService userService;

    @Override
    public List<Product> list() {
        return converter(userService.list());
    }

    private List<Product> converter(List<User> users) {
        return Optional.ofNullable(users).orElse(Lists.newArrayList()).stream().map(user -> {
            Product product = new Product();
            product.setProductName(user.getName());
            return product;
        }).collect(Collectors.toList());
    }

}
