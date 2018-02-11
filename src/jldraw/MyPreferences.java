/*
	Copyright 2014 Mario Pascucci <mpascucci@gmail.com>
	Copyright 2017 Jeremy Czajkowski <jeremy.cz@wp.pl>
	This file is part of JLDraw

	JLDraw is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	JLDraw is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with JLDraw.  If not, see <http://www.gnu.org/licenses/>.

*/


package jldraw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;


public class MyPreferences {

	private Preferences prefs;
	private File prefsFile;
	private boolean stored = false;
	
	
	public MyPreferences(Preferences p, File pf) {
		if (p == null || !(p instanceof Preferences))
			throw new IllegalArgumentException("Invalid or null Preferences instance");
		prefs = p;
		prefsFile = pf;
		if (pf.canRead() && pf.isFile()) {
			try {
				Preferences.importPreferences(new FileInputStream(pf));
				stored = true;
			} catch (FileNotFoundException ex) {
				try {
					prefs.clear();
				} catch (BackingStoreException ex1) {
					ex1.printStackTrace();
				}
			} catch (IOException | InvalidPreferencesFormatException ex) {
			}
		}
		else {
			try {
				prefs.clear();
			} catch (BackingStoreException ex1) {
				ex1.printStackTrace();
			}
		}
	}
	
	
	public boolean isStored() {
		return stored;
	}
	
	
	public void savePreferences() {
		try {
			prefs.exportSubtree(new FileOutputStream(prefsFile));
		} catch (IOException | BackingStoreException ex) {
		}
	}
	
	
	public void put(String key, String value) {
		prefs.put(key, value);
	}

	public void putList(String key, String[] value) {
		Preferences res = prefs.node(key);
		for (int k = 0; k < value.length; k++) {
			res.put(String.valueOf(k), value[k]);
		}
	}	
	public void putList(String key, int[] value) {
		Preferences res = prefs.node(key);
		for (int k = 0; k < value.length; k++) {
			res.putInt(String.valueOf(k), value[k]);
		}
	}	
	
	public void putBool(String key, boolean value) {
		prefs.putBoolean(key, value);
	}
	
	
	public void putInt(String key, int value) {
		prefs.putInt(key, value);
	}
	
	
	public String get(String key) {
		return prefs.get(key, "");
	}
	
	public boolean getBool(String key) {
		return prefs.getBoolean(key, false);
	}

	
	public int getInt(String key) {
		return prefs.getInt(key, 0);
	}

	public String[] getList(String key) {
		Preferences res = prefs.node(key);
		String[] result = {};
		try {
			String[] keys = res.keys();
			result = new String[keys.length];
			for (int k = 0; k < keys.length; k++) {
				result[k] = res.get(keys[k],"");
			}
		} catch (BackingStoreException ex) {
		}
		return result;
	}
}
