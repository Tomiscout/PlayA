
public class SongObject {

	private String name;
	private String length;
	
	public SongObject(String name, String length){
		this.name = name;
		this.length = length;
	}
	public SongObject(){
		this.name = "";
		this.length = "";
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
	

	
	
}
