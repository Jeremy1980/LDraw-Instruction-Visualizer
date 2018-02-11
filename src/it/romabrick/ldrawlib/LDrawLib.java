
/*
	Copyright 2013-2014 Mario Pascucci <mpascucci@gmail.com>
	Copyright 2013-2014 Jeremy Czajkowski <jeremy.cz@wp.pl>
	This file is part of LDrawLib.

	LDrawLib is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	LDrawLib is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with LDrawLib.  If not, see <http://www.gnu.org/licenses/>.

*/

package it.romabrick.ldrawlib;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LDrawLib {

	private String official, unofficial;
	private ZipFile o, u;
	private boolean fromFolder = false;
	private boolean officialOnly = false;
	private boolean forceLoRes = false;
	private boolean forceHiRes = false;
	private final String mainPath = "ldraw/";
	private final String partPath = "ldraw/parts/";
	private final String primitivesPath = "ldraw/p/";
	private final String hiResPrimitivesPath = "ldraw/p/48/";
	private final String loResPrimitivesPath = "ldraw/p/8/";
	private final String modelsPath = "ldraw/models/";
	private final String uPartPath = "parts/";
	private final String uPrimitivesPath = "p/";
	private final String uLoResPrimitivesPath = "p/8/";
	private final String uHiResPrimitivesPath = "p/48/";
	private final String uModelsPath = "models/";
	private Map<String, String> officialParts = new HashMap<String, String>();
	private Map<String, String> unofficialParts = new HashMap<String, String>();

	/**
	 * A library to read LDraw primitives, parts and other files from zipfiles.
	 * 
	 * Requires only Official part library to work. If supplied, uses unofficial
	 * library, too. To disable (momentary or permanently) use of unofficial
	 * parts, use setOfficialOnly(true)
	 * 
	 * @param official
	 *            complete pathname to official LDraw library zip file
	 * @param unofficial
	 *            complete pathname to unofficial LDraw library zip file,
	 *            Optional can be <b>null</b>
	 * @throws IOException
	 *             if zip file is unreadable or an official library is not
	 *             supplied
	 */
	public LDrawLib(String official, String unofficial) throws IOException {

		this.official = official;
		this.unofficial = unofficial;
		o = null;
		u = null;
		if (official != null) {
			File f = new File(this.official);
			if (f.isFile() && f.canRead()) {
				o = new ZipFile(official);
				Enumeration<? extends ZipEntry> en = o.entries();
				while (en.hasMoreElements()) {
					ZipEntry ze = en.nextElement();
					if (!ze.isDirectory()) {
		// if a duplicate exists, first win
						String key = ze.getName().toLowerCase();
						if (officialParts.get(key) != null) {
							continue;
						}
						officialParts.put(key, ze.getName());
					}
				}
			}
		}
		if (unofficial != null && unofficial.length() > 0) {
			File f = new File(this.unofficial);
			if (f.isFile() && f.canRead()) {
				u = new ZipFile(unofficial);
				Enumeration<? extends ZipEntry> en = u.entries();
				while (en.hasMoreElements()) {
					ZipEntry ze = en.nextElement();
					if (!ze.isDirectory()) {
		// if a duplicate exists, first win
						String key = ze.getName().toLowerCase();
						if (unofficialParts.get(key) != null) {
							continue;
						}
						unofficialParts.put(key, ze.getName());
					}
				}
			} else {
				officialOnly = true;
			}
		} else {
			officialOnly = true;
		}
		if (o == null)
			throw new IOException("[LDrawLib] No LDraw Official library file found or no file can be used.");
		fromFolder = false;
		LDrawColor.readFromLibrary(this);
		LDrawPart.setLdrlib(this);
	}

	/**
	 * Uses an unzipped library, instead of complete.zip
	 * 
	 * @param off
	 *            a directory where is the root of unzipped LDraw library
	 * @param unoff
	 *            is a directory where the root of unzipped Unofficial LDraw
	 *            parts
	 * @throws IOException
	 *             if path is invalid or unreadable
	 */
	public LDrawLib(File off, File unoff) throws IOException {

		if (!off.isDirectory() || !off.canRead()) {
			throw new IOException("[LDrawLib] Path to library root is invalid");
		}
		official = off.getAbsolutePath();
		// scan p folder
		File p = new File(off.getPath(), "p");
		if (!p.isDirectory() || !p.canRead())
			throw new IOException("Cannot read primitive directory: " + p.getPath());
		for (File f : p.listFiles()) {
			if (f.isFile() && f.canRead()) {
				String key = primitivesPath + f.getName().toLowerCase();
				if (officialParts.get(key) != null) {
					continue;
				}
				officialParts.put(key, f.getAbsolutePath());
			}
		}
		// scan p/48 folder
		p = new File(off.getPath(), "p/48");
		if (!p.isDirectory() || !p.canRead())
			throw new IOException("Cannot read hi-res primitive directory: " + p.getPath());
		for (File f : p.listFiles()) {
			if (f.isFile() && f.canRead()) {
				String key = hiResPrimitivesPath + f.getName().toLowerCase();
				if (officialParts.get(key) != null) {
					continue;
				}
				officialParts.put(key, f.getAbsolutePath());
			}
		}
		// scan p/8 folder
		p = new File(off.getPath(), "p/8");
		if (!p.isDirectory() || !p.canRead())
			throw new IOException("Cannot read lo-res primitive directory: " + p.getPath());
		for (File f : p.listFiles()) {
			if (f.isFile() && f.canRead()) {
				String key = loResPrimitivesPath + f.getName().toLowerCase();
				if (officialParts.get(key) != null) {
					continue;
				}
				officialParts.put(key, f.getAbsolutePath());
			}
		}
		// scan parts folder
		p = new File(off.getPath(), "parts");
		if (!p.isDirectory() || !p.canRead())
			throw new IOException("Cannot read parts directory: " + p.getPath());
		for (File f : p.listFiles()) {
			if (f.isFile() && f.canRead()) {
				String key = partPath + f.getName().toLowerCase();
				if (officialParts.get(key) != null) {
					continue;
				}
				officialParts.put(key, f.getAbsolutePath());
			}
		}
		// scan parts/s/ folder
		p = new File(off.getPath(), "parts/s");
		if (!p.isDirectory() || !p.canRead())
			throw new IOException("Cannot read subparts directory: " + p.getPath());
		for (File f : p.listFiles()) {
			if (f.isFile() && f.canRead()) {
				String key = partPath + "s/" + f.getName().toLowerCase();
				if (officialParts.get(key) != null) {
					continue;
				}
				officialParts.put(key, f.getAbsolutePath());
			}
		}
		fromFolder = true;
		LDrawColor.readFromLibrary(this);
		LDrawPart.setLdrlib(this);

		if (unoff == null || unoff.length() == 0)
			return;
		if (!unoff.isDirectory() || !unoff.canRead())
			throw new IOException("Cannot read unofficial parts directory: " + unoff.getPath());
		// reads unofficial parts
		unofficial = unoff.getAbsolutePath();
		// in unofficial parts reading is tolerant for missing/invalid folders
		p = new File(unoff.getPath(), "p");
		if (p.isDirectory() && p.canRead()) {
			for (File f : p.listFiles()) {
				if (f.isFile() && f.canRead()) {
					String key = uPrimitivesPath + f.getName().toLowerCase();
					if (unofficialParts.get(key) != null) {
						continue;
					}
					unofficialParts.put(key, f.getAbsolutePath());
				}
			}
		}
		// scan p/48 folder
		p = new File(unoff.getPath(), "p/48");
		if (p.isDirectory() && p.canRead()) {
			for (File f : p.listFiles()) {
				if (f.isFile() && f.canRead()) {
					String key = uHiResPrimitivesPath + f.getName().toLowerCase();
					if (unofficialParts.get(key) != null) {
						continue;
					}
					unofficialParts.put(key, f.getAbsolutePath());
				}
			}
		}
		// scan p/8 folder
		p = new File(unoff.getPath(), "p/8");
		if (p.isDirectory() && p.canRead()) {
			for (File f : p.listFiles()) {
				if (f.isFile() && f.canRead()) {
					String key = uLoResPrimitivesPath + f.getName().toLowerCase();
					if (unofficialParts.get(key) != null) {
						continue;
					}
					unofficialParts.put(key, f.getAbsolutePath());
				}
			}
		}
		// scan parts folder
		p = new File(unoff.getPath(), "parts");
		if (p.isDirectory() && p.canRead()) {
			for (File f : p.listFiles()) {
				if (f.isFile() && f.canRead()) {
					String key = uPartPath + f.getName().toLowerCase();
					if (unofficialParts.get(key) != null) {
						continue;
					}
					unofficialParts.put(key, f.getAbsolutePath());
				}
			}
		}
		// scan parts/s/ folder
		p = new File(unoff.getPath(), "parts/s");
		if (p.isDirectory() && p.canRead()) {
			for (File f : p.listFiles()) {
				if (f.isFile() && f.canRead()) {
					String key = uPartPath + "s/" + f.getName().toLowerCase();
					if (unofficialParts.get(key) != null) {
						continue;
					}
					unofficialParts.put(key, f.getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Force retrieval of low-resolution primitives, if available. If requested
	 * primitive is already chosen as lo- or hi- res this setting is ignored
	 */
	public void forceLoRes() {

		forceLoRes = true;
		forceHiRes = false;
	}

	/**
	 * Force retrieval of high-resolution primitives, if available If requested
	 * primitive is already chosen as lo- or hi- res this setting is ignored
	 */
	public void forceHiRes() {

		forceLoRes = false;
		forceHiRes = true;
	}

	/**
	 * Reset to standard resolution, or defined by single part request
	 */
	public void resetStdRes() {

		forceLoRes = false;
		forceHiRes = false;
	}

	/**
	 * checks if library is in hi-res mode
	 * 
	 * @return true if forced in hi-res mode
	 */
	public boolean isHiRes() {

		return forceHiRes;
	}

	/**
	 * checks if library is in lo-res mode
	 * 
	 * @return true if forced in lo-res mode
	 */
	public boolean isLoRes() {

		return forceLoRes;
	}

	/**
	 * checks if library is in standard mode (no hi- or lo-res primitives
	 * changing)
	 * 
	 * @return true if library returns parts primitives exactly as requested
	 */
	public boolean isStdRes() {

		return !(forceHiRes || forceLoRes);
	}

	/**
	 * Checks if LDrawLib looking for parts only in official library
	 * 
	 * @return true if using only official parts
	 */
	public boolean isOfficialOnly() {
		return officialOnly;
	}

	/**
	 * Enable or disable use of unofficial parts library
	 * 
	 * @param officialOnly
	 *            if true uses only official parts
	 */
	public void setOfficialOnly(boolean officialOnly) {
		this.officialOnly = officialOnly;
	}

	/**
	 * Checks if a LDraw parts, sub-parts or primitive is in current library
	 * 
	 * @param ldrawId
	 *            LDraw part id as string, with full path and ".dat" appended
	 * @return LDrawPartType != UNKNOWN if part <strong>ldrawId</strong> exists
	 *         in library
	 */
	public LDrawPartType checkPart(String ldrawId) {

		String[] part;

		// part search is case insensitive
		String ldrid = ldrawId.toLowerCase();
		ldrid = ldrid.replace('\\', '/'); // in ZIP path separator is always '/'
		part = ldrid.split("/");
		if (part.length > 1) {
			if (part[0].equals("s")) {
				if (officialParts.containsKey(partPath + ldrid))
					return LDrawPartType.SUBPART;
				else if (unofficialParts.containsKey(uPartPath + ldrid))
					return LDrawPartType.UNOFF_SUB;
			}
		// hi-res primitive
			else if (part[0].equals("48")) {
				if (officialParts.containsKey(primitivesPath + ldrid))
					return LDrawPartType.PRIMITIVE;
				else if (unofficialParts.containsKey(uPrimitivesPath + ldrid))
					return LDrawPartType.UNOFF_PRIM;
			}
		// lo-res primitive
			else if (part[0].equals("8")) {
				if (officialParts.containsKey(primitivesPath + ldrid))
					return LDrawPartType.PRIMITIVE;
				else if (unofficialParts.containsKey(uPrimitivesPath + ldrid))
					return LDrawPartType.UNOFF_PRIM;
			}
		} else {
			if (officialParts.containsKey(primitivesPath + ldrid))
				return LDrawPartType.PRIMITIVE;
			else if (unofficialParts.containsKey(uPrimitivesPath + ldrid))
				return LDrawPartType.UNOFF_PRIM;
			if (officialParts.containsKey(partPath + ldrid))
				return LDrawPartType.OFFICIAL;
			else if (unofficialParts.containsKey(uPartPath + ldrid))
				return LDrawPartType.UNOFFICIAL;
		}
		return LDrawPartType.UNKNOWN;

	}

	/**
	 * Gets a file from LDraw official Library
	 * 
	 * @param pathname
	 *            pathname into library zip file
	 * @return a LineNumberReader or null if file is not found
	 * @throws IOException
	 */
	public LineNumberReader getFile(String path) throws IOException {

		String pathname = path.toLowerCase();
		pathname = pathname.replace('\\', '/');
		if (fromFolder) {
			try {
				return new LineNumberReader(new FileReader(new File(official, path)));
			} catch (IOException e) {
				throw new IOException(
						"[LDrawLib] Unable to read file from folder: " + official + "\n" + e.getLocalizedMessage());
			}
		}
		ZipEntry ze = o.getEntry(officialParts.get(mainPath + pathname));
		if (ze != null)
			try {
				return new LineNumberReader(new InputStreamReader(o.getInputStream(ze)));
			} catch (IOException e) {
				throw new IOException(
						"[LDrawLib] Unable to get file " + pathname + " from zipfile\n" + e.getLocalizedMessage());
			}
		else {
			throw new IOException("[LDrawLib] File not found in zipfile: " + pathname);
		}

	}

	/**
	 * Gets a LineNumberReader for part identified by "ldrid"
	 * 
	 * @param ldrid
	 *            LDraw part ID as full pathname with ".dat" suffix
	 * @return a LineNumberReader or null if part is not found
	 */
	public LineNumberReader getPart(String ldrawid) {

		String[] part;
		String ze = null;
		boolean unoff = false;

		String ldrid = ldrawid.toLowerCase();
		ldrid = ldrid.replace('\\', '/');
		part = ldrid.split("/");

		if (part.length > 1) {
			if (part[0].equals("s")) {
				ze = officialParts.get(partPath + ldrid);
				if (!officialOnly && ze == null && u != null) {
					ze = unofficialParts.get(uPartPath + ldrid);
					if (ze != null)
						unoff = true;
				}
			} else if (part[0].equals("48")) {
				ze = officialParts.get(primitivesPath + ldrid);
				if (!officialOnly && ze == null && u != null) {
					ze = unofficialParts.get(uPrimitivesPath + ldrid);
					if (ze != null)
						unoff = true;
				}
			} else if (part[0].equals("8")) {
				ze = officialParts.get(primitivesPath + ldrid);
				if (!officialOnly && ze == null && u != null) {
					ze = unofficialParts.get(uPrimitivesPath + ldrid);
					if (ze != null)
						unoff = true;
				}
			}
		} else {
			if (forceLoRes) {
				ze = officialParts.get(loResPrimitivesPath + ldrid);
			} else if (forceHiRes) {
				ze = officialParts.get(hiResPrimitivesPath + ldrid);
			}
			if (ze == null) {
				ze = officialParts.get(primitivesPath + ldrid);
			}
			if (!officialOnly && ze == null && u != null) {
				if (forceLoRes) {
					ze = unofficialParts.get(uLoResPrimitivesPath + ldrid);
				} else if (forceHiRes) {
					ze = unofficialParts.get(uHiResPrimitivesPath + ldrid);
				}
				if (ze == null) {
					ze = unofficialParts.get(uPrimitivesPath + ldrid);
				}
				if (ze != null)
					unoff = true;
			}
			if (ze == null) {
				ze = officialParts.get(partPath + ldrid);
				if (!officialOnly && ze == null && u != null) {
					ze = unofficialParts.get(uPartPath + ldrid);
					if (ze != null)
						unoff = true;
				}
				if (ze == null) {
					ze = officialParts.get(modelsPath + ldrid);
					if (!officialOnly && ze == null && u != null) {
						ze = unofficialParts.get(uModelsPath + ldrid);
						if (ze != null)
							unoff = true;
					}
					if (ze == null) {
						return null;
					}
				}
			}
		}
		if (ze != null)
			try {
				if (fromFolder) {
					if (unoff) {
						return new LineNumberReader(new FileReader(ze));
					} else {
						return new LineNumberReader(new FileReader(ze));
					}
				} else {
					if (unoff) {
						return new LineNumberReader(new InputStreamReader(u.getInputStream(u.getEntry(ze))));
					} else {
						return new LineNumberReader(new InputStreamReader(o.getInputStream(o.getEntry(ze))));
					}
				}
			} catch (IOException ex) {
				System.out.println("[LDrawLib] Unable to get part " + ldrid + " " + ex.getLocalizedMessage());
				return null;
			}
		else {
			System.out.println("[LDrawLib] Unable to get part " + ldrid);
			return null;
		}
	}

	/**
	 * Try to find the path to the specified file. This public method is recursive.
	 * 
	 * @param basename  
	 * 			filename in format <i>name<b>.</b>extension</i>, like: <b>30027a.dat</b>
	 * @param root 
	 * 			path in file system, to start searching
	 * @return a String or null if file is not found
	 */
	public static String localizePart(String basename, File root) {
		String result = null;
		
		if (root != null)
		for(File f: root.listFiles()) {
			if (f.getName().compareToIgnoreCase(basename)==0) {
				return f.isFile() && f.canRead() ? f.getAbsolutePath() : null;
			}
			if (f.isDirectory()) {
				result = localizePart(basename,f);
				if (result != null) return result;
			}
		};
		return result;
	}

	/**
	 * Get path to part file from file system
	 * 
	 * @param basename
	 * 			filename with extension
	 * @return a String or null if file is not found
	 */
	public String getSearchResult(String basename) {
		String filename = localizePart(basename,new File(this.official));
		if (filename == null) filename = localizePart(basename,new File(this.unofficial));
		
		return filename;
	}

}
