package com._37coins.util;

import java.io.IOException;
import java.io.InputStream;

public class ResourceBundleInputStream extends InputStream {
    private final InputStream is;
    private int c = 0;
    private int prev = 0;
    
    public ResourceBundleInputStream(InputStream is){
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        if (c == 0){
            is.read();
            is.read();
            is.read();
            is.read();
            is.read();
            is.read();
            is.read();
            c = c + 7;
        }
        int i = is.read();
        c++;
        if (prev == 125 && i == 41)
            return -1;
        prev = i;
        return i;
    }

}
