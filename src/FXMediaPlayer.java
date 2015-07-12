import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class FXMediaPlayer {

	private static double Volume = 1.0;
	static MediaPlayer player;
	static boolean isPaused = false;

	public static void play(String fn) {
		if (isPaused) {
			isPaused = false;
			player.play();
		} else {
			// Checks if file exists
			File song = new File(fn);
			if (!song.exists()) {
				System.out.println("Song doesn't exist! " + fn);
				return;
			}

			Media media = new Media(song.toURI().toString());
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
			
			player.setVolume(Volume);
			player.play();

		}
	}

	public static void stop() {
		if (player != null)
			player.stop();
	}

	public static void pause() {
		if (player != null) {
			player.pause();
			isPaused = true;
		}
	}

	public static void seek(double d) {
		if (player != null) {
			player.seek(Duration.seconds(d));
		}
	}

	public static void dispose() {
		if (player != null)
			player.dispose();
	}

	public static boolean isNull() {
		return player == null;
	}

	public static void setVolume(double d) {
		Volume = d;
		if (!isNull()) {
			player.setVolume(d);
		}
	}
}
