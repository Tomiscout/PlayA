import java.io.File;

public class PlayerController {

	public static void play(File file) {	
		//If player is paused
		if(FXMediaPlayer.isPaused){
			FXMediaPlayer.play(null); //Sends empty string because it is ignored
			return;
		}
		
		FXMediaPlayer.dispose();
		FXMediaPlayer.play(file);
	}

	public static void pause() {
		FXMediaPlayer.pause();
	}

	public static void stop() {
		FXMediaPlayer.stop();
	}
	
	public static void parseMediaKey(){
		if(FXMediaPlayer.isPaused){
			FXMediaPlayer.play(null);
		}else{
			FXMediaPlayer.pause();
		}
	}
}
