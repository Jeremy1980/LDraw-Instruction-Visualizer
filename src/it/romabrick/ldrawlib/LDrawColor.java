/*
	Copyright 2014 Mario Pascucci <mpascucci@gmail.com>
	Copyright 2017 Jeremy Czajkowski <jeremy.cz@wp.pl>
	This file is part of LDrawLib

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

import java.awt.Color;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LDraw color system
 * 
 * @author Mario Pascucci
 *
 */
public class LDrawColor {

	private static final String ldrconfig = "LDConfig.ldr";
	public static final int INVALID_COLOR = -1;
	public static final int CURRENT = 16;
	public static final int EDGE = 24;
	private int id;
	private Color c;
	private Color edge;
	private String name;
	private static Map<Integer, LDrawColor> ldrColors = new LinkedHashMap<Integer, LDrawColor>();

	private LDrawColor(int id, Color c, Color e, String n) {

		this.id = id;
		this.c = c;
		edge = e;
		name = n;
	}

	/**
	 * Returns part color identified by <b>id</b> as LDraw color index or direct
	 * color
	 * 
	 * @param id
	 *            LDraw color index or a direct color
	 * @return Color (AWT) for part or <b>Color.BLACK</b> if index is
	 *         invalid/unknown
	 */
	static public Color getColorById(int id) {

		if (id < 0x2000000) {
			Color c = ldrColors.get(id).c;
			if (c != null)
				return c;
		} else if (id < 0x3000000)
			return new Color(id - 0x2000000);
		LDlogger.warn("Invalid color: " + id);
		return Color.BLACK;
	}

	/**
	 * Returns edge color identified by <b>id</b> as LDraw color index
	 * 
	 * @param id
	 *            LDraw color index or a direct color
	 * @return Color (AWT) for edge or <b>Color.RED</b> if index is invalid
	 */
	static public Color getEdgeColorById(int id) {

		if (id < 0x2000000) {
			Color c = ldrColors.get(id).edge;
			if (c != null)
				return c;
		} else if (id < 0x3000000) {
			LDlogger.warn("Invalid direct edge color: " + id);
			// this can't happens, but...
			return new Color(id - 0x2000000);
		}
		LDlogger.warn("Invalid color: " + id);
		return Color.RED;
	}

	/**
	 * Checks if <b>id</b> is a valid color index in LDraw.
	 * 
	 * If <b>id</b> is a "direct color" with 0x2RRGGBB syntax return true
	 * 
	 * @param id
	 * @return <b>true</b> if index is valid
	 */
	static public boolean isLDrawColor(int id) {

		if (id < 0x2000000)
			return ldrColors.containsKey(id);
		else if (id < 0x3000000)
			// is a direct color
			return true;
		return false;
	}

	/**
	 * Returns an LDraw color or a direct user color with index <b>id</b>
	 * 
	 * @param id
	 *            LDraw color index or an int value of direct color as
	 *            "0x2rrggbb"
	 * @return LDraw color with index <b>id</b> or newly created direct color
	 * @throws IndexOutOfBoundsException
	 *             if no color exists with index <b>id</b> or id > 0x2ffffff
	 */
	static public LDrawColor getById(int id) {

		LDrawColor c;

		if (id < 0x2000000) {
			c = ldrColors.get(id);
			if (c == null) {
				LDlogger.warn("[LdrawColor] Unknown color: " + id);
				return ldrColors.get(INVALID_COLOR);
			}
			return c;
		} else if (id < 0x3000000) {
			// it is a direct color
			return newLDrawColor("Direct color", id, new Color(id - 0x2000000), Color.BLACK);
		}
		LDlogger.warn("[LdrawColor] Illegal direct color: " + id);
		return ldrColors.get(INVALID_COLOR);
	}

	public String toString() {
		return String.format("%s [code=%d]", name.replace("_", " "), id);
	}

	public int getId() {
		return id;
	}

	public Color getColor() {
		return c;
	}

	public Color getEdge() {
		return edge;
	}

	public String getName() {
		return name;
	}

	/**
	 * Create a new LDrawColor object No checks are done about duplicated index,
	 * color and name
	 * 
	 * @param name
	 *            color identifier
	 * @param index
	 *            integer index of color
	 * @param mainColor
	 *            an AWT Color for polygons
	 * @param edgeColor
	 *            an AWT Color for edges
	 * @return LDrawColor object
	 * @throws IllegalArgumentException
	 *             if name is empty or null, if mainColor is null, if edgeColor
	 *             is null
	 */
	static LDrawColor newLDrawColor(String name, int index, Color mainColor, Color edgeColor) {

		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("[LDrawLib.LDrawColor] Color identifier can't be empty");
		if (mainColor == null || edgeColor == null)
			throw new IllegalArgumentException("[LDrawLib.LDrawColor] Main and Edge color can't be null");
		return new LDrawColor(index, mainColor, edgeColor, name);
	}

	/**
	 * reads standard color list from LDConfig.ldr file inside LDraw library
	 * 
	 * @param ldlib
	 *            initialized LDraw library
	 * @throws IOException
	 *             if file isn't readable or there are errors in file syntax
	 * @throws IllegalArgumentException
	 *             if ldlib is null
	 */
	public static void readFromLibrary(LDrawLib ldlib) throws IOException {

		String l;

		if (ldlib == null) {
			throw new IllegalArgumentException("[LDRawLib.readFromLibrary] LDraw library object cannot be null");
		}
		LineNumberReader lnr = ldlib.getFile(ldrconfig);
		try {
			while ((l = lnr.readLine()) != null) {
				LDrawCommand cmd = LDrawParser.parseCommand(l);
				if (cmd != LDrawCommand.COLOUR)
					continue;
				LDrawColor ldc;
				try {
					ldc = LDrawParser.parseColour(l);
					ldrColors.put(ldc.id, ldc);
				} catch (LDrawException e) {
					LDlogger.error(ldrconfig, lnr.getLineNumber(), "Unable to parse !COLOUR definition in library");
				}
			}
		} catch (IOException exc) {
			throw new IOException("[LDrawLib] Unable to read LDraw colors\n" + "File: " + ldrconfig + " line #: "
					+ lnr.getLineNumber());
		}
		// put invalid color code and color schema (deep black with red lines)
		ldrColors.put(-1, new LDrawColor(-1, Color.BLACK, Color.RED, "Invalid/Unknown color"));
	}

	public static Collection<LDrawColor> getColorList() {
		return ldrColors.values();
	}

}
