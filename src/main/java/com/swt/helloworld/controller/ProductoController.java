package com.swt.helloworld.controller;

import com.swt.helloworld.ProductoRespository;
import com.swt.helloworld.model.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductoController {

    @Autowired
    private ProductoRespository productoRespository;

    @GetMapping
    public List<Producto> getProducto() {
        return productoRespository.findAll();
    }

}
