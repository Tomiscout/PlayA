import java.util.ArrayList;

public class PlaylistController {
	static ArrayList<String> currentSongs = new ArrayList<String>();
	static String currentSong;

	public static void openPlaylist(String name) {
		
		String[] songs = PlaylistWriter.readPlaylist(name);

		currentSongs.clear();
		MainGui.data.clear();

		for (String s : songs) {
			String songName = s.substring(s.lastIndexOf("\\") + 1,
					s.lastIndexOf(".mp3"));
			int length = Integer
					.parseInt((s.substring(s.lastIndexOf(".mp3") + 5)));
			
			MainGui.data.add(new SongObject(songName , FileUtils.formatSeconds(length)));
			currentSongs.add(s.substring(0, s.lastIndexOf(".mp3") + 4));
		}

	}

	// Called when song ends.
	public static void playNextSong() {
		if (currentSongs.isEmpty())
			return;

		if (currentSong == null)
			return;

		for (int i = 0; i < currentSongs.size(); i++) {
			if (currentSongs.get(i).equals(currentSong)) {
				if (i == currentSongs.size() - 1) {
					playSongFilename(currentSongs.get(0));
					return;
				} else {
					playSongFilename(currentSongs.get(i + 1));
					return;
				}
			}
		}
		playSongFilename(currentSongs.get(0));
	}

	public static void playSongFilename(String fn) {
		System.out.println("Got request "+fn);
		PlayerController.play(fn);
		currentSong = fn;
	}
	
	public static String getSongFilepath(String name){
		for(String s : currentSongs){
			String truncated = s.substring(0, s.lastIndexOf(".mp3"));
			if(truncated.endsWith(name)) return s;
		}
		return null;
	}
}
