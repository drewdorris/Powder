package com.ruinscraft.powder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bukkit.Particle;
import org.bukkit.entity.Player;

import com.ruinscraft.powder.objects.ChangedParticle;
import com.ruinscraft.powder.objects.ParticleMap;
import com.ruinscraft.powder.objects.PowderMap;

public class ImageUtil {

	public static PowderMap getPowderMap(Player player, URL url, String powderName, Float spacing) throws IOException {

		Powder.getInstance().getLogger().info("1");
		final HttpURLConnection httpConnection;
		httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.connect();
		int httpCode = httpConnection.getResponseCode();
		if (httpCode != 200) {
			throw new IOException("Error while attempting to connect to URL: " + url.toString());
		}

		InputStream stream = httpConnection.getInputStream();
		BufferedImage image = ImageIO.read(stream);
		if (image == null) {
			throw new IOException("Error while attempting to read image: " + url.toString());
		}

		Powder.getInstance().getLogger().info("2");
		int height = image.getHeight();
		int width = image.getWidth();

		// make this part of cmd sometime
		Powder.getInstance().getLogger().info("3");

		List<ParticleMap> particleMaps = new ArrayList<ParticleMap>();
		List<Object> map = new ArrayList<Object>();
		Powder.getInstance().getLogger().info("4");
		Powder.getInstance().getLogger().info("height & width " + height + " " + width);
		for (int y = 1; y <= height - 1; y++) {
			Powder.getInstance().getLogger().info("get dat x");
			for (int x = 1; x <= width - 1; x++) {
				Powder.getInstance().getLogger().info("got it!");
				Powder.getInstance().getLogger().info(String.valueOf(x) + " " + String.valueOf(y));
				Color color = new Color(image.getRGB(x, y));
				Powder.getInstance().getLogger().info("get the color");
				int arr = color.getRed();
				int gee = color.getGreen();
				int bee = color.getBlue();
				if (arr > 250 && gee > 250 && bee > 250) {
					map.add(".");
					continue;
				}
				if (arr == 0) {
					arr = 1;
				}
				Powder.getInstance().getLogger().info("change");
				ChangedParticle changedParticle = new ChangedParticle(null, Particle.REDSTONE, arr, gee, bee);
				map.add(changedParticle);

			}
			map.add(";");
		}
		particleMaps.add(new ParticleMap(map, 0, width / 2, height, spacing));

		final PowderMap powderMap = new PowderMap(powderName, spacing, particleMaps, true, false, false, 0);

		return powderMap;

	}

}
