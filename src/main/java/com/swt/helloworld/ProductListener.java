package com.swt.helloworld;

import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.item.file.FlatFileParseException;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class ProductListener {

    private String writeErrorFileName = "error_items/read_skipped";

    @OnSkipInRead
    public void onSkipped(Throwable t) {
        if (t instanceof FlatFileParseException) {
            FlatFileParseException flag = (FlatFileParseException) t;
            onSkip(flag.getInput(), writeErrorFileName);
        }
    }

    public void onSkip(Object o, String fileName) {
        try {
            FileOutputStream fos = null;
            fos = new FileOutputStream(fileName, true);
            fos.write(o.toString().getBytes());
            fos.write("\r\n".getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
