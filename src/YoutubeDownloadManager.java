import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;
import com.github.axet.vget.info.VideoInfo.States;
import com.github.axet.vget.vhs.VimeoInfo;
import com.github.axet.vget.vhs.YouTubeMPGParser;
import com.github.axet.vget.vhs.YouTubeQParser;
import com.github.axet.vget.vhs.YoutubeInfo;
import com.github.axet.vget.vhs.YoutubeInfo.YoutubeQuality;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.DownloadInfo.Part;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

public class YoutubeDownloadManager {

	static public final int SOURCEHEADER = 22000;// aproximate number of chars
													// to skip
	static public final String WEBSITEHEADER = "http://keepvid.com/?url=";
	static public final String YOUTUBEHEADER = "https://www.youtube.com/watch?v=";
	static public final String YOUTUBESHORTHEADER = "https://youtu.be/";

	private static YouTube youtube;
	static private final String APIKEY = "AIzaSyBMKWJqJfaRnaZP9KHdSFJJmqrXsrDOe9k";

	// VGet
	static private VideoInfo info;
	static private long last;

	// checks link and put to queue
	public static void parseLink(String s) {
		try {
			String videoId = null;
			String playlistId = null;
			
			//Extracts id's from link
			if (s.startsWith(YOUTUBEHEADER)) {
				String info = s.substring(YOUTUBEHEADER.length(), s.length());
				//If link contains playlist
				if (info.contains("&")) {
					String[] params = info.split("&");
					for(String p : params){
						if(p.startsWith("list=")) playlistId = p.substring(5, p.length());
					}
					
					videoId = params[0];
				}else{
					videoId = info;
				}
			} else if (s.startsWith(YOUTUBESHORTHEADER)) {
				String info = s.substring(YOUTUBESHORTHEADER.length(), s.length());
				//If link contains playlist
				if (info.contains("?")) {
					String[] params = info.split("\\?");
					for(String p : params){
						if(p.startsWith("list=")) playlistId = p.substring(5, p.length());
					}
					videoId = params[0];
				}else{
					videoId = info;
				}
			}
			
			if(videoId != null){
				YoutubeDownloaderUI.displayDownloadOptions(videoId, playlistId);
			}else{
				//Handle wrong link
				System.out.println("Wrong link");
				YoutubeDownloaderUI.WriteInfo("Wrong link");
			}
						
		} catch (IndexOutOfBoundsException e) {
			System.out.println("Wrong link");
			e.printStackTrace();
		}
		// downloadVideo(s);
		getVideosFromPlaylist("PLjNnP__MSqn3cARtTz189JPtPNSFcJKAH");
	}

	private static void downloadVideo(String link) {
		try {
			String source = getUrlSource(new URL(WEBSITEHEADER + link));
			if (source != null) {
				URL M4ALink = extractM4AFromSource(source);
				System.out.println("source link: " + M4ALink);
				try {
					saveUrl(new File("M:\\test.m4a"), M4ALink);
					System.out.println("Ended download");
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("Failed to get page source.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveUrl(final File file, final URL url) throws IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(url.openStream());
			fout = new FileOutputStream(file);

			final byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (fout != null) {
				fout.close();
			}
		}
	}

	private static URL extractM4AFromSource(String source) {
		URL link = null;
		int linkPlace = source.indexOf(" Download M4A ");
		if (linkPlace != -1) {
			String sub = source.substring(SOURCEHEADER, linkPlace);
			if (sub != null) {
				try {
					link = new URL(sub.substring(sub.lastIndexOf("href=\"") + 6, sub.lastIndexOf("\" class=")));
				} catch (MalformedURLException e) {
					System.out.println("Couldn't create download URL");
					e.printStackTrace();
				}
			} else {
				System.out.println("SOURCEHEADER is in wrong place or bad source");
			}

		} else {
			System.out.println("Bad website source! Can't find link place!");
			YoutubeDownloaderUI.WriteInfo("Bad website source! Can't find link place!");
		}

		if (link == null) {
			System.out.println("Couldn't create final dl link");
		}
		return link;
	}

	// Takes correct url and downloads to directory
	private static void download(URL url, File path) {
		try {
			AtomicBoolean stop = new AtomicBoolean(false);
			Runnable notify = new Runnable() {
				@Override
				public void run() {
					VideoInfo i1 = info;
					DownloadInfo i2 = i1.getInfo();

					// notify app or save download state
					// you can extract information from DownloadInfo info;
					switch (i1.getState()) {
					case EXTRACTING:
					case EXTRACTING_DONE:
					case DONE:
						if (i1 instanceof YoutubeInfo) {
							YoutubeInfo i = (YoutubeInfo) i1;
							System.out.println(i1.getState() + " " + i.getVideoQuality());
						} else if (i1 instanceof VimeoInfo) {
							VimeoInfo i = (VimeoInfo) i1;
							System.out.println(i1.getState() + " " + i.getVideoQuality());
						} else {
							System.out.println("downloading unknown quality");
						}
						break;
					case RETRYING:
						System.out.println(i1.getState() + " " + i1.getDelay());
						break;
					case DOWNLOADING:
						long now = System.currentTimeMillis();
						if (now - 1000 > last) {
							last = now;

							String parts = "";

							List<Part> pp = i2.getParts();
							if (pp != null) {
								// multipart download
								System.out.println("Parts: " + pp.size());
								for (Part p : pp) {
									if (p.getState().equals(States.DOWNLOADING)) {
										parts += String.format("Part#%d(%.2f) ", p.getNumber(),
												p.getCount() / (float) p.getLength());
									}
								}
							}

							System.out.println(String.format("%s %.2f %s", i1.getState(),
									i2.getCount() / (float) i2.getLength(), parts));
						}
						break;
					default:
						break;
					}
				}
			};

			// [OPTIONAL] limit maximum quality, or do not call this function if
			// you wish maximum quality available.
			//
			// if youtube does not have video with requested quality, program
			// will raise en exception.
			VGetParser user = null;

			// create proper html parser depends on url
			// user = VGet.parser(url);

			// download maximum video quality from youtube
			user = new YouTubeQParser(YoutubeQuality.p360);

			// download mp4 format only, fail if non exist
			// user = new YouTubeMPGParser();

			// create proper videoinfo to keep specific video information
			info = user.info(url);

			VGet v = new VGet(info, path);

			// [OPTIONAL] call v.extract() only if you d like to get video title
			// or download url link
			// before start download. or just skip it.
			// v.extract(user, stop, notify);

			System.out.println("Title: " + info.getTitle());
			// System.out.println("Download URL: " +
			// info.getInfo().getSource());

			v.download(user, stop, notify);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getUrlSource(URL url) throws IOException {
		URLConnection yc = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream(), "UTF-8"));
		String inputLine;
		StringBuilder a = new StringBuilder();
		while ((inputLine = in.readLine()) != null)
			a.append(inputLine);
		in.close();

		return a.toString();
	}

	private static List<YoutubeVideo> getVideosFromPlaylist(String playlistId) {
		ArrayList<YoutubeVideo> videoList = new ArrayList<YoutubeVideo>();
		try {
			if (youtube == null) {
				List<String> scopes = new ArrayList<String>();
				scopes.add("https://www.googleapis.com/auth/youtube.readonly");
				GoogleCredential credential = new GoogleCredential.Builder().setTransport(new NetHttpTransport())
						.setJsonFactory(new JacksonFactory()).build();

				youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
						.setApplicationName("playa-1042").build();
			}
			// Define a list to store items in the list of uploaded videos.
			List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

			// Retrieve the playlist of the channel's uploaded videos.
			YouTube.PlaylistItems.List playlistItemRequest = youtube.playlistItems().list("contentDetails,snippet");
			playlistItemRequest.setKey(APIKEY);
			playlistItemRequest.setPlaylistId(playlistId);

			// Only retrieve data used in this application, thereby making
			// the application more efficient. See:
			// https://developers.google.com/youtube/v3/getting-started#partial
			playlistItemRequest.setFields("items(contentDetails/videoId,snippet/title),nextPageToken");
			System.out.println(playlistItemRequest.getUriTemplate());
			String nextToken = "";

			// Call the API one or more times to retrieve all items in the
			// list. As long as the API response returns a nextPageToken,
			// there are still more items to retrieve.
			do {

				playlistItemRequest.setPageToken(nextToken);
				PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();
				playlistItemList.addAll(playlistItemResult.getItems());

				nextToken = playlistItemResult.getNextPageToken();
			} while (nextToken != null);

			Iterator<PlaylistItem> itemIterator = playlistItemList.iterator();

			while (itemIterator.hasNext()) {
				PlaylistItem playlistItem = itemIterator.next();
				videoList.add(new YoutubeVideo(playlistItem.getSnippet().getTitle(),
						playlistItem.getContentDetails().getVideoId()));
			}
			return videoList;
		} catch (GoogleJsonResponseException e) {
			e.printStackTrace();
			System.err.println(
					"There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
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
