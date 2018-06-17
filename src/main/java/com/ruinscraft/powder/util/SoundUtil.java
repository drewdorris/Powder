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

	public static List<SoundEffect> getSoundEffectsFromNBS(String fileName, double volume, 
			double multiplier, boolean surroundSound, int transpose, boolean limitNotes, 
			int volumeMultiplier, int startTime, int repeatTime, int lockedIterations) {
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

			return getSoundEffectsFromSong(song, volume, multiplier, surroundSound,
					transpose, limitNotes, volumeMultiplier, startTime, repeatTime, lockedIterations);
		} else {
			File file = new File(PowderPlugin.getInstance().getDataFolder() + "/songs", fileName);
			if (!file.exists()) {
				PowderPlugin.getInstance().saveResource("songs/" + fileName, false);
			}
			Song song = NBSDecoder.parse(file);

			return getSoundEffectsFromSong(song, volume, multiplier, surroundSound,
					transpose, limitNotes, volumeMultiplier, startTime, repeatTime, lockedIterations);
		}
	}

	public static List<SoundEffect> getSoundEffectsFromSong(Song song, double volume, 
			double multiplier, boolean surroundSound, int transpose, boolean limitNotes, 
			int volumeMultiplier, int startTime, int repeatTime, int lockedIterations) {
		List<SoundEffect> soundEffects = new ArrayList<>();
		for (Integer integer : song.getLayerHashMap().keySet()) {
			Layer layer = song.getLayerHashMap().get(integer);
			for (int i = 0; i < volumeMultiplier; i++) {
				for (Integer tick : layer.getHashMap().keySet()) {
					Note note = layer.getHashMap().get(tick);
					Sound sound = Instrument.getInstrument(note.getInstrument());
					float newVolume = (layer.getVolume() / 100F) * ((float) volume);
					float pitch = note.getKey() - 33;
					if (limitNotes) {
						pitch = transpose((int) pitch + transpose);
					} else {
						pitch = pitch + transpose;
					}
					pitch = (float) Math.pow(2.0, (pitch - 12.0) / 12.0);
					SoundEffect soundEffect = new SoundEffect(sound, newVolume, 
							pitch, surroundSound, ((int) (tick * multiplier)) + startTime, 
							repeatTime, lockedIterations);
					soundEffects.add(soundEffect);
				}
			}
		}
		return soundEffects;
	}

	public static int transpose(int note) {
		if (note > 24) {
			return transpose(note - 12);
		} else if (note < 0) {
			return transpose(note + 12);
		} else {
			return note;
		}
	}

}
