package image_processing;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ColorWheel {
	private final static int COLOR_N = 12;
	private final static float SCALE = 0.08333333f;
	private final static float colorSwitch[] = {11.6667f, 0.6667f, 1.333f, 2.f, 2.8667f, 3.5f, 4.6667f, 5.8333f, 7.f, 8.5f, 9.3333f, 9.8333f};

	private final static Color WHITE = new Color(235, 235, 235);
	private final static Color BLACK = new Color(20, 20, 20);

	private Color mainColor;
	private float hsb[];
	private int area;
	private float wheel[];

	public ColorWheel(Color color) {
		this.mainColor = color;
		this.area = defineArea(mainColor);
	}

	public ArrayList<Color> complementaryColors() {
		ArrayList<Color> list = new ArrayList<>();
		if (wheel==null)
			calcSimpleWheel();
		list.add(mainColor);
		list.add(newColor(wheel[6]));
		list.add(WHITE);
		list.add(BLACK);
		return list;
	}

	public ArrayList<Color> adjacentColors() {
		ArrayList<Color> list = new ArrayList<>();
		if (wheel==null)
			calcSimpleWheel();
		list.add(mainColor);
		list.add(newColor(wheel[1]));
		list.add(newColor(wheel[11]));
		list.add(WHITE);
		list.add(BLACK);
		return list;
	}

	public ArrayList<Color> triadColors() {
		ArrayList<Color> list = new ArrayList<>();
		if (wheel==null)
			calcSimpleWheel();
		list.add(mainColor);
		list.add(newColor(wheel[5]));
		list.add(newColor(wheel[7]));
		list.add(WHITE);
		list.add(BLACK);
		return list;
	}

	public ArrayList<Color> tetradColors() {
		ArrayList<Color> list = new ArrayList<>();
		if (wheel==null)
			calcSimpleWheel();
		list.add(mainColor);
		list.add(newColor(wheel[3]));
		list.add(newColor(wheel[6]));
		list.add(newColor(wheel[9]));
		list.add(WHITE);
		list.add(BLACK);
		return list;
	}

	private ArrayList<Color> all() {
		ArrayList<Color> list = new ArrayList<>();
		list.add(mainColor);
		for (int i=1; i<12; i++) {
			list.add(newColor(wheel[i]));
		}
		list.add(WHITE);
		list.add(BLACK);
		return list;
	}

	private static float hue(Color color) {
		float[] hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		return hsb[0];
	}

	public static int defineArea(Color color) {
		float hsb[] = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		float roundH = hsb[0]/ SCALE;
		if ((roundH >= colorSwitch[0] && roundH < 12.0f) || (roundH >= 0.0f && roundH < colorSwitch[1]))
			return 0;
		if (roundH >= colorSwitch[1] && roundH < colorSwitch[2])
			return 1;
		if (roundH >= colorSwitch[2] && roundH < colorSwitch[3])
			return 2;
		if (roundH >= colorSwitch[3] && roundH < colorSwitch[4])
			return 3;
		if (roundH >= colorSwitch[4] && roundH < colorSwitch[5])
			return 4;
		if (roundH >= colorSwitch[5] && roundH < colorSwitch[6])
			return 5;
		if (roundH >= colorSwitch[6] && roundH < colorSwitch[7])
			return 6;
		if (roundH >= colorSwitch[7] && roundH < colorSwitch[8])
			return 7;
		if (roundH >= colorSwitch[8] && roundH < colorSwitch[9])
			return 8;
		if (roundH >= colorSwitch[9] && roundH < colorSwitch[10])
			return 9;
		if (roundH >= colorSwitch[10] && roundH < colorSwitch[11])
			return 10;
		if (roundH >= colorSwitch[11] && roundH < colorSwitch[0])
			return 11;
		return -1;
	}

	private float areaSize(int n) {
		if (n == 0)
			return (12.0f - colorSwitch[0] + colorSwitch[1]);
		else if (n != 11)
			return (colorSwitch[n+1]-colorSwitch[n]);
		else
			return (colorSwitch[0] - colorSwitch[11]);
	}

	private void calcSimpleWheel() {
		this.wheel = new float[12];
		this.hsb = new float[3];
		Color.RGBtoHSB(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), hsb);

		float delta;
		if (area!=11)
			delta = colorSwitch[area+1] - hsb[0]/SCALE;
		else
			delta = colorSwitch[0] - hsb[0]/SCALE;
		float areaSize = areaSize(area);

		wheel[0] = hsb[0];

		float times;
		for (int i = 1; i < COLOR_N; i++) {
			if (area+i+1 < 12) {
				times = areaSize(area+i)/areaSize;
				wheel[i] = (colorSwitch[area + 1 + i] - delta * times) * SCALE;
			} else if (area+i+1 == 12) {
				times = areaSize(11)/areaSize;
				wheel[i] = (colorSwitch[0] - delta * times) * SCALE;
			} else {
				times = areaSize(area+i-12)/areaSize;
				wheel[i] = (colorSwitch[area + 1 + i - 12] - delta * times) * SCALE;
			}
			//System.out.println(wheel[i]);
		}
	}

	private void calcAdvancedWheel() {
		this.wheel = new float[12];
		this.hsb = new float[3];
		Color.RGBtoHSB(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), hsb);

		if (hsb[1] > 0.6f || hsb[1] < 0.3f) {
			System.out.println(hsb[1]);
			hsb[1] = 0.6f;
		}
		if (hsb[2] > 0.6f || hsb[2] < 0.3f) {
			System.out.println(hsb[2]);
			hsb[2] = 0.6f;
		}

		float delta;
		if (area!=11)
			delta = colorSwitch[area+1] - hsb[0]/SCALE;
		else
			delta = colorSwitch[0] - hsb[0]/SCALE;
		float areaSize = areaSize(area);

		wheel[0] = hsb[0];

		float times;
		for (int i = 1; i < COLOR_N; i++) {
			if (area+i+1 < 12) {
				times = areaSize(area+i)/areaSize;
				wheel[i] = (colorSwitch[area + 1 + i] - delta * times) * SCALE;
			} else if (area+i+1 == 12) {
				times = areaSize(11)/areaSize;
				wheel[i] = (colorSwitch[0] - delta * times) * SCALE;
			} else {
				times = areaSize(area+i-12)/areaSize;
				wheel[i] = (colorSwitch[area + 1 + i - 12] - delta * times) * SCALE;
			}
			//System.out.println(wheel[i]);
		}

	}

	private Color newColor(float hue) {
		hsb[0] = hue;
		return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
	}

	public void displayResult(String name) {
		BufferedImage result = new BufferedImage(1100, 500, 2);
		int i=0, h=0;
		int offset = 20;
		int size = 70;

		//calcAdvancedWheel();

		for (Color c : complementaryColors()) {
			for (int j=0; j<size; j++) {
				for (int k=0; k<size; k++) {
					result.setRGB(offset+size*i+j, offset*(h+1)+size*h+k, c.getRGB());
				}
			}
			i++;
		}
		i=0;
		h++;
		for (Color c : adjacentColors()) {
			for (int j=0; j<size; j++) {
				for (int k=0; k<size; k++) {
					result.setRGB(offset+size*i+j, offset*(h+1)+size*h+k, c.getRGB());
				}
			}
			i++;
		}
		/*i=0;
		h++;
		for (Color c : triadColors()) {
			for (int j=0; j<size; j++) {
				for (int k=0; k<size; k++) {
					result.setRGB(offset+size*i+j, offset*(h+1)+size*h+k, c.getRGB());
				}
			}
			i++;
		}
		i=0;
		h++;
		for (Color c : tetradColors()) {
			for (int j=0; j<size; j++) {
				for (int k=0; k<size; k++) {
					result.setRGB(offset+size*i+j, offset*(h+1)+size*h+k, c.getRGB());
				}
			}
			i++;
		}*/
		i=0;
		h++;
		for (Color c : all()) {
			for (int j=0; j<size; j++) {
				for (int k=0; k<size; k++) {
					result.setRGB(offset+size*i+j, offset*(h+1)+size*h+k, c.getRGB());
				}
			}
			i++;
		}
		try {
			ImageIO.write(result, "png", new File(name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Color base = new Color(202, 190, 180);
		ColorWheel colorWheel = new ColorWheel(base);
		colorWheel.displayResult("comp-green.png");
	}
}
