package image_processing;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ColorDetection {

	private String name;
	private BufferedImage image;
	private int[][] sobel;
	private ColorWheel colorWheel;
	private Color mainColor;

	public ColorDetection(String filename) {
		name = filename;

		try {
			image = ImageIO.read(new File("src/main/java/image_processing/photos/" + name + ".jpg"));
		}catch (IOException e) {
			e.printStackTrace();
		}

		setSobel(image);

		mainColor = determineColor();
		System.out.println("Main color determined.");
		colorWheel = new ColorWheel(mainColor);
	}

	public ColorDetection(BufferedImage im) {
		image = im;

		setSobel(image);

		mainColor = determineColor();
		System.out.println("Main color determined.");
		colorWheel = new ColorWheel(mainColor);
	}

	private void setSobel(BufferedImage image) {
		Sobel s = new Sobel();
		s.process(image);
		sobel = s.doubleToInt();
	}

	private Color determineColor() {
		boolean flag = false;
		boolean done = false;
		ArrayList<Color> colors = new ArrayList<>();
		int s;
		Color c2, c3;
		for (int i = image.getHeight()/5; i < image.getHeight()/5*4; i++) {
			for (int j = 20; j < image.getWidth() - 20; j++) {
				try {
					s = sobel[i][j];
					if (flag && s < 100) {
						c2 = new Color(image.getRGB(j, j));
						if (sameColor(c2, colors.get(0))) {
							colors.add(c2);
						}
					} else if (!flag && s > 150) {
						c2 = new Color(image.getRGB(j - 20, i));
						c3 = new Color(image.getRGB(j + 20, i));
						if (!sameColor(c2, c3)) {
							Point p = moreStableColor(new Point(j - 20, i), new Point(j + 20, i));
							colors.add(new Color(image.getRGB(p.x, p.y)));
							j = p.x;
							flag = true;
						}
					} else if (flag && s > 150 && colors.size() > 1) {
						c2 = new Color(image.getRGB(j - 20, i));
						c3 = new Color(image.getRGB(j + 20, i));
						if (!sameColor(c2, c3)) {
							done = true;
							break;
						}
					}
				}catch (ArrayIndexOutOfBoundsException e) {
					System.err.println(j + " " + i);
				}
			}
			if (done) break;
		}

		/*try {
			ImageIO.write(sobel, "png", new File("photos/" + name + "_.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		return averageColor(colors);
	}

	private static boolean sameColor(Color c1, Color c2) {
		float hsb1[] = new float[3];
		Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), hsb1);
		float hsb2[] = new float[3];
		Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), hsb2);
		if (Math.abs(ColorWheel.defineArea(c1) - ColorWheel.defineArea(c2)) == 0)
			return Math.abs(hsb1[1] - hsb2[1]) < 0.5f || Math.abs(hsb1[2] - hsb2[2]) < 0.5f;
		else if (Math.abs(ColorWheel.defineArea(c1) - ColorWheel.defineArea(c2)) < 2)
			return Math.abs(hsb1[1] - hsb2[1]) < 0.3f && Math.abs(hsb1[2] - hsb2[2]) < 0.3f;
		else
			return false;
	}

	/**
	 * Says if color on the photo matches generated color.
	 *
	 * @param found - complementary color or one of adjacent colors
	 * @param photo - main color on the photo of potential item
	 * @return
	 */
	public static boolean colorFits(int found, int photo) {
		Color foundColor = new Color(found);
		Color photoColor = new Color(photo);

		return sameColor(foundColor, photoColor);
	}

	private Color averageColor(ArrayList<Color> colors) {
		int r = 0, g = 0, b = 0;
		for (int i=0; i<colors.size(); i++) {
			r += colors.get(i).getRed();
			g += colors.get(i).getGreen();
			b += colors.get(i).getBlue();
		}
		r /= colors.size();
		g /= colors.size();
		b /= colors.size();
		return new Color(r, g, b);
	}

	private Point moreStableColor(Point l, Point r) {
		if (l.x < image.getWidth()/4)
			return r;
		else if (r.x > image.getWidth()/4*3)
			return l;
		else {
			Color lc = new Color(image.getRGB(l.x, l.y));
			Color rc = new Color(image.getRGB(r.x, r.y));
			int lw = 0, rw = 0;
			for (int i=1; i<Math.min(l.x, image.getWidth()-r.x)-10; i++) {
				if (sameColor(new Color(image.getRGB(l.x-i, l.y)), lc))
					++lw;
				if (sameColor(new Color(image.getRGB(r.x+i, r.y)), rc))
					++rw;
			}
			if (lw>rw)
				return l;
			else
				return r;
		}
	}

	public ArrayList<Color> getComplementaryColors() {
		return colorWheel.complementaryColors();
	}

	public ArrayList<Color> getAdjacentColors() {
		return colorWheel.adjacentColors();
	}

	public Color getMainColor() {
		return mainColor;
	}

	private void showColorCombination() {
		colorWheel.displayResult("src/main/java/image_processing/photos/" + name + "_colors.png");
		System.out.println("Color combinations generated.");
	}

	public static void main(String[] args) {
		String photo = "jeans";
		// file to read = "src/main/java/image_processing/photos/" + photo + ".jpg";
		ColorDetection cd1 = new ColorDetection(photo);
		ColorDetection cd2 = new ColorDetection("shirt");
		boolean fits = ColorDetection.colorFits(cd1.getComplementaryColors().get(2).getRGB(), cd2.getComplementaryColors().get(0).getRGB());
		System.out.println(fits);
		//cd1.getComplementaryColors().get(1).getRGB();
		cd1.showColorCombination();
		cd2.showColorCombination();
	}
}
