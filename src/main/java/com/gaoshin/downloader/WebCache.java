package com.gaoshin.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class WebCache {
    private String base;
    private int minFileSize;
    
    public WebCache(String base, int minFileSize) {
        this.base = base;
        this.minFileSize = minFileSize;
        File file = new File(base);
        if(!file.exists())
            file.mkdirs();
    }
    
    public WebCache(String base) {
        this(base, 1);
    }
    
    public InputStream getUrlStream(String id, String url) throws Exception {
        File file = fetch(id, url);
        return new FileInputStream(file);
    }
    
    public File fetch(String id, String url) throws Exception {
        if(id == null)
            id = MD5.md5(url);
        String subdir = null;
        if(id.length() < 3)
            subdir = id;
        else if(id.length() < 5)
            subdir = id.substring(0, 2) + "/" + id.substring(2);
        else
            subdir = id.substring(0, 2) + "/" + id.substring(2, 4);
        File file = new File(base + "/" + subdir);
        if(!file.exists())
            file.mkdirs();
        file = new File(base + "/" + subdir + "/" + id);
        if(file.exists() && file.length()>minFileSize) {
            System.err.println("web cache found for " + url);
            return file;
        }
        InputStream openStream = null;
        FileOutputStream fw = null;
        
        try {
            System.err.println("fetch content from " + url);
            openStream = new URL(url).openStream();
            byte[] buff = new byte[8192];
            fw = new FileOutputStream(file, false);
            while(true) {
                int len = openStream.read(buff);
                if(len < 0)
                    break;
                fw.write(buff, 0, len);
            }
        }
        finally {
            if(fw != null)
                fw.close();
            if(openStream != null)
                openStream.close();
        }
        
        return file;
    }
}
