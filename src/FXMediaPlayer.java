import java.io.File;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class FXMediaPlayer {

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

	public static void seekAt(int i) {
		if (player != null) {

		}
	}

	public static void dispose() {
		if (player != null)
			player.dispose();
	}

}
