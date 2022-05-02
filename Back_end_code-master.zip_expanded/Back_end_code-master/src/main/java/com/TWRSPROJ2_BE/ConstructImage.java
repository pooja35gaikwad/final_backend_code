package com.TWRSPROJ2_BE;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ConstructImage {
	int[][] PixelArray;
	private Graphics2D g2;

	public ConstructImage(Integer height, Integer width, int[][] matrix) {
		try {

//			bufferimage = ImageIO.read(new File("F:/DHANASHRI/Dhanashri Data/images2.jpg"));
			// int height = bufferimage.getHeight();
			// int width = bufferimage.getWidth();
			// PixelArray = new int[width][height];
//			for (int i = 0; i < width; i++) {
//				for (int j = 0; j < height; j++) {
//					PixelArray[i][j] = bufferimage.getRGB(i, j);
//				}
//			}
			PixelArray = matrix;
			/////// create Image from this PixelArray
			BufferedImage bufferImage2 = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int Pixel = PixelArray[x][y] << 16 | PixelArray[x][y] << 8 | PixelArray[x][y];
					bufferImage2.setRGB(x, y, Pixel);
				}

			}
			//g2 = (Graphics2D) bufferImage2.getGraphics();

			File outputfile = new File("F:/DHANASHRI/Dhanashri Data/aaa5.jpg");
			ImageIO.write(bufferImage2, "jpg", outputfile);
			return;

		} catch (Exception ee) {
			ee.printStackTrace();
		}
	}
}
