
public class PlaylistObject {

	private String name;
	private int songs;
	private String length;
	
	public PlaylistObject(String name, int songs, String length){
		this.name = name;
		this.songs = songs;
		this.length = length;
	}
	public PlaylistObject(){
		this.name = "";
		this.songs = 0;
		this.length = "";
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSongs() {
		return songs;
	}
	public void setSongs(int songs) {
		this.songs = songs;
	}
	public String getLength() {
		return length;
	}
	public void setLength(String length) {
		this.length = length;
	}

	
}
