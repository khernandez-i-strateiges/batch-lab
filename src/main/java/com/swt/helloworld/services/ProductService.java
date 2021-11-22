package com.swt.helloworld.services;

import com.swt.helloworld.model.Producto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductService {
    public List<Producto> getProducts() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8081/product";
        return Arrays.stream(restTemplate.getForObject(url, Producto[].class)).collect(Collectors.toList());
    }
}
