package com.swt.helloworld;

import org.springframework.batch.core.annotation.OnSkipInProcess;
import org.springframework.batch.core.annotation.OnSkipInRead;
import org.springframework.batch.core.annotation.OnSkipInWrite;
import org.springframework.batch.item.file.FlatFileParseException;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class ProductListener {

    private String writeErrorFileNameRead = "error_items/read_skipped";
    private String writeErrorFileNameProceess = "error_items/process_skipped";
    private String writeErrorFileNameWrite = "error_items/write_skipped";

    @OnSkipInRead
    public void onSkipped(Throwable t) {
        if (t instanceof FlatFileParseException) {
            FlatFileParseException flag = (FlatFileParseException) t;
            onSkip(flag.getInput(), writeErrorFileNameRead);
        }
    }

    @OnSkipInProcess
    public void onSkippedProcess(Object item, Throwable t) {
        if (t instanceof RuntimeException) {
            System.out.println("ERROR_RUMTIME");
            onSkip(item, writeErrorFileNameProceess);
        }
    }

    @OnSkipInWrite
    public void onSkippedWrite(Object item, Throwable t) {
        if (t instanceof RuntimeException) {
            onSkip(item, writeErrorFileNameProceess);
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
