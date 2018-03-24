package com.ruinscraft.powder;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Particle;

import com.ruinscraft.powder.objects.ChangedParticle;

public class ImageUtil {

	public static List<Object> getMatrixFromURL(List<Object> matrix, URL url, int resizedWidth, int resizedHeight) throws IOException {
		
		PowderPlugin.getInstance().getServer().getScheduler().runTask(PowderPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {

				try {

					final HttpURLConnection httpConnection;
					httpConnection = (HttpURLConnection) url.openConnection();
					httpConnection.connect();
					int httpCode = httpConnection.getResponseCode();
					if (httpCode != 200) {
						throw new IOException("Error while attempting to connect to URL: " + url.toString());
					}

					InputStream stream = httpConnection.getInputStream();
					BufferedImage bufferedImage = ImageIO.read(stream);

					if (bufferedImage == null) {
						throw new IOException("Error while attempting to read image: " + url.toString());
					}
					
					matrix.addAll(addToMatrix(matrix, bufferedImage, resizedWidth, resizedHeight));

				} catch (IOException io) {
					io.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});
		
		return matrix;

	}
	
	public static List<Object> getMatrixFromPath(List<Object> matrix, String fileName, int resizedWidth, int resizedHeight) throws IOException {
		
		PowderPlugin.getInstance().getServer().getScheduler().runTask(PowderPlugin.getInstance(), new Runnable() {

			@Override
			public void run() {
				
				try {
					File file = new File(PowderPlugin.getInstance().getDataFolder() + "/images", fileName);
					BufferedImage bufferedImage;
					bufferedImage = ImageIO.read(file);
					if (bufferedImage == null) {
						throw new IOException("Error while attempting to read image: " + fileName);
					}
					
					matrix.addAll(addToMatrix(matrix, bufferedImage, resizedWidth, resizedHeight));
				} catch (IOException io) {
					io.printStackTrace();
				}
				
			}

		});
		
		return matrix;
		
	}
	
	public static BufferedImage getScaledImage(BufferedImage bufferedImage, int width, int height){
	    int finalWidth = width;
	    int finalHeight = height;
	    double factor = 1.0d;
	    if(bufferedImage.getWidth() > bufferedImage.getHeight()){
	        factor = ((double)bufferedImage.getHeight()/(double)bufferedImage.getWidth());
	        finalHeight = (int)(finalWidth * factor);                
	    }else{
	        factor = ((double)bufferedImage.getWidth()/(double)bufferedImage.getHeight());
	        finalWidth = (int)(finalHeight * factor);
	    }   

	    BufferedImage resizedImg = new BufferedImage(finalWidth, finalHeight, BufferedImage.TRANSLUCENT);
	    Graphics2D g2 = resizedImg.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(bufferedImage, 0, 0, finalWidth, finalHeight, null);
	    g2.dispose();
	    return resizedImg;
	}
	
	public static List<Object> addToMatrix(List<Object> matrix, BufferedImage bufferedImage, 
			int resizedWidth, int resizedHeight) {
		
		BufferedImage newImage = getScaledImage(bufferedImage, resizedWidth, resizedHeight);

		for (int y = 0; y <= newImage.getHeight() - 1; y++) {

			for (int x = 0; x <= newImage.getWidth() - 1; x++) {

				int pixel;
				try {
					pixel = newImage.getRGB(x, y);
				} catch (Exception e) {
					PowderPlugin.getInstance().getLogger().info("|| " + newImage.getWidth() + " " + newImage.getHeight());
					PowderPlugin.getInstance().getLogger().info(x + " " + y);
					continue;
				}
				if ((pixel >> 24) == 0x00) {
					matrix.add(".");
					continue;
				}
				Color color = new Color(pixel);
				int arr = color.getRed();
				int gee = color.getGreen();
				int bee = color.getBlue();
				if (arr == 0) {
					arr = 1;
				}
				ChangedParticle changedParticle = new ChangedParticle(null, Particle.REDSTONE, arr, gee, bee);
				matrix.add(changedParticle);

			}

			matrix.add(";");

		}
		
		return matrix;
		
	}

}