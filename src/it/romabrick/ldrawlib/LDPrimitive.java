/*
	Copyright 2014 Mario Pascucci <mpascucci@gmail.com>
	Copyright 2014 Jeremy Czajkowski <jeremy.cz@wp.pl>
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

import it.romabrick.matrix3d.Matrix3D;

import java.util.Arrays;

/**
 * 
 * class to store primitive drawing instructions for LDraw parts
 * 
 * @author Mario Pascucci
 *
 */
public class LDPrimitive {

	private LDrawCommand type;
	private String partId;
	private LDrawColor color; // color for !COLOUR primitive
	private int colorIndex; // color for primitive (as LDraw id)
	private float[] normal = null; // normal vector for primitive
	// vertices are assumed co-planar,
	// so you need only one normal
	private float[] vertex = null; /*
									 * vertices stored as follow: p[0] = x1,
									 * p[1] = y1, p[2] = z1 p[3] = x2, p[4] =
									 * y2, p[5] = z2 ...
									 */
	private Matrix3D matrix; // transformation matrix
	private boolean invert;

	private LDPrimitive(LDPrimitive p) {

		type = p.getType();
		partId = p.getId();
		color = p.getColor();
		colorIndex = p.getColorIndex();
		invert = p.isInvert();
		if (p.getNormalFV() != null)
			normal = p.getNormalFV().clone();
		if (p.getPointsFV() != null)
			vertex = p.getPointsFV().clone();
		if (p.getTransformation() != null)
			matrix = p.getTransformation().getCopy();
	}

	// private constructor (as Effective Java 2nd ed. Items 1,13,14,15)
	private LDPrimitive(LDrawCommand type, int c, String id) {

		this.type = type;
		switch (this.type) {
		case LINE:
			vertex = new float[6];
			break;
		case TRIANGLE:
			vertex = new float[9];
			break;
		case QUAD:
			vertex = new float[12];
		case REFERENCE:
			partId = id;
			break;
		case AUXLINE:
			vertex = new float[12];
			break;
		case STEP:
		case UNKNOWN:
			partId = id;
			break;
		default:
			throw new IllegalArgumentException("[LDPrimitive] Primitive type not allowed: " + this.type.toString());
		}
		this.colorIndex = c;
	}

	private LDPrimitive(LDrawColor c) {

		if (c == null) {
			throw new NullPointerException("[LDPrimitive] Primitive color type cannot be null");
		}
		partId = "Local color " + c.getId();
		type = LDrawCommand.COLOUR;
		color = c;
	}

	@Override
	public String toString() {
		return "LDPrimitive [t=" + type + ", partId=" + partId + ", c_idx=" + colorIndex + ", n="
				+ Arrays.toString(normal) + ", p=" + Arrays.toString(vertex) + ", invert=" + invert + ", matrix="
				+ matrix + "]";
	}

	public static LDPrimitive cmdColour(LDrawColor c) {

		return new LDPrimitive(c);
	}

	public static LDPrimitive errorPrimitive(String why) {

		return new LDPrimitive(LDrawCommand.UNKNOWN, LDrawColor.INVALID_COLOR, why);
	}

	public static LDPrimitive newLine(int color, float x1, float y1, float z1, float x2, float y2, float z2) {

		LDPrimitive l = new LDPrimitive(LDrawCommand.LINE, color, null);
		l.vertex[0] = x1;
		l.vertex[1] = y1;
		l.vertex[2] = z1;
		l.vertex[3] = x2;
		l.vertex[4] = y2;
		l.vertex[5] = z2;
		return l;
	}

	public static LDPrimitive newAuxLine(int color, float x1, float y1, float z1, float x2, float y2, float z2,
			float x3, float y3, float z3, float x4, float y4, float z4) {

		LDPrimitive l = new LDPrimitive(LDrawCommand.AUXLINE, color, null);
		l.vertex[0] = x1;
		l.vertex[1] = y1;
		l.vertex[2] = z1;
		l.vertex[3] = x2;
		l.vertex[4] = y2;
		l.vertex[5] = z2;
		l.vertex[6] = x3;
		l.vertex[7] = y3;
		l.vertex[8] = z3;
		l.vertex[9] = x4;
		l.vertex[10] = y4;
		l.vertex[11] = z4;
		return l;
	}

	/*
	 * compute normal, not normalized.
	 */
	public static float[] calcNormal(float x, float y, float z, float x1, float y1, float z1, float x2, float y2,
			float z2) {

		float v1x, v1y, v1z, v2x, v2y, v2z, xn, yn, zn;

		v1x = x1 - x;
		v1y = y1 - y;
		v1z = z1 - z;
		v2x = x2 - x1;
		v2y = y2 - y1;
		v2z = z2 - z1;
		xn = v1y * v2z - v1z * v2y;
		yn = v1z * v2x - v1x * v2z;
		zn = v1x * v2y - v1y * v2x;
		return new float[] { xn, yn, zn };
	}

	public static LDPrimitive newTriangle(int color, boolean isClockWise, float x1, float y1, float z1, float x2,
			float y2, float z2, float x3, float y3, float z3) {

		LDPrimitive l = new LDPrimitive(LDrawCommand.TRIANGLE, color, null);
		l.vertex[0] = x1;
		l.vertex[1] = y1;
		l.vertex[2] = z1;
		l.vertex[3] = x2;
		l.vertex[4] = y2;
		l.vertex[5] = z2;
		l.vertex[6] = x3;
		l.vertex[7] = y3;
		l.vertex[8] = z3;
		if (!isClockWise) {
			l.normal = calcNormal(x1, y1, z1, x2, y2, z2, x3, y3, z3);
		} else {
			l.normal = calcNormal(x3, y3, z3, x2, y2, z2, x1, y1, z1);
		}
		return l;
	}

	public static LDPrimitive newQuad(int color, boolean isClockWise, float x1, float y1, float z1, float x2, float y2,
			float z2, float x3, float y3, float z3, float x4, float y4, float z4) {

		LDPrimitive l = new LDPrimitive(LDrawCommand.QUAD, color, null);
		l.vertex[0] = x1;
		l.vertex[1] = y1;
		l.vertex[2] = z1;
		l.vertex[3] = x2;
		l.vertex[4] = y2;
		l.vertex[5] = z2;
		l.vertex[6] = x3;
		l.vertex[7] = y3;
		l.vertex[8] = z3;
		l.vertex[9] = x4;
		l.vertex[10] = y4;
		l.vertex[11] = z4;

		if (!isClockWise) {
			l.normal = calcNormal(x1, y1, z1, x2, y2, z2, x3, y3, z3);
		} else {
			l.normal = calcNormal(x3, y3, z3, x2, y2, z2, x1, y1, z1);
		}
		return l;
	}

	public static LDPrimitive newPart(int color, String id, boolean invert, float a, float b, float c, float d, float e,
			float f, float g, float h, float i, float x, float y, float z) {

		LDPrimitive l = new LDPrimitive(LDrawCommand.REFERENCE, color, id);
		l.invert = invert;
		l.matrix = new Matrix3D(a, b, c, d, e, f, g, h, i, x, y, z);
		return l;
	}

	/**
	 * 
	 * @param id	unique identification number.
	 * @return 		a single step in an Instruction book.
	 */
	public static LDPrimitive newStep(String id) {
		return new LDPrimitive(LDrawCommand.STEP, LDrawColor.INVALID_COLOR, id);
	}

	public LDPrimitive getClone() {

		return new LDPrimitive(this);
	}

	public LDrawColor getColor() {

		return color;
	}

	public int getColorIndex() {

		return colorIndex;
	}

	public String getId() {

		return partId;
	}

	public Matrix3D getTransformation() {

		return matrix;
	}

	public void setTransformation(Matrix3D m) {

		matrix = m;
	}

	public boolean isInvert() {

		return invert;
	}

	public LDrawCommand getType() {

		return type;
	}

	public float[] getPointsFV() {

		return vertex;
	}

	public float[] getNormalFV() {

		return normal;
	}

}
