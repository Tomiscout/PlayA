import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.IMediaViewer;

public class DownloadThreadManager {
	static Vector<DownloadTask> downloadTasks = new Vector<DownloadTask>();
	static ArrayList<DownloadTask> deletionList = new ArrayList<DownloadTask>();
	public static boolean isRunning = false;
	static int maxThreads = 1;
	static int currentThreads = 0;
	static ArrayList<Thread> threadList = new ArrayList<Thread>();
	public static boolean fastDownload = false;
	private static int listIdCount = 0;

	public static void addToQueue(YoutubeVideo video, File dir) {
		if (dir.isDirectory()) {
			downloadTasks.add(new DownloadTask(video, dir));
			System.out.println("Adding new task:" + video.getVideoName());
		}
	}

	public static void addToQueue(YoutubeVideo video, File dir, int listPlaylistId) {
		if (dir.isDirectory()) {
			downloadTasks.add(new DownloadTask(video, dir, listPlaylistId));
			System.out.println("Adding new task:" + video.getVideoName());
		}
	}

	public static void addToQueue(String playlistId, File dir, boolean sync) {
		new Thread() {
			public void run() {
				System.out.println("Getting playlist videos from youtube...");

				int listPlaylistId = provideListItemId();
				YoutubeDownloaderUI.addListItem(dir.getName(), listPlaylistId, false);
				YoutubeDownloaderUI.refreshList();
				
				List<YoutubeVideo> vids = YtDownloadUtils.getVideosFromPlaylist(playlistId);

				YoutubeDownloaderUI.getListItem(listPlaylistId).getBar().setTotalWork(vids.size());
				
				for (YoutubeVideo v : vids) {
					addToQueue(v, dir, listPlaylistId);
				}
				YoutubeDownloaderUI.refreshList();
			}
		}.start();
	}

	public DownloadThreadManager() {
		startMainThread();
	}
	public static void stopThreads(){
		isRunning = false;
	}
	
	static class DownloadTask {
		private YoutubeVideo video;
		private File dir;
		private boolean downloading = false;
		private int listId;
		private int listPlaylistId = -1;
		private boolean complete = false;

		public DownloadTask(YoutubeVideo video, File dir) {
			this.video = video;
			this.dir = dir;
			listId = provideListItemId();
			YoutubeDownloaderUI.addListItem(video.getVideoName(), listId, false);
		}

		public DownloadTask(YoutubeVideo video, File dir, int listPlaylistId) {
			this.video = video;
			this.dir = dir;
			listId = provideListItemId();
			this.listPlaylistId = listPlaylistId;
			YoutubeDownloaderUI.addListItem(video.getVideoName(), listId, false);
		}

		public YoutubeVideo getVideo() {
			return video;
		}

		public File getDir() {
			return dir;
		}

		public boolean isDownloading() {
			return downloading;
		}

		public void setDownloading(boolean downloading) {
			this.downloading = downloading;
		}

		public boolean getDownloading() {
			return downloading;
		}

		public int getId() {
			return listId;
		}

		public boolean isPlaylistTask(){
			return listPlaylistId != -1;
		}
		public void complete() {
			complete = true;
			if (listPlaylistId > 0) {
				YoutubeDownloaderUI.getListItem(listPlaylistId).plusDownloaded();
			}
		}
		public int getListPlaylistId(){
			return listPlaylistId;
		}
	}

	// Main thread for getting
	public void startMainThread() {
		
		File dlFile = new File("M:\\test.m4a");
		IMediaReader mediaReader = ToolFactory.makeReader(dlFile.getAbsolutePath());
		// create a media writer
	    IMediaWriter mediaWriter = 
	           ToolFactory.makeWriter(FileUtils.truncateFileType(dlFile.getAbsolutePath())+".mp3", mediaReader);
	    
	 // add a writer to the reader, to create the output file
        mediaReader.addListener(mediaWriter);
        
        // create a media viewer with stats enabled
        IMediaViewer mediaViewer = ToolFactory.makeViewer(true);
        
        // add a viewer to the reader, to see the decoded media
        mediaReader.addListener(mediaViewer);
        System.out.println("Starting transcoding");
        // read and decode packets from the source file and
        // and dispatch decoded audio and video to the writer
        while (mediaReader.readPacket() == null) ;
        System.out.println("done transcoding");
        
		isRunning = true;
		new Thread() {
			public void run() {
				while (isRunning) {
					if (!deletionList.isEmpty()) {
						for (DownloadTask task : deletionList) {
							downloadTasks.remove(task);
						}
					}

					if (!downloadTasks.isEmpty()) {
						for (DownloadTask task : downloadTasks) {
							if (currentThreads <= maxThreads && currentThreads >= 0 && !task.getDownloading()) {
								task.setDownloading(true);
								startThread(task);
							}
						}
					}

					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	private void startThread(DownloadTask t) {
		t.setDownloading(true);
		Thread thread = new Thread(new DownloadThread(t));
		thread.start();
		threadList.add(thread);
		addCurrentThread(1);
	}

	class DownloadThread implements Runnable {
		private Thread t;
		private DownloadTask task;

		DownloadThread(DownloadTask task) {
			this.task = task;
		}

		public DownloadTask getDownloadTask() {
			return task;
		}

		public void run() {
			try {
				boolean badFirstSource = false; // false if first webpage is
												// down or can't parse video
				File dlFile;
				if (fastDownload == false) {
					String source = NetUtils.getUrlSource(new URL(YtDownloadUtils.KEEPVIDHEADER
							+ YtDownloadUtils.getYoutubeUrlFromId(task.getVideo().getVideoId())));
					if (source != null) {
						YtDownloadUtils.KeepVidSource sourceInfo = new YtDownloadUtils.KeepVidSource(source,
								task.getVideo().getVideoId());
						if (sourceInfo.getM4ALink() != null) {
							// TODO download M4A
							System.out.println("M4A: "+sourceInfo.getM4ALink());
							dlFile = new File(task.getDir().getAbsolutePath()+"\\"+task.getVideo().getVideoName()+".mp4");
							System.out.println("Dl file: "+dlFile.getAbsolutePath());
							
							NetUtils.saveUrl(dlFile, sourceInfo.getMP4Link());
							System.out.println("Downloaded");
							
							IMediaReader reader = ToolFactory.makeReader(dlFile.getAbsolutePath());
							// create a media writer
						    IMediaWriter mediaWriter = 
						           ToolFactory.makeWriter(FileUtils.truncateFileType(dlFile.getAbsolutePath())+".mp3", reader);

							System.out.println("converted");
							
						} else {
							badFirstSource = true;
						}
					} else {
						badFirstSource = true;
					}
				}

				// Use second mirror
				if (badFirstSource) {
					String source = NetUtils.getUrlSource(new URL(YtDownloadUtils.YTBINMP4HEADER
							+ YtDownloadUtils.getYoutubeUrlFromId(task.getVideo().getVideoId())));
					if (source != null) {
						YtDownloadUtils.YoutubeInMp4Source sourceInfo2 = new YtDownloadUtils.YoutubeInMp4Source(source);
						if (sourceInfo2.getMP4Link() != null) {
							// TODO download MP4
							System.out.println("Second mirror MP4: " + sourceInfo2.getMP4Link());
						} else {
							YoutubeDownloaderUI.WriteInfo("Can't download video. Both links down");
							System.out.println(
									"Both source parsers returned null. Probably of old age. (program too old)");
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(task.isPlaylistTask()){
				YoutubeDownloaderUI.getListItem(task.getListPlaylistId()).plusDownloaded();
			}
			deletionList.add(task);
			addCurrentThread(-1);
		}
	}

	private synchronized void addCurrentThread(int i) {
		currentThreads += i;
	}

	private synchronized void requiresDeletion(DownloadTask dt) {
		deletionList.add(dt);
		dt.complete();
	}

	static class YoutubeVideo {
		private String videoId = "";
		private String videoName = "";

		public YoutubeVideo(String name, String id) {
			videoName = name;
			videoId = id;
		}

		public String getVideoId() {
			return videoId;
		}

		public String getVideoName() {
			return videoName;
		}
	}

	private static int provideListItemId() {
		listIdCount++;
		return listIdCount;
	}
}
