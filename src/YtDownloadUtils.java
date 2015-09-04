import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

public class YtDownloadUtils {

	static public final String YTBINMP4HEADER = "http://youtubeinmp4.com/youtube.php?video=";
	static public final String KEEPVIDHEADER = "http://keepvid.com/?url=";
	static public final String YOUTUBEHEADER = "https://www.youtube.com/watch?v=";
	static public final String YOUTUBESHORTHEADER = "https://youtu.be/";

	private static YouTube youtube;
	static private final String APIKEY = "AIzaSyBMKWJqJfaRnaZP9KHdSFJJmqrXsrDOe9k";


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
		//getVideosFromPlaylist("PLjNnP__MSqn3cARtTz189JPtPNSFcJKAH");
	}
	
	public static URL getYoutubeUrlFromId(String videoId){
		URL link = null;
		try {
			link = new URL(YOUTUBEHEADER+videoId);
		} catch (MalformedURLException e) {
			System.out.println("Couldn't create Youtube URL");
			e.printStackTrace();
		}
		return link;
	}
	
	public static URL extractM4AFromSource(String source) {
		URL link = null;
		

		if (link == null) {
			System.out.println("Couldn't create final dl link");
		}
		return link;
	}

	public static List<DownloadThreadManager.YoutubeVideo> getVideosFromPlaylist(String playlistId) {
		ArrayList<DownloadThreadManager.YoutubeVideo> videoList = new ArrayList<DownloadThreadManager.YoutubeVideo>();
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
				videoList.add(new DownloadThreadManager.YoutubeVideo(playlistItem.getSnippet().getTitle(),
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

	//Backup website for mp4 files(vevo) http://youtubeinmp4.com/youtube.php?video=
	static class YoutubeInMp4Source{
		private URL MP4Link;
		
		public YoutubeInMp4Source(String source){
			int workableEnd = source.indexOf("\" class=\"downloadMP4");
			if(workableEnd == -1) return;
			String urlString = source.substring(source.indexOf("downloadMP4\" href=\"")+19, workableEnd);
			
			try{
				MP4Link = new URL("http://youtubeinmp4.com/"+urlString);
			}catch(MalformedURLException e){
				e.printStackTrace();
			}
		}
		
		public URL getMP4Link() {
			return MP4Link;
		}
	}
	
	//KeepVid 
	static class KeepVidSource{
		private URL M4ALink;
		private URL MP4Link;
		
		//Dirty way of getting source info
		public KeepVidSource(String source, String debug){
			int dlPlace = source.indexOf("<div id=\"dl\">");
			if(dlPlace == -1) return;
			String workableSource = source.substring(dlPlace);
			int workableEndPlace = workableSource.indexOf("<ul class=\"switches\">");
			if(workableEndPlace == -1) return;
			workableSource = workableSource.substring(0, workableEndPlace);

			try{
				int linkPlace = workableSource.indexOf(" Download MP4 ");
			//Gets MP4 link
				if (linkPlace != -1) {
					String sub = workableSource.substring(0, linkPlace);
					if (sub != null) {
						MP4Link = new URL(sub.substring(sub.lastIndexOf("href=\"") + 6, sub.lastIndexOf("\" class=")));
					} else {
						return;
					}

				} else {
					System.out.println("Bad website source! Can't find link place! || "+debug+" ||");
					return;
				}
				
			//Gets M4A link
				linkPlace = workableSource.indexOf(" Download M4A ");
				if (linkPlace != -1) {
					String sub = workableSource.substring(0, linkPlace);
					if (sub != null) {
						M4ALink = new URL(sub.substring(sub.lastIndexOf("href=\"") + 6, sub.lastIndexOf("\" class=")));
					} else {
						return;
					}

				} else {
					System.out.println("Bad website source! Can't find link place! || "+debug+" ||");
					return;
				}
			}catch(MalformedURLException e){
				System.out.println("Couldn't create download URL");
				e.printStackTrace();
			}
		}

		public URL getM4ALink() {
			return M4ALink;
		}

		public URL getMP4Link() {
			return MP4Link;
		}

	}
}
