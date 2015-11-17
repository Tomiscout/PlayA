import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.images.Artwork;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.FileTypeBox;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.UserDataBox;
import com.coremedia.iso.boxes.apple.AppleItemListBox;
import com.coremedia.iso.boxes.mdat.MediaDataBox;
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.DirectFileReadDataSource;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.boxes.apple.AppleCoverBox;

public class DataUtils {

	public static String formatSeconds(long s, boolean letters) {
		String string = "";

		if (s < 1)
			return "00:00";

		long remainder;
		long days = (int) s / 86400;
		long hours = (int) (s % 86400) / 3600;
		remainder = s - days * 86400 - hours * 3600;
		long mins = (int) remainder / 60;
		long secs = remainder % 60;

		if (days > 0) {
			string += days;
			if (letters)
				string += "d";
		}
		if (hours > 0) {
			string += hours;
			if (letters)
				string += "h ";
		}
		if (mins < 10)
			string += "0" + mins + ":";
		else
			string += mins + ":";

		if (secs < 10)
			string += "0" + secs;
		else
			string += secs;

		return string;
	}

	public static BufferedImage downloadImage(URL url) {
		try {
			InputStream in = new BufferedInputStream(url.openStream());
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}
			out.close();
			in.close();
			byte[] byteArray = out.toByteArray();
			ByteArrayInputStream inByte = new ByteArrayInputStream(byteArray);
			BufferedImage read = ImageIO.read(inByte);
			return read;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return null;
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public static BufferedImage getCoverArtFromMp3File(File f) {
		MP3File mp3;
		java.awt.Image cover = null;
		try {
			mp3 = (MP3File) AudioFileIO.read(f);
			AbstractID3v2Tag v24tag = mp3.getID3v2TagAsv24();

			Artwork artWork = null;
			if (v24tag != null)
				artWork = v24tag.getFirstArtwork();

			if (artWork != null) {
				cover = (java.awt.Image) artWork.getImage();
			} else {
				System.out.println("No cover art");
			}
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			e.printStackTrace();
		}

		if (cover != null) {
			return DataUtils.toBufferedImage(cover);
		}

		return null;
	}

	// Walks down mp4 structure to get cover art image
	public static BufferedImage getCoverArtFromMp4File(File f) {
		IsoFile isoFile = null;
		try {
			isoFile = new IsoFile(new DirectFileReadDataSource(f));
		} catch (IOException e) {
			e.printStackTrace();
		}

		MovieBox moov = (MovieBox) isoFile.getMovieBox();
		UserDataBox uData = null;
		if (moov != null) {
			List<Box> boxes = moov.getBoxes();
			// Finds UserDataBox
			for (Box b : boxes) {
				if (b instanceof UserDataBox)
					uData = (UserDataBox) b;
			}
		}
		AppleCoverBox cover = null; // CoverArt box
		if (uData != null) {
			MetaBox metaBox = (MetaBox) uData.getBoxes().get(0);
			if (metaBox != null) {
				AppleItemListBox ib = null;
				List<Box> appleBoxes = metaBox.getBoxes();

				// Finds AppleItemListBox from metadata boxes
				for (Box appleBox : appleBoxes) {
					if (appleBox instanceof AppleItemListBox) {
						ib = (AppleItemListBox) appleBox;
						break;
					}
				}

				if (ib != null) {
					List<Box> itemListBox = ib.getBoxes();
					// Finds AppleCoverBox from AppleItemListBox boxes
					for (Box b : itemListBox) {
						if (b instanceof AppleCoverBox) {
							cover = (AppleCoverBox) b;
							break;
						}
					}
				} else {
					System.out.println("No AppleItemListBox");
				}
			}
		}

		// If file contains coverArt
		if (cover != null) {
			byte[] imageData = cover.getCoverData();
			InputStream in = new ByteArrayInputStream(imageData);
			try {
				BufferedImage bi = ImageIO.read(in);
				return bi;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
