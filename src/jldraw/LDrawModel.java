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

import it.romabrick.ldrawlib.LDPrimitive;
import it.romabrick.ldrawlib.LDrawException;
import it.romabrick.ldrawlib.LDrawPart;
import it.romabrick.ldrawlib.LDrawPartType;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * A complete model in LDraw format (LDR or MPD)
 * 
 * @author Mario Pascucci
 *
 */
public class LDrawModel {

	private String name;
	private String fileName;
	private String description;
	private String Author;
	private Map<Integer, LDrawPart> partList = new TreeMap<Integer, LDrawPart>();
	private Map<Integer, LDrawPart> stepList = new LinkedHashMap<Integer, LDrawPart>();

	private LDrawModel(String name) {

		this.name = name.toLowerCase();
		this.fileName = name;
	}

	public void listParts(boolean both) {

		System.out.println("LDraw Models: " + partList.size() + " --------------");
		for (LDrawPart m : partList.values()) {
			System.out.println(m.toString());
		}
		
		if (both) {
			System.out.println("LDraw Steps: " + stepList.size() + " --------------");
			for (LDrawPart m : stepList.values()) {
				System.out.println(m.toString());
			}
		}
	}

	@Override
	public String toString() {
		return "LDrawModel [name=" + name + ", filename=" + fileName + ", partList=" + partList.size() + "]";
	}

	public static void clearModels() {

		LDrawPart.clearCustomParts();
		LDrawPart.clearParts();
		LDRenderedPart.clearRenderedParts();
	}

	public static LDrawModel newLDrawModel(String name) {

		LDrawModel m = new LDrawModel(name);
		return m;
	}

	public void addPart(LDrawPart part) {

		if (part != null) partList.put(part.getId(), part);
	}

	public void addStep(LDrawPart part) {

		if (part != null) stepList.put(stepList.size() + 1, part);
	}

	/**
	 * Replaces submodel identified by global ID id with its parts
	 * 
	 * @param id 	global id of submodel to replace
	 * 
	 * @return number of parts replaced
	 * @throws IOException
	 * @throws LDrawException
	 */
	public int replaceParts(int id) throws IOException, LDrawException {

		LDrawPart replaced = LDrawPart.getByGlobalID(id);
		if (replaced.getPartType() != LDrawPartType.SUBMODEL)
			return 0;
		int i = 0;
		if (partList.containsKey(id)) {
			partList.remove(id);
			for (LDPrimitive p : replaced.getPrimitives()) {
				LDPrimitive pc = p.getClone();
				pc.setTransformation(pc.getTransformation().transform(replaced.getTransform()));
				LDrawPart pp = LDrawPart.newPlacedPart(pc);
				partList.put(pp.getId(), pp);
				i++;
			}
		}
		return i;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {

		return fileName;
	}

	public Collection<LDrawPart> getPartList() {
		return partList.values();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAuthor() {
		return Author;
	}

	public void setAuthor(String author) {
		Author = author;
	}

	public int stepCount() {
		return stepList.size();
	}

	public LDrawPart getStep(int index) {
		try {
			return stepList.get(index);
		} catch (ClassCastException | NullPointerException ex) {
			return null;
		}
	}


}
