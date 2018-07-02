package com.ruinscraft.powder.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Particle;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.particle.PowderParticle;

public class ImageUtil {

	public static List<List<PowderParticle>> getRows(List<List<PowderParticle>> rows, String name, 
			int resizedWidth, int resizedHeight) throws IOException {
		if (name.contains("/")) {
			return getRowsFromURL(rows, name, resizedWidth, resizedHeight);
		} else {
			return getRowsFromPath(rows, name, resizedWidth, resizedHeight);
		}
	}

	public static boolean imageExists(String fileName) {
		return PowderUtil.fileExists("/images", fileName);
	}

	// gets rows of a Layer from an image from a URL
	public static List<List<PowderParticle>> getRowsFromURL(List<List<PowderParticle>> rows,
			String urlName, int resizedWidth, int resizedHeight) throws IOException {
		URL url = PowderUtil.readURL(urlName);

		try {
			InputStream stream = PowderUtil.getInputStreamFromURL(url);
			if (stream == null) {
				throw new IOException("Error while attempting to read URL: " + url.toString());
			}
			BufferedImage bufferedImage = ImageIO.read(stream);
			addToLayer(rows, bufferedImage, resizedWidth, resizedHeight);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rows;
	}

	// gets rows of a Layer from an image from a path in the data folder
	public static List<List<PowderParticle>> getRowsFromPath(List<List<PowderParticle>> rows, 
			String fileName, int resizedWidth, int resizedHeight) throws IOException {
		try {
			File file = new File(PowderPlugin.getInstance().getDataFolder() + "/images", fileName);
			if (!file.exists()) {
				PowderPlugin.getInstance().saveResource("images/" + fileName, false);
			}
			BufferedImage bufferedImage;
			bufferedImage = ImageIO.read(file);
			if (bufferedImage == null) {
				throw new IOException("Error while attempting to read image: " + fileName);
			}
			addToLayer(rows, bufferedImage, resizedWidth, resizedHeight);
		} catch (IOException io) {
			io.printStackTrace();
		}

		return rows;
	}
	// scales an image to the given width/height (height is generally ignored)
	public static BufferedImage getScaledImage(BufferedImage bufferedImage, int width, int height) {
		int finalWidth = width;
		int finalHeight = height;
		double factor = 1.0;
		if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
			factor = ((double) bufferedImage.getHeight() / (double) bufferedImage.getWidth());
			finalHeight = (int) (finalWidth * factor);                
		} else {
			factor = ((double) bufferedImage.getWidth() / (double) bufferedImage.getHeight());
			finalWidth = (int) (finalHeight * factor);
		}   
		BufferedImage resizedImg = new BufferedImage(finalWidth, 
				finalHeight, BufferedImage.TRANSLUCENT);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(bufferedImage, 0, 0, finalWidth, finalHeight, null);
		g2.dispose();
		return resizedImg;
	}

	// creates PowderParticles from pixels and adds them to the rows
	// which are then added to the Layer
	public static List<List<PowderParticle>> addToLayer(List<List<PowderParticle>> rows, 
			BufferedImage bufferedImage, int resizedWidth, int resizedHeight) {
		BufferedImage newImage = getScaledImage(bufferedImage, resizedWidth, resizedHeight);

		for (int y = 0; y <= newImage.getHeight() - 1; y++) {
			List<PowderParticle> row = new ArrayList<>();
			for (int x = 0; x <= newImage.getWidth() - 1; x++) {
				int pixel;
				try {
					pixel = newImage.getRGB(x, y);
				} catch (Exception e) {
					PowderPlugin.getInstance().getLogger().warning(
							"Error while processing pixel " + x + ";" + y + " in image!");
					continue;
				}
				// if pixel is transparent, make it null
				if ((pixel >> 24) == 0x00) {
					row.add(new PowderParticle());
					continue;
				}
				Color color = new Color(pixel);
				int arr = color.getRed();
				int gee = color.getGreen();
				int bee = color.getBlue();
				if (arr == 0) {
					arr = 1;
				}
				PowderParticle powderParticle = new PowderParticle(
						Particle.REDSTONE, 0, arr, gee, bee, 1);
				row.add(powderParticle);
			}
			rows.add(row);
		}

		return rows;
	}

}
