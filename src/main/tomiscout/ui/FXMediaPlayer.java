package main.tomiscout.ui;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import main.tomiscout.playlistControl.PlaylistController;
import main.tomiscout.utils.DataUtils;
import main.tomiscout.utils.FileUtils;

public class FXMediaPlayer {

	private static double Volume = 1.0;
	static MediaPlayer player;
	static boolean isPaused = false;
	static File currentSong;

	public static void play(File song, boolean forced) {
		if (isPaused && !forced) {
			isPaused = false;
			player.play();
			MainGui.changePlayImage(false);
			return;
		}
		// Checks if file exists
		if (song != null && !song.exists()) {
			System.out.println("Song doesn't exist! " + song.getAbsolutePath());
			return;
		}
		currentSong = song;

		String filePath = null;
		try {
			// Encoding filename to UTF-8, doesn't support folders with
			// UTF-8 characters
			filePath = song.getParentFile().toURI().toString()
					+ URLEncoder.encode(song.getName(), "UTF-8").replace("+", "%20");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Media media = new Media(filePath);

		if (player != null)
			player.dispose();
		player = new MediaPlayer(media);
		player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
			public void changed(ObservableValue<? extends Duration> observable, Duration duration,
					Duration currentDuration) {
				MainGui.setSeekValue(currentDuration.toSeconds());
			}

		});
		player.setOnReady(() -> {
			MainGui.seekBar.setMax(player.getTotalDuration().toSeconds());
		});
		player.setOnEndOfMedia(() -> {
			PlaylistController.playNextSong();
		});
	
		MainGui.setAlbumArt(DataUtils.getCoverArt(song));
		MainGui.setSongLengthLabel(FileUtils.getSongLength(song));

		MainGui.scrollToFile(song);
		MainGui.setSongName(FileUtils.truncateFileType(song.getName()));
		MainGui.changePlayImage(false);
		// MainGui.setAlbumArt();
		player.setVolume(Volume);
		player.play();

	}

	public static void stop() {
		if (player != null)
			player.stop();
	}

	public static void pause() {
		if (player != null) {
			player.pause();
			isPaused = true;
			MainGui.changePlayImage(true);
		}
	}

	// toggles between play or pause
	public static void togglePlay() {
		if (isPaused) {
			play(null, false);
		} else
			pause();
	}

	public static void seek(double d) {
		if (player != null) {
			player.seek(Duration.seconds(d));
		}
	}

	public static void dispose() {
		if (player != null)
			player.dispose();
		player = null;
	}

	public static boolean isNull() {
		return player == null;
	}

	public static void setVolume(double d) {
		if (!isNull()) {
			double vol = d*(2.0/3)+(1.0/3)-0.001;
			Volume = Math.exp(6.908*vol)/1000;
			player.setVolume(Volume);
		}
	}

	public static File getCurrentSong() {
		return currentSong;
	}
}
