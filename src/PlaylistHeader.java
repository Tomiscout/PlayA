import java.io.File;

public class PlaylistHeader {
	private int length;
	private int type;
	private int songCount;
	private String name;
	private String url = "";

	
	public PlaylistHeader(String name, int type, int length, int songCount){
		this.name = name;
		this.type = type;
		this.length = length;
		this.songCount = songCount;
	}
	
	public PlaylistHeader(File playlist) {
		name = playlist.getName().substring(0, playlist.getName().length() - 4);
		String line = FileUtils.getFirstLine(playlist);
		if (line != null) {
			String[] properties = line.split(PlaylistWriter.SEPARATOR);
			for (String prop : properties) {
				String info = null;
				if (prop.startsWith(PlaylistWriter.LENGTHHEADER)) {
					info = prop.substring(PlaylistWriter.LENGTHHEADER.length());
					length = parseInt(info);

				} else if (prop.startsWith(PlaylistWriter.TYPEHEADER)) {
					info = prop.substring(PlaylistWriter.TYPEHEADER.length());
					type = parseInt(info);

				} else if (prop.startsWith(PlaylistWriter.SONGCOUNTHEADER)) {
					info = prop.substring(PlaylistWriter.TYPEHEADER.length()+1);
					songCount = parseInt(info);

				} else if (prop.startsWith(PlaylistWriter.URLHEADER)) {
					url = prop.substring(PlaylistWriter.URLHEADER.length());
				}
			}
		} else {
			System.out.println("Empty playlist file: " + name);
		}
	}

	public String toString(){
		String sep = PlaylistWriter.SEPARATOR;
		String header = sep;
		
		header += PlaylistWriter.TYPEHEADER + getType() + sep;
		header += PlaylistWriter.LENGTHHEADER + getLength() + sep;
		header += PlaylistWriter.SONGCOUNTHEADER + getSongCount() + sep;
		header += PlaylistWriter.URLHEADER + getUrl();
		return header;
	}
	
	public PlaylistObject getPlaylistObject() {
		return new PlaylistObject(getName(), getSongCount(), FileUtils.formatSeconds(getLength()));
	}

	public int getSongCount() {
		return songCount;
	}

	public int getLength() {
		return length;
	}

	public int getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public String getName() {
		return name;
	}

	private int parseInt(String i) {
		try {
			int t = Integer.parseInt(i);
			return t;
		} catch (NumberFormatException e) {
			System.out.println("Couldn't parse number \"" + i + "\" into integer.");
			return 0;
		}
	}

}
