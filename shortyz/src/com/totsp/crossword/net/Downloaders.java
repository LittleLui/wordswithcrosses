package com.totsp.crossword.net;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;

import com.totsp.crossword.BrowseActivity;
import com.totsp.crossword.PlayActivity;
import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Puzzle;
import com.totsp.crossword.puz.PuzzleMeta;


public class Downloaders {	
    private static final Logger LOG = Logger.getLogger("com.totsp.crossword");
    private List<Downloader> downloaders = new LinkedList<Downloader>();
    private Context context;
    private NotificationManager notificationManager;
    private boolean supressMessages;
    private NYTDownloader nyt = null;
    public Downloaders(SharedPreferences prefs,
        NotificationManager notificationManager, Context context) {
        this.notificationManager = notificationManager;
        this.context = context;

        if (prefs.getBoolean("downloadGlobe", true)) {
            downloaders.add(new BostonGlobeDownloader());
        }

        if (prefs.getBoolean("downloadThinks", true)) {
            downloaders.add(new ThinksDownloader());
        }

        if (prefs.getBoolean("downloadChron", true)) {
            downloaders.add(new ChronDownloader());
        }

        if (prefs.getBoolean("downloadWsj", true)) {
            downloaders.add(new WSJDownloader());
        }
        
        if (prefs.getBoolean("downloadWaPoPuzzler", true)) {
        	downloaders.add(new WaPoPuzzlerDownloader());
        }
        
        if (prefs.getBoolean("downloadNYTClassic", true)) {
        	downloaders.add(new NYTClassicDownloader());
        }

        if (prefs.getBoolean("downloadInkwell", true)) {
            downloaders.add(new InkwellDownloader());
        }
        
        if (prefs.getBoolean("downloadJonesin", true)) {
        	downloaders.add(new JonesinDownloader());
        }

        if (prefs.getBoolean("downloadLat", true)) {
            downloaders.add(new LATDownloader());
        }
        
        if (prefs.getBoolean("downloadAvClub", true)) {
            downloaders.add(new AVClubDownloader());
        }
        
        if (prefs.getBoolean("downloadPhilly", true)) {
            downloaders.add(new PhillyDownloader());
        }
        
        if (prefs.getBoolean("downloadCHE", true)) {
        	downloaders.add(new CHEDownloader());
        }
        
        if (prefs.getBoolean("downloadJoseph", true)) {
        	downloaders.add(new KFSDownloader("joseph", "Joseph Crosswords", 
        			"Thomas Joseph", Downloader.DATE_NO_SUNDAY));
        }
        
        if (prefs.getBoolean("downloadSheffer", true)) {
        	downloaders.add(new KFSDownloader("sheffer", "Sheffer Crosswords", 
        			"Eugene Sheffer", Downloader.DATE_NO_SUNDAY));
        }
        
        if (prefs.getBoolean("downloadPremier", true)) {
        	downloaders.add(new KFSDownloader("premier", "Premier Crosswords", 
        			"Frank Longo", Downloader.DATE_SUNDAY));
        }
        
        if (prefs.getBoolean("downloadNewsday", true)) {
        	downloaders.add(new UclickDownloader("crnet", "Newsday",
        			"Stanley Newman, distributed by Creators Syndicate, Inc.",
        			Downloader.DATE_DAILY));
        }
        
        if (prefs.getBoolean("downloadUSAToday", true)) {
        	downloaders.add(new UclickDownloader("usaon", "USA Today",
        			"USA Today", Downloader.DATE_NO_SUNDAY));
        }
        
        if (prefs.getBoolean("downloadUniversal", true)) {
        	downloaders.add(new UclickDownloader("fcx", "Universal Crossword",
        			"uclick LLC", Downloader.DATE_DAILY));
        }
        
        if (prefs.getBoolean("downloadLACal", true)) {
        	downloaders.add(new UclickDownloader("lacal", "LAT Sunday Calendar",
        			"Los Angeles Times",
        			Downloader.DATE_SUNDAY));
        }

        if (prefs.getBoolean("downloadNYT", false)) {
            downloaders.add(nyt = new NYTDownloader(prefs.getString("nytUsername", ""),
                    prefs.getString("nytPassword", "")));
        }
        this.supressMessages = prefs.getBoolean("supressMessages", false);
    }
    
    public List<Downloader> getDownloaders(Date date) {
    	int dayOfWeek = date.getDay();
    	List<Downloader> retVal = new LinkedList<Downloader>();
    	for (Downloader d : downloaders) {
    		if (Arrays.binarySearch(d.getDownloadDates(), dayOfWeek) >= 0) {
    			retVal.add(d);
    		}
    	}
    	return retVal;
    }
    
    public void download(Date date) {
    	download(date, getDownloaders(date));
    }

    public void download(Date date, List<Downloader> downloaders) {
    	Calendar cal = Calendar.getInstance();
    	Calendar now = Calendar.getInstance();
    	cal.setTime(date);
    	cal.set(Calendar.HOUR_OF_DAY, 0);
    	cal.set(Calendar.MINUTE, 0);
    	cal.set(Calendar.SECOND, 0);
    	cal.set(Calendar.MILLISECOND, 0);
    	
    	date = cal.getTime();
    	now.setTimeInMillis(System.currentTimeMillis());
    	
    	now.set(Calendar.MINUTE, 0);
    	now.set(Calendar.SECOND, 0);
    	now.set(Calendar.MILLISECOND, 0);
    	now.set(Calendar.HOUR_OF_DAY, 0);
        int i = 1;
        String contentTitle = "Downloading Puzzles";

        Notification not = new Notification(android.R.drawable.stat_sys_download,
                contentTitle, System.currentTimeMillis());
        boolean somethingDownloaded = false;
        File crosswords = new File(Environment.getExternalStorageDirectory(),
                "crosswords/");
        File archive =  new File(Environment.getExternalStorageDirectory(),
                "crosswords/archive/");
        
        for(File isDel : crosswords.listFiles()){
        	if(isDel.getName().endsWith(".tmp")){
        		isDel.delete();
        	}
        }
        
        boolean update = false;
        
        if (downloaders == null) {
        	// Download called from periodic updater - perform updates.
        	update = true;
        	downloaders = getDownloaders(date);
        }
        
        
        HashSet<File> newlyDownloaded = new HashSet<File>();
        for (Downloader d : downloaders) {
            String contentText = "Downloading from " + d.getName();
            Intent notificationIntent = new Intent(context, PlayActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                    notificationIntent, 0);
            not.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);

            if (this.notificationManager != null) {
                this.notificationManager.notify(0, not);
            }

            File downloaded = new File( crosswords, d.createFileName(date));
            File archived  = new File( archive, d.createFileName(date));

            System.out.println(downloaded.getAbsolutePath()+" "+downloaded.exists() + " OR "+archived.getAbsolutePath()+" "+archived.exists());
            if (downloaded.exists() || archived.exists()) {
                continue;
            } 

            downloaded = d.download(date);

            if (downloaded != null) {
            	boolean updatable = false;
            	if (d instanceof NYTDownloader && date.getTime() >= now.getTimeInMillis()) {
                	updatable = true;
                }
                if (processDownloadedPuzzle(downloaded, date, d.getName(), d.sourceUrl(date), updatable)) {
                    if(!this.supressMessages){
                    	this.postDownloadedNotification(i, d.getName(), downloaded);
                    }
                	newlyDownloaded.add(downloaded);
                	somethingDownloaded = true;
                }
            }

            i++;
        }

        if(update) {
	        ArrayList<File> checkUpdate = new ArrayList<File>();
	        try{
		        for(File file : crosswords.listFiles()){
		        	if(file.getName().endsWith(".shortyz") ){
		        		File puz = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.')+1) +"puz");
		        		System.out.println(puz.getAbsolutePath());
		        		if(!newlyDownloaded.contains(puz)){
		        			checkUpdate.add(puz);
		        		}
		        	}
		        }
		        archive.mkdirs();
		        
		        for(File file : archive.listFiles()){
		        	if(file.getName().endsWith(".shortyz") ){
		        		checkUpdate.add(new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.')+1) +"puz"));
		        	}
		        }
	        } catch(Exception e){
	        	e.printStackTrace();
	        }
	        
	        for(File file : checkUpdate){
	        	try{
	        	PuzzleMeta meta = IO.meta(file);
		        	if(meta != null && meta.updateable && nyt!= null && nyt.getName().equals(meta.source)){
		        		System.out.println("Trying update for "+file);
		        		File updated = nyt.update(file);
		        		if(updated != null){
		            		this.postUpdatedNotification(i, nyt.getName(), updated);
		        		}
		        	}
	        	} catch(IOException e){
	        		e.printStackTrace();
	        	}
	        }
        }
    
    	if (this.notificationManager != null) {
            this.notificationManager.cancel(0);
        }	
        	
        if( somethingDownloaded && this.supressMessages){
        	this.postDownloadedGeneral();
        }
    }
    
    public static boolean processDownloadedPuzzle(File downloaded, Date date, String source,
    		String sourceUrl, boolean updatable) {
    	try {
            Puzzle puz = IO.load(downloaded);
            puz.setDate(date);
            puz.setSource(source);
            puz.setSourceUrl(sourceUrl);
            puz.setUpdatable(updatable);
            
            IO.save(puz, downloaded);
            return true;
        } catch (Exception ioe) {
            LOG.log(Level.WARNING, "Exception reading " + downloaded,
                ioe);
            downloaded.delete();
            return false;
        }
    }

    private void postDownloadedNotification(int i, String name, File puzFile) {
        String contentTitle = "Downloaded " + name;
        Notification not = new Notification(android.R.drawable.stat_sys_download_done,
                contentTitle, System.currentTimeMillis());
        Intent notificationIntent = new Intent(Intent.ACTION_EDIT,
                Uri.fromFile(puzFile), context, PlayActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        not.setLatestEventInfo(context, contentTitle, puzFile.getName(),
            contentIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(i, not);
        }
    }
    
    private void postDownloadedGeneral() {
        String contentTitle = "Downloaded new puzzles!" ;
        Notification not = new Notification(android.R.drawable.stat_sys_download_done,
                contentTitle, System.currentTimeMillis());
        Intent notificationIntent = new Intent(Intent.ACTION_EDIT,
               null, context, BrowseActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        not.setLatestEventInfo(context, contentTitle, "New puzzles were downloaded.",
            contentIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(0, not);
        }
    }
    
    private void postUpdatedNotification(int i, String name, File puzFile) {
        String contentTitle = "Updated " + name;
        Notification not = new Notification(android.R.drawable.stat_sys_download_done,
                contentTitle, System.currentTimeMillis());
        Intent notificationIntent = new Intent(Intent.ACTION_EDIT,
                Uri.fromFile(puzFile), context, PlayActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        not.setLatestEventInfo(context, contentTitle, puzFile.getName(),
            contentIntent);

        if (this.notificationManager != null) {
            this.notificationManager.notify(i, not);
        }
    }
}
