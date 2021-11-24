package com.swt.helloworld.Clssifier;

import com.swt.helloworld.model.Producto;
import org.springframework.batch.support.annotation.Classifier;

import java.math.BigDecimal;

public class myClassifier {

    @Classifier
    public String identificarCancelados(Producto itemFile) {
        if (itemFile.getPrice().compareTo(new BigDecimal(55)) == 1) {
            return "C_APROBADO";
        } else {
            return "C_RECHAZADO";
        }

    }
}
