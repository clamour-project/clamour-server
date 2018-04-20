package image_processing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sobel {

	public double[][] gradient;

	public void process(String filename) {
		int[][] img = readImg(filename);
		int rows = img.length;
		int cols = img[0].length;

		double[][] Gx = new double[rows][cols];
		double[][] Gy = new double[rows][cols];
		double[][] grad = new double[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (i == 0 || i == rows - 1 || j == 0 || j == cols - 1) {
					Gx[i][j] = Gy[i][j] = grad[i][j] = 0;
				} else {
					Gx[i][j] = img[i+1][j-1] + 2 * img[i+1][j] + img[i+1][j+1]
							- img[i-1][j-1] - 2 * img[i-1][j] - img[i-1][j+1];
					Gy[i][j] = img[i-1][j+1] + 2 * img[i][j+1] + img[i+1][j+1]
							- img[i-1][j-1] - 2 * img[i][j-1] - img[i+1][j-1];

					grad[i][j] = Math.sqrt(Gx[i][j] * Gx[i][j] + Gy[i][j] * Gy[i][j]);
				}
			}
		}

		gradient = scaledGradient(grad);
	}

	private double[][] scaledGradient(double[][] m) {
		double[][] mag = new double[m.length][m[0].length];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				mag[i][j] = (m[i][j] * 255)/500.0;
			}
		}
		return mag;
	}

	private int[][] readImg(String filename) {
		try {
			BufferedImage bi = ImageIO.read(new File(filename));
			int[][] r = new int[bi.getHeight()][bi.getWidth()];
			int[][] g = new int[bi.getHeight()][bi.getWidth()];
			int[][] b = new int[bi.getHeight()][bi.getWidth()];
			for (int i = 0; i < r.length; ++i) {
				for (int j = 0; j < r[i].length; ++j) {
					r[i][j] = bi.getRGB(j, i) >> 16 & 0xFF;
					g[i][j] = bi.getRGB(j, i) >> 8 & 0xFF;
					b[i][j] = bi.getRGB(j, i) & 0xFF;
				}
			}
			return g;
		} catch (IOException e) {
			System.out.println("Sobel says: Image I/O error");
			return null;
		}
	}

	public int[][] doubleToInt() {
		int[][] res = new int[gradient.length][gradient[0].length];
		for (int i=0; i<gradient.length; i++) {
			for (int j=0; j<gradient[0].length; j++) {
				res[i][j] = (int) gradient[i][j];
			}
		}
		return res;
	}

	public BufferedImage writeImg(double[][] img) {
		BufferedImage bi = null;
		try {
			bi = new BufferedImage(img[0].length, img.length, BufferedImage.TYPE_INT_RGB);

			for (int i = 0; i < bi.getHeight(); ++i) {
				for (int j = 0; j < bi.getWidth(); ++j) {
					int val = (int) img[i][j];
					int pixel = (val << 16) | (val << 8) | (val);
					bi.setRGB(j, i, pixel);
				}
			}

			File outputfile = new File("photos/sobel.png");
			ImageIO.write(bi, "png", outputfile);
		} catch (IOException e) {
			System.out.println(e);
		}
		return bi;
	}
}