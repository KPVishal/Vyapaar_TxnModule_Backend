package com.bijlipay.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@EnableWebMvc
@Configuration
@ComponentScan({"com.bijlipay"})
public class WebConfig extends WebMvcConfigurerAdapter {
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setOneIndexedParameters(true);
        argumentResolvers.add(resolver);
        super.addArgumentResolvers(argumentResolvers);
    }

    public Pageable resolvePageable(Map<String, String> requestParams, Pageable pageable) {
        List<Sort.Order> orders = new ArrayList();
        Sort sorts = pageable.getSort();
        if (sorts != null) {
            sorts.forEach((sort) -> {
                String propertyName = sort.getProperty();
                String dirKey = propertyName + ".dir";
                if (requestParams.containsKey(dirKey)) {
                    String dirValue = (String)requestParams.get(dirKey);
                    Sort.Direction direction = dirValue.equals("desc") ? Direction.DESC : Direction.ASC;
                    orders.add(new Sort.Order(direction, propertyName));
                }

            });
        }

        return (Pageable)(orders.isEmpty() ? pageable : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders)));
    }

    public Pageable resolvePageableCamelToSnake(Map<String, String> requestParams, Pageable pageable) {
        Sort sorts = pageable.getSort();
        List<Sort.Order> orders = new ArrayList();
        if (sorts != null) {
            sorts.forEach((sort) -> {
                String propertyName = sort.getProperty();
                String prcString = camelToSnake(propertyName);
                String dirKey = propertyName + ".dir";
                if (requestParams.containsKey(dirKey)) {
                    String dirValue = (String)requestParams.get(dirKey);
                    Sort.Direction direction = dirValue.equals("desc") ? Direction.DESC : Direction.ASC;
                    orders.add(new Sort.Order(direction, prcString));
                }

            });
        }

        return (Pageable)(orders.isEmpty() ? pageable : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders)));
    }

    public static String camelToSnake(String str) {
        String result = "";
        char c = str.charAt(0);
        result = result + Character.toLowerCase(c);

        for(int i = 1; i < str.length(); ++i) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) {
                result = result + "_";
                result = result + Character.toLowerCase(ch);
            } else {
                result = result + ch;
            }
        }

        return result;
    }
}