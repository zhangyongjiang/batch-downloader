package com.gaoshin.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class WebCache {
    private String base;
    private int minFileSize;
    private String ext;
    private int zeroLenFileCnt = 0;
    
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
    
    private String getFileExt(String url) {
        if(ext == null)
            return "";

        if(ext.equals("###")) {
            int pos = url.lastIndexOf("?");
            if(pos != -1)
                url = url.substring(0,  pos);
            if(url.lastIndexOf("/") > url.indexOf("//")+1) {            
                int pos0 = url.lastIndexOf("/");
                pos = url.lastIndexOf(".");
                if(pos != -1 && pos > pos0)
                    return url.substring(pos);
            }
            return "";
        }
        
        return "." + ext;
    }
    
    public File fetch(String id, String url) throws Exception {
        File file = getFile(id, url);
        if(file.exists() && file.length()>minFileSize) {
            System.err.println("web cache found for " + url);
            return file;
        }
        InputStream openStream = null;
        FileOutputStream fw = null;
        
        int totalBytes = 0;
        try {
            openStream = new URL(url).openStream();
            byte[] buff = new byte[8192];
            fw = new FileOutputStream(file, false);
            while(true) {
                int len = openStream.read(buff);
                if(len < 0)
                    break;
                totalBytes += len;
                fw.write(buff, 0, len);
            }
        }
        finally {
            if(fw != null)
                fw.close();
            if(openStream != null)
                openStream.close();
            System.err.println("fetch content from " + url + ". total bytes: " + totalBytes);
        }
        
        if(totalBytes == 0)
            setZeroLenFileCnt(getZeroLenFileCnt() + 1);
        
        return file;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public int getZeroLenFileCnt() {
        return zeroLenFileCnt;
    }

    public void setZeroLenFileCnt(int zeroLenFileCnt) {
        this.zeroLenFileCnt = zeroLenFileCnt;
    }

    public File getFile(String id, String url) {
        if(id == null)
            id = MD5.md5(url);
        id = id.trim();
        url = url.trim();
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
        file = new File(base + "/" + subdir + "/" + id + getFileExt(url));
        return file;
    }
}
