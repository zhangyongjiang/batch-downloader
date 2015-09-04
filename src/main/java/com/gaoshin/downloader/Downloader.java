package com.gaoshin.downloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Downloader {
    public static void main(String[] args) throws Exception {
//        args = new String[]{"/tmp", "1", "###"};
        String base = args[0];
        
        int threads = 1;
        if(args.length>1)
            threads = Integer.parseInt(args[1]);
        
        String ext = null;
        if(args.length>2)
            ext = args[2];
        
        new Downloader().run(base, threads, ext);
    }

    private int totalJobs = 0;
    private int finishedJobs = 0;
    
    public void run(String base, int threads, String ext) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        final WebCache webCache = new WebCache(base, 1);
        webCache.setExt(ext);
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int delay = delayMilliSecondsFromEnv();
        while(true) {
            String line = br.readLine();
            if(line == null)
                break;
            String[] items = line.split("\t");
            String url = null;
            int i = 0;
            for(; i<items.length; i++) {
                String s = items[i];
                if(s.startsWith("http:") || s.startsWith("https:")) {
                    url = s;
                    break;
                }
            }
            String id = MD5.md5(url);
            if(items.length>0) {
                id = items[i==0 ? 1 : 0];
            }
            
            
            FetchJob fetchJob = new FetchJob(webCache, id, url);
            if(fetchJob.exists()) {
                System.err.println("skip " + url);
                continue;
            }
            executor.execute(fetchJob);
            totalJobs++;
            if(delay > 0)
                Thread.sleep(delay);
        }
        
        while(totalJobs > finishedJobs) {
            System.err.println("Total jobs: " + totalJobs + ", finished jobs: " + finishedJobs + ", zero len files: " + webCache.getZeroLenFileCnt());
            Thread.sleep(3000);
        }
        
        executor.shutdown();
    }
    
    class FetchJob implements Runnable {
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
            synchronized (Downloader.this) {
                finishedJobs ++;
            }
        }
        
        public boolean exists() {
            File file = fetcher.getFile(id, url);
            if(file.exists() && file.length() > 0) {
                return true;
            }
            return false;
        }
    }
    
    public static int delayMilliSecondsFromEnv() {
        Map<String, String> env = System.getenv();
        String value = env.get("delayMilliSeconds");
        if(value == null)
            return 0;
        else
            return Integer.parseInt(value);
    }
    
}
