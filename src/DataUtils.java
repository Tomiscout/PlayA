import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

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
}
