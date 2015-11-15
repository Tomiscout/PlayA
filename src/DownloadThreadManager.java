import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DownloadThreadManager {
	static Vector<DownloadTask> downloadTasks = new Vector<DownloadTask>();
	static ArrayList<DownloadTask> deletionList = new ArrayList<DownloadTask>();
	public static boolean isRunning = false;
	static int maxThreads = 2;
	static int currentThreads = 0;
	static ArrayList<Thread> threadList = new ArrayList<Thread>();
	public static boolean fastDownload = false;
	private static int listIdCount = 0;

	public static void setThreads(int i){
		maxThreads=i;
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

	public DownloadThreadManager() {
		startMainThread();
	}

	public static void stopThreads() {
		isRunning = false;
	}

	private void startThread(DownloadTask t) {
		t.setDownloading(true);
		Thread thread = new Thread(new DownloadThread(t));
		thread.start();
		threadList.add(thread);
		addCurrentThread(1);
	}

	class DownloadThread implements Runnable {
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
				File dlFile = null;
				URL dlLink = null;
				if (fastDownload == false) {
					String source = NetUtils.getUrlSource(new URL(YtDownloadUtils.KEEPVIDHEADER
							+ YtDownloadUtils.getYoutubeUrlFromId(task.getVideo().getVideoId())));
					if (source != null) {
						YtDownloadUtils.KeepVidSource sourceInfo = new YtDownloadUtils.KeepVidSource(source,
								task.getVideo().getVideoId());
						if (sourceInfo.getM4ALink() != null) {
							// TODO download M4A
							System.out.println("M4A: " + sourceInfo.getM4ALink());
							dlFile = new File(
									task.getDir().getAbsolutePath() + "\\" + task.getVideo().getVideoName() + ".m4a");
							System.out.println(
									"Dl file of " + task.getVideo().getVideoId() + ": " + dlFile.getAbsolutePath());
							dlLink = sourceInfo.getM4ALink();
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
							System.out.println("Both source parsers returned null. Program died of old age.");
						}
					} else
						System.out.println("Can't connect to second host.");
				}

				if (dlFile != null && dlLink != null) {
					saveUrlWithProgress(dlFile, dlLink, task.getId());
					System.out.println("Downloaded " + dlFile.getName());

					// If downloaded .mp4 file, convert
					if (dlFile.getAbsolutePath().toLowerCase().endsWith(".mp4")) {
						System.out.println(task.getVideo().getVideoName() + " needs converting to mp4");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (task.isPlaylistTask()) {
				YoutubeDownloaderUI.getListItem(task.getListPlaylistId()).plusDownloaded();
			}
			deletionList.add(task);
			addCurrentThread(-1);
		}
	}

	// Downloads file and shows progress in
	// YoutubeDownloaderUI.ProgressIndicatorBar by task id
	public static void saveUrlWithProgress(final File file, final URL url, int id) throws IOException {
		YoutubeDownloaderUI.ProgressIndicatorBar bar = YoutubeDownloaderUI.getListItem(id).getBar();

		InputStream is = null;
		FileOutputStream fout = null;
		try {
			URLConnection connection = url.openConnection();
			connection.connect();

			long totalSize = connection.getContentLengthLong();
			if (totalSize <= 0) {
				// Throw error
			}

			is = url.openStream();
			fout = new FileOutputStream(file);

			int bytesRead = 0;
			int totalBytesRead = 0;
			byte[] buffer = new byte[1024];

			int kilobytes = 0;
			long start = System.nanoTime();
			while ((bytesRead = is.read(buffer, 0, buffer.length)) > 0) {
				// TODO on program exit terminate download
				fout.write(buffer, 0, bytesRead);
				totalBytesRead += bytesRead;
				kilobytes++;

				double progress = (double) (totalBytesRead) / (double) (totalSize);
				bar.setProgress(progress);

				// if second elapsed
				long elapsedTime = System.nanoTime() - start;
				if ((elapsedTime / 1000000000.0) > 1) {
					start = System.nanoTime();
					String progressInfo = String.format("%d%% %d kB\\s", (int) (progress * 100), kilobytes);
					bar.setText(progressInfo);
					kilobytes = 0;
				}
			}
			// Sets progress to Completed
			YoutubeDownloaderUI.getListItem(id).setCompleted();
		} catch (FileNotFoundException fio) {
			fio.printStackTrace();
		} catch (SecurityException se) {
			se.printStackTrace();
		} finally {
			if (is != null) {
				is.close();
			}
			if (fout != null) {
				fout.close();
			}
		}

	}

	private synchronized void addCurrentThread(int i) {
		System.out.println("Current threads: " + i + " input: " + i);
		currentThreads += i;
	}

	public static void addToQueue(String playlistId, File dir, boolean merge) {
		new Thread() {
			public void run() {
				System.out.println("Getting playlist videos from youtube...");

				int listPlaylistId = provideListItemId();

				List<YoutubeVideo> vids = YtDownloadUtils.getVideosFromPlaylist(playlistId);

				System.out.println("Got "+vids.size()+" videos");
				//YoutubeDownloaderUI.getListItem(listPlaylistId).getBar().setTotalWork(vids.size());
				
				int existingSongs = 0;
				for (YoutubeVideo v : vids) {
					if (dir.isDirectory()) {
						// Merge feature
						if (merge && new File(dir.getAbsolutePath() + "\\" + v.getVideoName() + ".m4a").exists()) {
							existingSongs++;
						} else {
							downloadTasks.add(new DownloadTask(v, dir, listPlaylistId));
							System.out.println("Adding child task:" + v.getVideoName() + ", parent:" + listPlaylistId);
						}

					}
				}
				System.out.println("Existing songs: "+existingSongs);
				YoutubeDownloaderUI.refreshList();
			}
		}.start();
	}

	public static void addToQueueSingle(String videoId, File dir) {
		new Thread() {
			public void run() {
				String name = YtDownloadUtils.getVideoName(videoId);

				YoutubeVideo video = new YoutubeVideo(name, videoId);

				if (dir.isDirectory()) {
					downloadTasks.add(new DownloadTask(video, dir));
					System.out.println("Adding new task:" + video.getVideoName());
					YoutubeDownloaderUI.refreshList();
				}
			}
		}.start();
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
			YoutubeDownloaderUI.addListItem(video.getVideoName(), listId);
		}

		public DownloadTask(YoutubeVideo video, File dir, int listPlaylistId) {
			this.video = video;
			this.dir = dir;
			listId = provideListItemId();
			this.listPlaylistId = listPlaylistId;
			YoutubeDownloaderUI.addListItem(video.getVideoName(), listId);
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

		public boolean isPlaylistTask() {
			return listPlaylistId != -1;
		}

		public void complete() {
			complete = true;
			if (listPlaylistId > 0) {
				YoutubeDownloaderUI.getListItem(listPlaylistId).plusDownloaded();
			}else{
				YoutubeDownloaderUI.getListItem(listId).getBar().complete();
			}
		}

		public int getListPlaylistId() {
			return listPlaylistId;
		}
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
