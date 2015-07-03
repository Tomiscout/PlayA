import java.util.ArrayList;
import java.util.Random;

public class PlaylistController {
	static ArrayList<String> currentSongs = new ArrayList<String>();
	static ArrayList<String> previousSongs = new ArrayList<>();
	static String currentSong;
	static String playingMode = "";

	public static void openPlaylist(String name) {

		String[] songs = PlaylistWriter.readPlaylist(name);

		currentSongs.clear();
		MainGui.data.clear();

		for (String s : songs) {
			String songName = s.substring(s.lastIndexOf("\\") + 1, s.lastIndexOf(".mp3"));
			int length = Integer.parseInt((s.substring(s.lastIndexOf(".mp3") + 5)));

			MainGui.data.add(new SongObject(songName, FileUtils.formatSeconds(length)));
			currentSongs.add(s.substring(0, s.lastIndexOf(".mp3") + 4));
		}

	}

	// Called when song ends.
	public static void playNextSong() {
		if (currentSongs.isEmpty())
			return;

		if (currentSong == null)
			return;

		if(playingMode.equals("Shuffle")){
			int randomSong;
			Random rand = new Random();
			int randomNum;
			boolean isSame=false;
				if(currentSongs.size()>5){
					if(!previousSongs.isEmpty())
						do{
							randomNum = rand.nextInt(currentSongs.size());
							
							int previousLoop=0;
							if(previousSongs.size()>4) previousLoop = previousSongs.size()-1;
							for(int i = previousSongs.size()-1; i>=previousLoop; i--){
								if(previousSongs.get(i).equals(currentSongs.get(randomNum))){
									isSame = true;
									break;
								}
							}
						}
						while(isSame);
					else{
						randomNum = rand.nextInt(currentSongs.size());
					}
					String song = currentSongs.get(randomNum);
					previousSongs.add(song);	
					playSongFilename(song);
				}else{
					
				}
			
		}
		else
		{
			for (int i = 0; i < currentSongs.size(); i++) {
				if (currentSongs.get(i).equals(currentSong)) {
					if (i == currentSongs.size() - 1) {
						previousSongs.add(currentSong);
						playSongFilename(currentSongs.get(0));
						return;
					} else {
						previousSongs.add(currentSong);
						playSongFilename(currentSongs.get(i + 1));
						return;
					}
				}
			}
			playSongFilename(currentSongs.get(0));
		}
	}

	public static void playPreviousSong() {
		if (previousSongs.isEmpty())
			return;

		int prevSong = previousSongs.size() - 1;
		currentSong = previousSongs.get(prevSong);
		previousSongs.remove(prevSong);
		PlayerController.play(currentSong);
	}

	public static void playSongFilename(String fn) {
		PlayerController.play(fn);
		currentSong = fn;
		String name = fn.substring(fn.lastIndexOf("\\") + 1, fn.lastIndexOf(".mp3"));
		MainGui.songLabel.setText(name);
	}

	public static String getSongFilepath(String name) {
		for (String s : currentSongs) {
			String truncated = s.substring(0, s.lastIndexOf(".mp3"));
			if (truncated.endsWith(name))
				return s;
		}
		return null;
	}

	public static void setPlayingMode(String s) {
		playingMode = s;
	}

}
