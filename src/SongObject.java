import java.io.File;

public class SongObject {

	private String name;
	private String length;
	private File file;
	private String playlist;
	
	public SongObject(File file, String length, String playlist){
		String filePath = file.getAbsolutePath();
		this.file = new File(filePath.substring(0, filePath.lastIndexOf(".mp3")+4));
		if(file == null) return;
		this.length = length;
		this.playlist = playlist;
		this.name = file.getAbsolutePath();
		if(name.contains(".mp3")){
			this.name = name.substring(name.lastIndexOf("\\") + 1, name.lastIndexOf(".mp3"));
		}else{
			this.name = name;
		}
	}
	public SongObject(){
		this.name = "";
		this.length = "";
		this.file = null;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLength() {
		return length;
	}
	public void setLength(String length) {
		this.length = length;
	}
	
	public File getFile(){
		return file;
	}
	public void setFile(File file){
		this.file = file;
	}
	public String getPlaylist() {
		return playlist;
	}
	public void setPlaylist(String playlist) {
		this.playlist = playlist;
	}
	
	
}
