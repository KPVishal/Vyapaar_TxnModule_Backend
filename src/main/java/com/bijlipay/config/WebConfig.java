package com.bijlipay.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EnableWebMvc
@Configuration
@ComponentScan("com.bijlipay")
public class WebConfig extends WebMvcConfigurerAdapter {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
        resolver.setOneIndexedParameters(true);
        argumentResolvers.add(resolver);
        super.addArgumentResolvers(argumentResolvers);
    }

    // Custom resolver method for pageable sort direction (.dir) support
    public Pageable resolvePageable(Map<String, String> requestParams, Pageable pageable) {
        List<Order> orders = new ArrayList<>();
        Sort sorts = pageable.getSort();
        if (sorts != null) {
            sorts.forEach((sort) -> {
                String propertyName = sort.getProperty();
                String dirKey = propertyName + ".dir";
                if (requestParams.containsKey(dirKey)) {
                    String dirValue = requestParams.get(dirKey);
                    Direction direction = dirValue.equals("desc") ? Direction.DESC : Direction.ASC;
                    orders.add(new Order(direction, propertyName));
                }
            });
        }
        return orders.isEmpty() ? pageable : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    public Pageable resolvePageableCamelToSnake(Map<String, String> requestParams, Pageable pageable) {
        Sort sorts = pageable.getSort();

        List<Sort.Order> orders = new ArrayList<>();
        if (sorts != null) {
            sorts.forEach(sort -> {
                String propertyName = sort.getProperty();
                String prcString = camelToSnake(propertyName);
                String dirKey = propertyName + ".dir";
                if (requestParams.containsKey(dirKey)) {
                    String dirValue = requestParams.get(dirKey);
//                    Direction direction = dirValue.equals("desc") ? Direction.DESC : Direction.ASC;
                    Sort.Direction direction = dirValue.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    orders.add(new Sort.Order(direction, prcString));
                }
            });
        }
        return orders.isEmpty() ? pageable : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    public static String camelToSnake(String str) {
        String result = "";
        // Append first character(in lower case) to result string
        char c = str.charAt(0);
        result = result + Character.toLowerCase(c);
        // Traverse the string from ist index to last index
        for (int i = 1; i < str.length(); i++) {
            char ch = str.charAt(i);
            // Check if the character is upper case then append '_' and such character (in lower case) to result string
            if (Character.isUpperCase(ch)) {
                result = result + '_';
                result = result + Character.toLowerCase(ch);
            }
            // If the character is lower case then add such character into result string
            else {
                result = result + ch;
            }
        }
        // return the result
        return result;
    }
}