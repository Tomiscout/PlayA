import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

public class PlaylistController {
	static ArrayList<File> currentSongs = new ArrayList<File>();
	static ArrayList<File> previousSongs = new ArrayList<File>();
	static File currentSong;
	static private boolean isShuffle = true;
	static private boolean isRepeat = false;
	static File workingDir = PlaylistWriter.getWorkingDir();

	public static void openPlaylist(String name, boolean clearList) {
		String[] songs = PlaylistWriter.readPlaylist(name);
		
		if(clearList){
			currentSongs.clear();
			MainGui.table.getData().clear();
		}

		for (String song : songs) {
			PlaylistWriter.SongObject songObj = PlaylistWriter.parseSongObject(song);
			MainGui.table.getData().add(songObj);
			currentSongs.add(songObj.getFile());
		}

	}

	public static void openPlaylist(String[] names, boolean clearList) {
		if(clearList){
			currentSongs.clear();
			MainGui.table.getData().clear();
		}
		for (String name : names) {
			openPlaylist(name, false);
		}
	}

	// Called when song ends.
	public static void playNextSong() {
		if (currentSongs.isEmpty())
			return;

		if (currentSong == null)
			return;

		//Gives priority to repeating song
		if(isRepeat){
			playSong(currentSong, true);
		}else if (isShuffle) {
			Random rand = new Random();
			int randomNum;
			boolean isSame = false;
			if (currentSongs.size() > 5) {
				if (!previousSongs.isEmpty())
					do {
						randomNum = rand.nextInt(currentSongs.size());

						int previousLoop = 0;
						if (previousSongs.size() > 5)
							previousLoop = previousSongs.size();
						for (int i = previousSongs.size() - 1; i >= previousLoop; i--) {
							if (previousSongs.get(i).equals(currentSongs.get(randomNum))) {
								isSame = true;
								break;
							}
						}
						System.out.println(isSame);
					} while (isSame);
				else {
					randomNum = rand.nextInt(currentSongs.size());
				}
			} else {
				randomNum = rand.nextInt(currentSongs.size());
			}
			File song = currentSongs.get(randomNum);
			previousSongs.add(song);
			playSong(song, true);
		} else {
			for (int i = 0; i < currentSongs.size(); i++) {
				if (currentSongs.get(i).equals(currentSong)) {
					if (i == currentSongs.size() - 1) {
						previousSongs.add(currentSong);
						playSong(currentSongs.get(0), true);
						return;
					} else {
						previousSongs.add(currentSong);
						
						playSong(currentSongs.get(i + 1), true);
						return;
					}
				}
			}
			playSong(currentSongs.get(0), true);
		}
	}

	public static void playPreviousSong() {
		if (previousSongs.isEmpty())
			return;

		int prevSong = previousSongs.size() - 1;
		currentSong = previousSongs.get(prevSong);

		if (currentSong != null) {
			previousSongs.remove(prevSong);
			FXMediaPlayer.play(currentSong, true);
		}
	}

	public static void playSong(File file, boolean force) {
		FXMediaPlayer.play(file, force);
		if (currentSong != null)
			previousSongs.add(currentSong);
		currentSong = file;
	}

	public static boolean toggleShuffle() {
		isShuffle = !isShuffle;
		return isShuffle;
	}
	public static boolean toggleRepeat(){
		isRepeat = !isRepeat;
		return isRepeat;
	}

	public static void DeleteSelectedPlaylists() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Playlist deletion");
		alert.setHeaderText("Are you sure?");
		ObservableList<PlaylistObject> list = PlaylistPane.getCurrentSelectedItems();
		PlaylistObject[] listArray = list.toArray(new PlaylistObject[list.size()]);

		if (list != null) {
			alert.setContentText("Do you want to delete " + list.size() + " playlist(s)?");
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get() == ButtonType.OK) {
				for (PlaylistObject po : listArray) {
					String s = po.getName();
					// Makes correct filename for deletion
					String fName;
					if (s.endsWith(".plp")) {
						fName = workingDir + "\\" + s;
					} else {
						fName = workingDir + "\\" + s + ".plp";
					}

					File delFile = new File(fName);
					if (delFile.exists()) {
						delFile.delete();
						System.out.println("Deleted playlist: " + s);
					} else {
						System.out.println("Couldn't find playlist for deletion: " + delFile.getAbsolutePath());
					}
					PlaylistPane.removeItem(po);// Deleting from the table
				}

			} else {
				return;
			}

		}
	}

	public static void RenamePlaylist() {
		PlaylistObject selectedObj = PlaylistPane.getCurrentSelectedItem();
		if (selectedObj != null) {
			File playlistFile = new File(workingDir.getAbsolutePath() + "\\" + selectedObj.getName() + ".plp");
			if (!playlistFile.exists())
				return;

			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Playlist name");
			dialog.setContentText("Enter playlist name: ");

			// Gets input result and loops if filename is not allowed
			boolean nameLoop = true;
			Optional<String> result = null;
			while (nameLoop) {
				nameLoop = false;
				result = dialog.showAndWait();
				if (result.isPresent()) {
					if (!FileUtils.isNameCorrect(result.get())) {
						nameLoop = true;
					}
				}
			}

			if (result.isPresent()) {
				playlistFile.renameTo(new File(playlistFile.getParent() + "\\" + result.get() + ".plp"));
				PlaylistPane.reloadPlaylists();
			}
		}
	}

	public static void OpenSelectedPlaylists() {
		ObservableList<PlaylistObject> list = PlaylistPane.getCurrentSelectedItems();
		String[] playlistArray = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			playlistArray[i] = list.get(i).getName();
		}
		PlaylistController.openPlaylist(playlistArray, true);
	}

	public static void RescanSelectedPlaylists() {
		ObservableList<PlaylistObject> list = PlaylistPane.getCurrentSelectedItems();
		System.out.println("Selected items: "+list.size());
		for (PlaylistObject po : list) {
			System.out.println("Rescanning "+po.getName()+"...");
			PlaylistWriter.rescanPlaylist(po);
		}
	}
}
