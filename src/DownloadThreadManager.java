import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DownloadThreadManager {
	static Vector<DownloadTask> downloadTasks = new Vector<DownloadTask>();
	static ArrayList<DownloadTask> deletionList = new ArrayList<DownloadTask>();
	public static boolean isRunning = false;
	static int maxThreads = 4;
	static int currentThreads = 0;
	static ArrayList<Thread> threadList = new ArrayList<Thread>();
	public static boolean fastDownload = false;

	public static void addToQueue(YoutubeVideo video, File dir) {
		if (dir.isDirectory()) {
			downloadTasks.add(new DownloadTask(video, dir));
			System.out.println("Adding new task:" + video.getVideoName());
		}
	}

	public static void addToQueue(String playlistId, File dir, boolean sync) {
		new Thread() {
			public void run() {
				System.out.println("Getting playlist videos from youtube...");
				List<YoutubeVideo> vids = YtDownloadUtils.getVideosFromPlaylist(playlistId);
				for (YoutubeVideo v : vids) {
					addToQueue(v, dir);
				}
			}
		}.start();
	}

	public DownloadThreadManager() {
		startMainThread();
	}

	static class DownloadTask {
		private YoutubeVideo video;
		private File dir;
		private boolean downloading = false;

		public DownloadTask(YoutubeVideo video, File dir) {
			this.video = video;
			this.dir = dir;
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
	}

	// Main thread for getting
	public void startMainThread() {
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
							if (currentThreads < maxThreads && currentThreads >= 0 && !task.getDownloading()) {
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

	private synchronized void requiresDeletion(DownloadTask dt) {
		deletionList.add(dt);
	}

	private void startThread(DownloadTask t) {
		t.setDownloading(true);
		Thread thread = new Thread(new DownloadThread(t));
		thread.start();
		threadList.add(thread);
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

				if (fastDownload == false) {
					String source = NetUtils.getUrlSource(new URL(YtDownloadUtils.KEEPVIDHEADER
							+ YtDownloadUtils.getYoutubeUrlFromId(task.getVideo().getVideoId())));
					if (source != null) {
						YtDownloadUtils.KeepVidSource sourceInfo = new YtDownloadUtils.KeepVidSource(source);
						if (sourceInfo.getM4ALink() != null) {
							// TODO download M4A

						} else {
							badFirstSource = true;
						}
					} else {
						badFirstSource = true;
					}
				}

				// Use second mirror
				if (badFirstSource) {
					System.out.println("Using second mirror");
					String source = NetUtils.getUrlSource(new URL(YtDownloadUtils.YTBINMP4HEADER
							+ YtDownloadUtils.getYoutubeUrlFromId(task.getVideo().getVideoId())));
					if (source != null) {
						YtDownloadUtils.YoutubeInMp4Source sourceInfo2 = new YtDownloadUtils.YoutubeInMp4Source(source);
						if (sourceInfo2.getMP4Link() != null) {
							// TODO download MP4
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

			deletionList.add(task);
			currentThreads--;
		}
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
}
