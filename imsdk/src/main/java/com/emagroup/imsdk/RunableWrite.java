package com.emagroup.imsdk;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;


public class RunableWrite implements Runnable {

    private BufferedWriter mSocketWriter;
    private String msg;
    private boolean isChanged=false;


    RunableWrite(OutputStream os) {
        mSocketWriter = new BufferedWriter(new OutputStreamWriter(os));

    }

    public void run() {
        try {
            while (true) {
                if (isChanged) {
                    mSocketWriter.write(msg);
                    mSocketWriter.flush();
                    isChanged=false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void putStrIntoSocket(String string) {
        if (string != null) {
            this.msg = string;
            isChanged=true;
        }
    }

}
