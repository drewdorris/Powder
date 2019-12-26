package com.ruinscraft.powder.util;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.model.particle.PositionedPowderParticle;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageUtil {

	public static List<PositionedPowderParticle> getRows(String name, int z,
			int yAdd, int xAdd, int resizedWidth, int resizedHeight) throws IOException {
		if (name.contains("/")) {
			return getRowsFromURL(name, z, yAdd, xAdd, resizedWidth, resizedHeight);
		} else {
			return getRowsFromPath(name, z, yAdd, xAdd, resizedWidth, resizedHeight);
		}
	}

	public static boolean imageExists(String fileName) {
		return PowderUtil.fileExists("/images", fileName);
	}

	// gets rows of a Layer from an image from a URL
	public static List<PositionedPowderParticle> getRowsFromURL(String urlName,
			int z, int yAdd, int xAdd, int resizedWidth, int resizedHeight) throws IOException {
		URL url = PowderUtil.readURL(urlName);
		List<PositionedPowderParticle> particles = new ArrayList<>();

		try {
			InputStream stream = PowderUtil.getInputStreamFromURL(url);
			if (stream == null) {
				throw new IOException("Error while attempting to read URL: " + url.toString());
			}
			BufferedImage bufferedImage = ImageIO.read(stream);
			particles.addAll(addToLayer(bufferedImage, z, yAdd, xAdd, resizedWidth, resizedHeight));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return particles;
	}

	// gets rows of a Layer from an image from a path in the data folder
	public static List<PositionedPowderParticle> getRowsFromPath(String fileName,
			int z, int yAdd, int xAdd, int resizedWidth, int resizedHeight) throws IOException {
		List<PositionedPowderParticle> particles = new ArrayList<>();

		try {
			File file = new File(PowderPlugin.get().getDataFolder() + "/images", fileName);
			if (!file.exists()) {
				PowderPlugin.get().saveResource("images/" + fileName, false);
			}
			BufferedImage bufferedImage;
			bufferedImage = ImageIO.read(file);
			if (bufferedImage == null) {
				throw new IOException("Error while attempting to read image: " + fileName);
			}
			particles.addAll(addToLayer(bufferedImage, z, yAdd, xAdd, resizedWidth, resizedHeight));
		} catch (IOException io) {
			io.printStackTrace();
		}

		return particles;
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
	public static List<PositionedPowderParticle> addToLayer(BufferedImage bufferedImage,
			int z, int yAdd, int xAdd, int resizedWidth, int resizedHeight) {
		BufferedImage newImage = getScaledImage(bufferedImage, resizedWidth, resizedHeight);

		List<PositionedPowderParticle> particles = new ArrayList<>();
		for (int y = 0; y < newImage.getHeight(); y++) {
			for (int x = 0; x < newImage.getWidth(); x++) {
				int pixel;
				try {
					pixel = newImage.getRGB(x, y);
				} catch (Exception e) {
					PowderPlugin.warning(
							"Error while processing pixel " + x + ";" + y + " in image!");
					continue;
				}
				// if pixel is transparent, make it null
				if ((pixel >> 24) == 0x00) {
					continue;
				}

				Color color = new Color(pixel);
				int arr = color.getRed();
				int gee = color.getGreen();
				int bee = color.getBlue();
				if (arr == 0) {
					arr = 1;
				}

				Object data = (Void) null;
				if (PowderPlugin.is1_13()) {
					data = new DustOptions(
							org.bukkit.Color.fromRGB(arr, gee, bee), 1F);
				}

				PositionedPowderParticle powderParticle = new PositionedPowderParticle(
						Particle.REDSTONE, 0, arr, gee, bee, data, x + xAdd, y + yAdd, z);
				particles.add(powderParticle);
			}
		}

		return particles;
	}

}
