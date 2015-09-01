package com.gaoshin.downloader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {
    public static void main(String[] args) throws Exception {
        String base = args[0];
        
        int threads = 1;
        if(args.length>1)
            threads = Integer.parseInt(args[1]);
        
        int miniFileSize = 1;
        if(args.length>2)
            miniFileSize = Integer.parseInt(args[2]);
        
        new Downloader().run(base, threads, miniFileSize);
    }
    
    
    public void run(String base, int threads, int miniFileSize) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        final WebCache webCache = new WebCache(base, miniFileSize);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            String line = br.readLine();
            if(line == null)
                break;
            String[] items = line.split("\t");
            String url = items[0];
            String id = MD5.md5(url);
            if(items.length>0)
                id = items[1];
            executor.execute(new FetchJob(webCache, id, url));
        }
    }
    
    static class FetchJob implements Runnable {
        private String id;
        private String url;
        private WebCache fetcher;
        private int done;
        
        public FetchJob(WebCache fetcher, String id, String url) {
            this.fetcher = fetcher;
            this.id = id;
            this.url = url;
        }

        public void run() {
            try {
                fetcher.fetch(id, url);
                done = 1;
            } catch (Exception e) {
                done = -1;
            }
        }
    }
}
