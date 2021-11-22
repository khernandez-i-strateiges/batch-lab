package com.swt.helloworld.reader;

import com.swt.helloworld.model.Producto;
import com.swt.helloworld.services.ProductService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductServiceAdapter implements InitializingBean {

    @Autowired
    private ProductService service;

    private List<Producto> productos;

    private Boolean flag = true;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public void getProducts() {
        this.productos = service.getProducts();
    }

    public Producto nextProduct() {
        if (flag) {
            getProducts();
        }
        flag = false;
        if (productos.size() > 0) {
            return productos.remove(0);
        } else {
            return null;
        }
    }

    public ProductService getService() {
        return service;
    }

    public void setService(ProductService service) {
        this.service = service;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
