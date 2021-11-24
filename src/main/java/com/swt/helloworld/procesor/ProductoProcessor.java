package com.swt.helloworld.procesor;

import com.swt.helloworld.model.Producto;
import org.springframework.batch.item.ItemProcessor;

import java.util.Locale;

public class ProductoProcessor implements ItemProcessor<Producto, Producto> {

    /**
     * this method ignore data where id == 2 and not have writen process
     *
     * @param item
     * @return
     * @throws Exception
     */
    @Override
    public Producto process(Producto item) throws Exception {
        if (item.getProductId() == 2) {
            throw  new RuntimeException("Because id == 2");
        } else {
            item.setProductDesc(item.getProductDesc().toUpperCase());
        }
        return item;
    }
}
