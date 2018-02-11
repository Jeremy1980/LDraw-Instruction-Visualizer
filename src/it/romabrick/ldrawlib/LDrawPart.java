/*
	Copyright 2013-2014 Mario Pascucci <mpascucci@gmail.com>
	Copyright 2017 Jeremy Czajkowski <jeremy.cz@wp.pl>
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

import it.romabrick.matrix3d.Matrix3D;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LDrawPart {

	private int id;
	private String ldrawid;
	private String description;
	private String canonicalDescription = null;
	private String author;
	private String partName;
	private String relativePartPath = null;
	private String category;
	private String keywords;
	private Matrix3D matrix;
	private int colorId = LDrawColor.CURRENT;
	private int stepNo = -1;
	private boolean placed = false;
	private LDrawPartType partType = LDrawPartType.UNKNOWN;
	private List<LDPrimitive> primitives = new ArrayList<LDPrimitive>();
	private static LDrawLib ldrlib = null;
	private static Map<String, LDrawPart> partCache = new LinkedHashMap<String, LDrawPart>();
	private static Map<String, LDrawPart> customPartCache = new LinkedHashMap<String, LDrawPart>();
	private static int globalId = 0;
	private static Map<Integer, LDrawPart> globalPartList = new LinkedHashMap<Integer, LDrawPart>();

	/**
	 * Private constructor for LDraw Part factory
	 * 
	 * @param ldrid
	 *            is LDraw part name (LDraw is case-sensitive, so use
	 *            lowercase!)
	 * @throws IOException
	 *             if LDraw library files isn't readable
	 * @throws LDrawException
	 *             if there are parse or format error
	 */
	private LDrawPart(String ldrid) throws IOException, LDrawException {

		if (ldrlib == null) {
			throw new IllegalStateException("[LDrawPart] LDraw library not initialized");
		}
		ldrawid = ldrid;
		id = getUniqueId();
		LineNumberReader ldf = ldrlib.getPart(ldrawid);
		parse(ldf);
	}

	/**
	 * creates unofficial/submodel/custom part
	 */
	private LDrawPart() {

		id = getUniqueId();
	}
	

	private LDrawPart(LDPrimitive p) throws IOException, LDrawException {

		id = getUniqueId();
		switch (p.getType()) {
		case AUXLINE:
			partType = LDrawPartType.GEOM_PRIMITIVE;
			ldrawid = "AuxLine";
			category = "Primitives";
			description = "LDraw auxiliary line primitive";
			matrix = new Matrix3D();
			primitives.add(p);
			break;
		case COLOUR:
			partType = LDrawPartType.COMMAND;
			ldrawid = "Colour";
			category = "Commands";
			description = "LDraw !Colour statement";
			primitives.add(p);
			break;
		case LINE:
			partType = LDrawPartType.GEOM_PRIMITIVE;
			ldrawid = "Line";
			category = "Primitives";
			description = "LDraw line primitive";
			matrix = new Matrix3D();
			primitives.add(p);
			break;
		case QUAD:
			partType = LDrawPartType.GEOM_PRIMITIVE;
			ldrawid = "Quad";
			category = "Primitives";
			description = "LDraw quad primitive";
			matrix = new Matrix3D();
			primitives.add(p);
			break;
		case STEP:
			partType = LDrawPartType.COMMAND;
			ldrawid = "Colour";
			category = "Commands";
			description = "LDraw !Colour statement";
			primitives.add(p);
			break;
		case TRIANGLE:
			partType = LDrawPartType.GEOM_PRIMITIVE;
			ldrawid = "Triangle";
			category = "Primitives";
			description = "LDraw triangle primitive";
			matrix = new Matrix3D();
			primitives.add(p);
			break;
		case REFERENCE:
			LDrawPart ldp = LDrawPart.getPart(p.getId());
			ldrawid = ldp.ldrawid;
			placed = true;
			matrix = p.getTransformation();
			colorId = p.getColorIndex();
			description = ldp.description;
			author = ldp.author;
			partName = ldp.partName;
			category = ldp.category;
			keywords = ldp.keywords;
			partType = ldp.partType;
			primitives = ldp.getPrimitives();
			break;
		default:
			break;
		}
	}

	public static void listcache() {

		System.out.println("LDraw Part cache: " + partCache.size() + " ----------------------");
		for (LDrawPart p : partCache.values()) {
			System.out.println(p);
		}
		System.out.println("LDraw User Part cache: " + customPartCache.size() + " ----------------------");
		for (LDrawPart p : customPartCache.values()) {
			System.out.println(p);
		}
	}

	@Override
	public String toString() {
		if (partType == LDrawPartType.GEOM_PRIMITIVE) {
			return "LDrawPart [id=" + id + ", ldrawid=" + ldrawid + ", type=" + partType + ", name=" + description
					+ ", primitives=" + primitives.get(0) + "]";
		}
		return "LDrawPart [id=" + id + ", ldrawid=" + ldrawid + ", type=" + partType + ", name=" + description
				+ ", primitives=" + primitives.size() + ", stepNo=" + stepNo + "]";
	}

	private static synchronized int getUniqueId() {

		return ++globalId;
	}

	public static void clearParts() {

		globalPartList.clear();
	}

	public static void clearAll() {
		clearCustomParts();
		clearParts();
	}

	public static LDrawPart newPlacedPart(LDPrimitive p) throws IOException, LDrawException {

		LDrawPart part = new LDrawPart(p);
		part.placed = true;
		globalPartList.put(part.id, part);
		return part;
	}

	public static LDrawPart newPlacedStep(LDPrimitive p) throws IOException, LDrawException {
		LDrawPart part = new LDrawPart(p);
		part.setStepIndex(Integer.valueOf(p.getId()));
		return part;
	}

	public LDrawPart getCopy() {

		LDrawPart pt = new LDrawPart();
		pt.author = author;
		pt.category = category;
		pt.description = description;
		pt.keywords = keywords;
		pt.ldrawid = ldrawid;
		pt.partName = partName;
		pt.partType = partType;
		for (LDPrimitive p : primitives) {
			pt.addPrimitive(p.getClone());
		}
		return pt;
	}

	public static LDrawPart getByGlobalID(int id) {

		return globalPartList.get(id);
	}

	public static void setLdrlib(LDrawLib ldrlib) {

		if (ldrlib == null)
			throw new NullPointerException("[LDrawPart] LDraw library can't be null");
		LDrawPart.ldrlib = ldrlib;
	}

	public int getId() {
		return id;
	}

	public String getPartName() {
		return partName;
	}

	public void setPartName(String partName) {
		if (isOfficial())
			throw new IllegalArgumentException("[LDrawPart.setPartName] Cannot modify official part reference");
		this.partName = partName;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDescription() {
		return description;
	}

	public String getCanonicalDescription() throws IOException {
		if (canonicalDescription == null) {
			canonicalDescription = description;
			if (description.toLowerCase().contains("moved")) {
		// after split, the result is in example: ['~Moved', 'to', '30027a']
				String[] designation = description.split(" ");
				String basename = designation[designation.length-1];
		// get path from library cache
				relativePartPath = ldrlib.getSearchResult(basename+".dat");
				if (relativePartPath != null) {
		// get and parse first line as part description
					LineNumberReader content = new LineNumberReader(new FileReader(new File(relativePartPath)));
					canonicalDescription = LDrawParser.parseDescription(content.readLine());
					content.close();
				}
			}
		}
		return canonicalDescription;
	}	
	
	public String getCategory() {
		return category;
	}

	public String getKeywords() {
		return keywords;
	}

	public boolean isOfficial() {
		return partType == LDrawPartType.OFFICIAL || partType == LDrawPartType.PRIMITIVE
				|| partType == LDrawPartType.SHORTCUT || partType == LDrawPartType.SUBPART;
	}

	/**
	 * @return a string for LDraw brick ID
	 */
	public String getLdrawid() {
		return ldrawid;
	}

	public static void clearCustomParts() {

		customPartCache.clear();
	}

	public List<LDPrimitive> getPrimitives() {
		return primitives;
	}

	/*
	 * Special functions for single primitive reference parts
	 */
	public Matrix3D getTransform() {
		if (placed) {
			return matrix;
		} else
			return new Matrix3D();
	}
	
	public int getColorIndex() {
		return colorId;
	}

	public int getStepIndex() {
		return stepNo;
	}

	public static LDrawPart getLDrawPart(String ldrid) throws IOException, LDrawException {

		if (ldrlib == null) {
			throw new IllegalStateException("[LDrawPart] LDraw library not initialized");
		}
		String id = ldrid.toLowerCase();
		// it is in cache?
		LDrawPart p = partCache.get(id);
		if (p == null) {
		// first seen part
			if (ldrlib.checkPart(id) == LDrawPartType.UNKNOWN) {
		// unknown part
				return null;
			}
			p = new LDrawPart(id);
		// put in cache
			partCache.put(id, p);
			return p;
		} else {
		// part in cache
			return p;
		}
	}

	public static LDrawPart getPart(String name) throws IOException, LDrawException {

		if (name == null || name.length() == 0) {
			throw new LDrawException(LDrawPart.class, "[getPart] Part name is null or empty");
		}
		LDrawPart p = getLDrawPart(name);
		if (p == null) {
			p = getCustomPart(name);
		}
		if (p == null) {
			throw new LDrawException(LDrawPart.class, "[getPart] Unknown part: " + name);
		}
		return p;
	}

	public LDrawPartType getPartType() {
		return partType;
	}

	public void setPartType(LDrawPartType partType) {
		this.partType = partType;
	}

	public void addPrimitive(LDPrimitive p) {

		if (isOfficial()) {
			throw new IllegalArgumentException("[LDrawPart.addPrimitive] Cannot add primitives to official part");
		}
		primitives.add(p);
	}

	public static LDrawPart newCustomPart(String name) throws IOException {

		LDrawPart p = new LDrawPart();
		p.ldrawid = name;
		p.partName = name;
		if (customPartCache.containsKey(name)) {
			throw new IllegalArgumentException("[LDrawPart.newCustomPart] Duplicated part name: " + name);
		}
		customPartCache.put(name, p);
		return p;
	}

	public static LDrawPart getCustomPart(String name) {

		return customPartCache.get(name);
	}

	public static boolean isLdrPart(String ldrid) {

		if (ldrlib == null) {
			throw new IllegalStateException("[LDrawPart] LDraw library not initialized");
		}
		return ldrlib.checkPart(ldrid.toLowerCase()) != LDrawPartType.UNKNOWN;
	}

	public static boolean existsCustomPart(String name) {

		return customPartCache.containsKey(name);
	}

	private void parse(LineNumberReader ldf) throws IOException, LDrawException {

		String l;
		boolean invNext = false;
		boolean isClockWise = false;
		boolean firstLine = true;

		keywords = "";

		if (ldf == null) {
			return;
		}

		while ((l = ldf.readLine()) != null) {
			LDrawCommand cmd = LDrawParser.parseCommand(l);
			switch (cmd) {
			case AUTHOR:
				author = LDrawParser.parseAuthor(l);
				break;
			case AUXLINE:
				primitives.add(LDrawParser.parseLineType5(l));
				break;
			case BFC_CCW:
				isClockWise = false;
				break;
			case BFC_CW:
				isClockWise = true;
				break;
			case BFC_INVERTNEXT:
				invNext = true;
				break;
			case CATEGORY:
				category = LDrawParser.parseCategory(l);
				break;
			case COLOUR:
				primitives.add(LDPrimitive.cmdColour(LDrawParser.parseColour(l)));
				break;
			case COMMENT:
				break;
			case EMPTY:
				break;
			case KEYWORDS:
				keywords += LDrawParser.parseKeywords(l);
				break;
			case LINE:
				primitives.add(LDrawParser.parseLineType2(l));
				break;
			case MPDFILE:
				break;
			case MPDNOFILE:
				break;
			case NAME:
				partName = LDrawParser.parsePartName(l);
				break;
			case FILETYPE:
				partType = LDrawParser.parsePartType(l);
				break;
			case QUAD:
				primitives.add(LDrawParser.parseLineType4(l, isClockWise));
				break;
			case REFERENCE:
				primitives.add(LDrawParser.parseLineType1(l, invNext));
				invNext = false;
				break;
			case SAVE:
				break;
			case STEP:
				break;
			case TRIANGLE:
				primitives.add(LDrawParser.parseLineType3(l, isClockWise));
				break;
			case UNKNOWN:
				break;
			case META_UNKNOWN:
				if (firstLine) {
					description = LDrawParser.parseDescription(l);
					firstLine = false;
				}
				break;
			default:
				break;

			}
		}
	}

	public void setDescription(String parseDescription) {

		description = parseDescription;
	}

	public void setStepIndex(int index) {

		stepNo = index;
	}

	public void setColor(int color) {
		 if(LDrawColor.isLDrawColor(color)) colorId = color;
	}

}
