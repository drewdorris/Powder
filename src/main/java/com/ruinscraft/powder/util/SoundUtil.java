package com.ruinscraft.powder.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Sound;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.models.SoundEffect;
import com.xxmicloxx.NoteBlockAPI.Instrument;
import com.xxmicloxx.NoteBlockAPI.Layer;
import com.xxmicloxx.NoteBlockAPI.NBSDecoder;
import com.xxmicloxx.NoteBlockAPI.Note;
import com.xxmicloxx.NoteBlockAPI.Song;

public class SoundUtil {

	public static List<SoundEffect> getSoundEffectsFromNBS(String fileName, double volume, double multiplier, int addTime) {
		if (fileName.contains("/")) {
			URL url = PowderUtil.readURL(fileName);
			Song song;
			try {
				InputStream inputStream = PowderUtil.getInputStreamFromURL(url);
				song = NBSDecoder.parse(inputStream);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			return getSoundEffectsFromSong(song, volume, multiplier, addTime);
		} else {
			File file = new File(PowderPlugin.getInstance().getDataFolder() + "/songs", fileName);
			Song song = NBSDecoder.parse(file);

			return getSoundEffectsFromSong(song, volume, multiplier, addTime);
		}
	}

	public static List<SoundEffect> getSoundEffectsFromSong(Song song, double volume, double multiplier, int addTime) {
		List<SoundEffect> soundEffects = new ArrayList<SoundEffect>();
		for (Integer integer : song.getLayerHashMap().keySet()) {
			Layer layer = song.getLayerHashMap().get(integer);
			for (Integer tick : layer.getHashMap().keySet()) {
				Note note = layer.getHashMap().get(tick);
				Sound sound = Instrument.getInstrument(note.getInstrument());
				float newVolume = (layer.getVolume() / 100F) * ((float) volume);
				float pitch = note.getKey() - 33;
				pitch = (float) Math.pow(2.0, (pitch - 12.0) / 12.0);
				SoundEffect soundEffect = new SoundEffect(sound, newVolume, pitch, ((int) (tick * multiplier)) + addTime);
				soundEffects.add(soundEffect);
			}
		}
		return soundEffects;
	}

}
