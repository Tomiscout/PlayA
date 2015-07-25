public class PlayerController {

	public static void play(String fn) {	
		//If player is paused
		if(FXMediaPlayer.isPaused){
			FXMediaPlayer.play(""); //Sends empty string because it is ignored
			return;
		}
		
		FXMediaPlayer.dispose();
		FXMediaPlayer.play(fn);
	}

	public static void pause() {
		FXMediaPlayer.pause();
	}

	public static void stop() {
		FXMediaPlayer.stop();
	}
	
	public static void parseMediaKey(){
		if(FXMediaPlayer.isPaused){
			FXMediaPlayer.play("");
		}else{
			FXMediaPlayer.pause();
		}
	}
}
