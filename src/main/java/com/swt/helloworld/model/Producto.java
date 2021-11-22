package com.swt.helloworld.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product", schema = "spring_batch_lab_dev")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long productId;

    @Column(name = "product_name")
    private String prodName;
    @Column(name = "product_desc")
    private String productDesc;
    private Integer unit;
    private BigDecimal price;

    public Producto() {

    }

    public Producto(Long productId, String prodName, String productDesc, Integer unit, BigDecimal price) {
        this.productId = productId;
        this.prodName = prodName;
        this.productDesc = productDesc;
        this.unit = unit;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public Integer getUnit() {
        return unit;
    }

    public void setUnit(Integer unit) {
        this.unit = unit;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "productId=" + productId +
                ", prodName='" + prodName + '\'' +
                ", productDesc='" + productDesc + '\'' +
                ", unit=" + unit +
                ", price=" + price +
                '}';
    }
}

