/*
	Copyright 2014 Mario Pascucci <mpascucci@gmail.com>
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

import it.romabrick.matrix3d.Matrix3D;
import it.romabrick.ldrawlib.LDrawColor;
import it.romabrick.ldrawlib.LDPrimitive;
import it.romabrick.ldrawlib.LDrawException;
import it.romabrick.ldrawlib.LDrawPart;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Mario Pascucci
 *
 */


/* 
 * a single LDraw part, identified from its part code, colored and placed in space 
 * with VBOs already full of triangles and lines.
 * Two VBOs: one for polygons, one for wireframe. Total four arrays:
 *  - triangles vertex with normals (floats)
 *  - color per vertex (bytes)
 *  - lines vertex (no normals, floats)
 *  - color per vertex (bytes)
 *  - aux lines vertex (no normals, floats)
 *  - color per vertex (bytes)
 *  
 */
public class LDRenderedPart {
	
	
	
	public LDrawPart placedPart;
	private float[] polyVBO = null; 	// Vertex Buffer Object for triangles
	private float[] wireVBO = null;		// Vertex for  lines
	private float[] auxWireVBO = null;	// aux lines
	private byte[] polyColorVA = null;
	private byte[] wireColorVA = null;
	private byte[] auxWireColorVA = null;
	
	private int triangleName;			// VBO names for OpenGL
	private int triangleColorName;
	private int lineName;
	private int lineColorName;
	private int auxLineName;
	private int auxLineColorName;
	private int triangleVertexCount = 0;
	private int lineVertexCount = 0;
	private int auxLineVertexCount = 0;
	private boolean selected = false;
	private boolean hidden = false;

	private static Map<Integer,LDRenderedPart> renderedParts = new HashMap<Integer,LDRenderedPart>();
	private int tvertexIndex;
	private int tColorIndex;
	private int lvertexIndex;
	private int lcolorIndex;
	private int avertexIndex;
	private int acolorIndex;
	
	
	private LDRenderedPart (LDrawPart p) throws IOException {
		
		placedPart = p;
		generatePartVBOs();
	}
	
	
	public int getId() {
		return placedPart.getId();
	}
	
	
	@Override
	public String toString() {
		return "LDRenderedPart [pp=" + placedPart + ", "
				+ "polyVBO[" + polyVBO.length + "], "
				+ "wireVBO[" + wireVBO.length + "], "
				+ "auxWireVBO[" + auxWireVBO.length + "], "
				+ "polyColorVA[" + polyColorVA.length + "], "
				+ "wireColorVA[" + wireColorVA.length + "], "
				+ "auxWireColorVA[" + auxWireColorVA.length + "] ]";
	}


	
	static public void clearRenderedParts() {
		
		renderedParts.clear();
	}

	
	
	public static void listCache() {
		
		System.out.println("Rendered parts: "+ renderedParts.size() +" ---------------------");
		for (LDRenderedPart p : renderedParts.values()) {
			System.out.println(p);
		}
	}
	
	
	
	private void addPolyVertex(float[] point, float[] normal, Color c) {
		polyVBO[tvertexIndex++] = point[0];
		polyVBO[tvertexIndex++] = point[1];
		polyVBO[tvertexIndex++] = point[2];
		polyVBO[tvertexIndex++] = normal[0];
		polyVBO[tvertexIndex++] = normal[1];
		polyVBO[tvertexIndex++] = normal[2];
		polyColorVA[tColorIndex++] = (byte)c.getRed();
		polyColorVA[tColorIndex++] = (byte)c.getGreen();
		polyColorVA[tColorIndex++] = (byte)c.getBlue();
		polyColorVA[tColorIndex++] = (byte)c.getAlpha();
	}
	
	
	
	private void addLineVertex(float[] point, Color c) {
		
		wireVBO[lvertexIndex++] = point[0];
		wireVBO[lvertexIndex++] = point[1];
		wireVBO[lvertexIndex++] = point[2];
		wireColorVA[lcolorIndex++] = (byte)c.getRed();
		wireColorVA[lcolorIndex++] = (byte)c.getGreen();
		wireColorVA[lcolorIndex++] = (byte)c.getBlue();
		wireColorVA[lcolorIndex++] = (byte)c.getAlpha();

	}


	
	private void addAuxLineVertex(float[] point, Color c) {
		
		auxWireVBO[avertexIndex++] = point[0];
		auxWireVBO[avertexIndex++] = point[1];
		auxWireVBO[avertexIndex++] = point[2];
		auxWireColorVA[acolorIndex++] = (byte)c.getRed();
		auxWireColorVA[acolorIndex++] = (byte)c.getGreen();
		auxWireColorVA[acolorIndex++] = (byte)c.getBlue();
		auxWireColorVA[acolorIndex++] = (byte)c.getAlpha();

	}


	
	/**
	 * A really complex function that uses OpenGL Vertex Buffer Object
	 * specification to create arrays of float for a part vertex and
	 * arrays of byte for colors
	 *
	 * Triangles with attribute array:
	 *  - coordinate (x,y,z)
	 *  - normal (x,y,z)
	 *  for every vertex
	 *  Separate color attribute byte array:
	 *  - color (r,g,b,a)
	 *  for every vertex
	 *
	 * Lines with attribute array:
	 *  - coordinate (x,y,z)
	 *  for every vertex
	 *  Separate color attribute byte array:
	 *  - color (r,g,b,a)
	 *  for every vertex
	 * @throws IOException 
	 * 
	 */ 
	private void renderPart(Collection<LDPrimitive> pt, int color, Matrix3D m, boolean invert) throws IOException {

		float[] nm = new float[3];
		Color pc;

		for (LDPrimitive p : pt) {
			switch (p.getType()) {
			case TRIANGLE:
			// triangle:
				if (p.getColorIndex() == LDrawColor.CURRENT) {
			// current color
					pc = LDrawColor.getById(color).getColor();
				}
				else if (p.getColorIndex() == LDrawColor.EDGE) {
			// edge color
					pc = LDrawColor.getById(color).getEdge();
				}
				else {
			// specific color
					pc = LDrawColor.getById(p.getColorIndex()).getColor();
				}
			// place every vertex with color and normal on array
				if (invert) {
					nm = m.transformNormal(-p.getNormalFV()[0], -p.getNormalFV()[1],-p.getNormalFV()[2]);
				}
				else {
					nm = m.transformNormal(p.getNormalFV()[0], p.getNormalFV()[1],p.getNormalFV()[2]);
				}
				for (int i=0;i<3;i++) {
					addPolyVertex(
							m.transformPoint(p.getPointsFV()[i*3], p.getPointsFV()[i*3+1],p.getPointsFV()[i*3+2]),
							nm, pc);
				}

				break;
			case QUAD:
			// quad, rendered as two adjacent triangles:
				if (p.getColorIndex() == LDrawColor.CURRENT) {
			// current color
					pc = LDrawColor.getById(color).getColor();
				}
				else if (p.getColorIndex() == LDrawColor.EDGE) {
			// edge color
					pc = LDrawColor.getById(color).getEdge();
				}
				else {
			// specific color
					pc = LDrawColor.getById(p.getColorIndex()).getColor();
				}
			// place every vertex with color and normal on array
				if (invert) {
					nm = m.transformNormal(-p.getNormalFV()[0], -p.getNormalFV()[1],-p.getNormalFV()[2]);
				}
				else {
					nm = m.transformNormal(p.getNormalFV()[0], p.getNormalFV()[1],p.getNormalFV()[2]);
				}
				for (int i=0;i<3;i++) {
					addPolyVertex(
							m.transformPoint(p.getPointsFV()[i*3], p.getPointsFV()[i*3+1],p.getPointsFV()[i*3+2]),
							nm, pc);
				}
			// now vertex 0,2,3
				addPolyVertex(
						m.transformPoint(p.getPointsFV()[0*3], p.getPointsFV()[0*3+1],p.getPointsFV()[0*3+2]),
						nm, pc);
				addPolyVertex(
						m.transformPoint(p.getPointsFV()[2*3], p.getPointsFV()[2*3+1],p.getPointsFV()[2*3+2]),
						nm, pc);
				addPolyVertex(
						m.transformPoint(p.getPointsFV()[3*3], p.getPointsFV()[3*3+1],p.getPointsFV()[3*3+2]),
						nm, pc);
				

				break;
			case REFERENCE:
			// sub-part
				int localColor = 0;
				if (p.getColorIndex() == LDrawColor.CURRENT) {
			// current color
					localColor = color;
				}
				else if (p.getColorIndex() == LDrawColor.EDGE) {
			// edge color is illegal in sub-part!
					System.out.println("[LDRenderedPart] Illegal EDGE color in sub-part:\n"+p.toString());
					localColor = color;
				}
				else {
			// specific color
					localColor = p.getColorIndex();
				}
			// get as VBO
				try {
					renderPart(LDrawPart.getPart(p.getId()).getPrimitives(), 
							localColor, p.getTransformation().transform(m), p.isInvert()^invert);
				} catch (LDrawException e) {
					e.printStackTrace();
				}
				break;
			case LINE:
			// it is a line, so place in a wireframe VBO
				if (p.getColorIndex() == LDrawColor.CURRENT) {
			// current color
					pc = LDrawColor.getById(color).getColor();
				}
				else if (p.getColorIndex() == LDrawColor.EDGE) {
			// edge color
					pc = LDrawColor.getById(color).getEdge();
				}
				else {
			// specific color
					pc = LDrawColor.getById(p.getColorIndex()).getColor();
				}
			// place every vertex with color and normal on array
				for (int i=0;i<2;i++) {
					addLineVertex(
							m.transformPoint(p.getPointsFV()[i*3], p.getPointsFV()[i*3+1],p.getPointsFV()[i*3+2]),
							pc);
				}
				break;
			case AUXLINE:
			// it is an aux line, so place in a auxWireframe VBO
				if (p.getColorIndex() == LDrawColor.CURRENT) {
			// current color
					pc = LDrawColor.getById(color).getColor();
				}
				else if (p.getColorIndex() == LDrawColor.EDGE) {
			// edge color
					pc = LDrawColor.getById(color).getEdge();
				}
				else {
			// specific color
					pc = LDrawColor.getById(p.getColorIndex()).getColor();
				}
			// place every vertex with color and normal on array
				for (int i=0;i<2;i++) {
					addAuxLineVertex(
							m.transformPoint(p.getPointsFV()[i*3], p.getPointsFV()[i*3+1],p.getPointsFV()[i*3+2]),
							pc);
				}
				break;
			default:
				break;
			}
		}
	}

	
	/**
	 * A "preventive" function to count vertex needed for a rendered part 
	 * 
	 * @return an int[3] array with: triangle count, line count, aux line count
	 * @throws IOException 
	 */
	private int[] countVertex(Collection<LDPrimitive> lp) throws IOException {

		int triangles = 0;
		int lines = 0;
		int auxlines = 0;
		
		for (LDPrimitive p : lp) {
			switch (p.getType()) {
			case TRIANGLE:
				triangles++;
				break;
			case QUAD:
				triangles += 2;
				break;
			case REFERENCE:
				// get as VBO
				int[] c;
				try {
					c = countVertex(LDrawPart.getPart(p.getId()).getPrimitives());
					triangles += c[0];
					lines += c[1];
					auxlines += c[2];
				} catch (LDrawException e) {
					e.printStackTrace();
				}
				break;
			case LINE:
				lines++;
				break;
			case AUXLINE:
				auxlines++;
				break;
			default:
				break;
			}
		}
		return new int[] {triangles,lines,auxlines};

	}
	
	

	
	private void generatePartVBOs() throws IOException {
		
		// count vertex for triangles and lines
		int[] c = countVertex(placedPart.getPrimitives());

		try {
			polyVBO = new float[c[0]*3*6];
			polyColorVA = new byte[c[0]*3*4];
			wireVBO = new float[c[1]*2*3];
			wireColorVA = new byte[c[1]*2*4];
			auxWireVBO = new float[c[2]*2*3];
			auxWireColorVA = new byte[c[2]*2*4];
		}
		catch (OutOfMemoryError ex) {
			polyVBO = null;
			polyColorVA = null;
			wireVBO = null;
			wireColorVA = null;
			auxWireVBO = null;
			auxWireColorVA = null;
			throw new OutOfMemoryError("Your model is too big to render."); 
		}
		tvertexIndex = 0;
		tColorIndex = 0;
		lvertexIndex = 0;
		lcolorIndex = 0;
		avertexIndex = 0;
		acolorIndex = 0;

		renderPart(placedPart.getPrimitives(),placedPart.getColorIndex(),placedPart.getTransform(),false);

		// normalize normals
		for (int i=3;i<c[0]*3*6;i+=6) {
			float x = polyVBO[i];
			float y = polyVBO[i+1];
			float z = polyVBO[i+2];
			float d = (float) Math.sqrt((float)x*x+y*y+z*z);
			if (d == 0) {
				polyVBO[i] = 0f;
				polyVBO[i+1] = 0f;
				polyVBO[i+2] = 1f;
			}
			else {
				polyVBO[i] = x/d;
				polyVBO[i+1] = y/d;
				polyVBO[i+2] = z/d;
			}
		}
		
	}
	
	
	
	public static LDRenderedPart newRenderedPart(LDrawPart p) throws IOException {
		
		LDRenderedPart part = new LDRenderedPart(p);
		renderedParts.put(p.getId(),part);
		part.polyVBO = part.getTrianglesVBO();
		part.polyColorVA = part.getTriangleColorVA();
		part.wireVBO = part.getWireFrameVBO();
		part.wireColorVA = part.getWireColorVa();
		part.triangleVertexCount = part.polyVBO.length/6;  // 3*coords + 3*normal
		part.lineVertexCount = part.wireVBO.length/3;
		part.auxLineVertexCount = part.auxWireVBO.length/3;
		return part;
	}
	
	
	
	public static LDRenderedPart getByGlobalId(int id) {
		
		return renderedParts.get(id);
	}
	
	
	public static void deleteFromCache(int id) {
		
		renderedParts.remove(id);
	}
	
	

	public void select() {
		
		selected = true;
	}
	
	
	
	public void unSelect() {
		
		selected = false;
	}
	

	public boolean isSelected() {
		return selected;
	}
	
	
	
	public void hide() {
		
		hidden = true;
	}
	
	
	public void show() {
		
		hidden = false;
	}

	
	public boolean isHidden() {
		
		return hidden;
	}

	
	
	public float[] getTrianglesVBO() {
		return polyVBO;
	}

	
	public float[] getWireFrameVBO() {
		return wireVBO;
	}
	
	
	public byte[] getTriangleColorVA() {
		return polyColorVA;
	}
	
	
	public byte[] getWireColorVa() {
		return wireColorVA;
	}


	public float[] getAuxWireFrameVBO() {
		return auxWireVBO;
	}
	
	
	public byte[] getAuxWireColorVa() {
		return auxWireColorVA;
	}


	public int getTriangleVertexCount() {
		return triangleVertexCount;
	}


	public int getLineVertexCount() {
		return lineVertexCount;
	}


	public int getAuxLineVertexCount() {
		return auxLineVertexCount;
	}


	public int getTriangleName() {
		return triangleName;
	}


	public void setTriangleName(int triangleName) {
		this.triangleName = triangleName;
	}


	public int getTriangleColorName() {
		return triangleColorName;
	}


	public void setTriangleColorName(int triangleColorName) {
		this.triangleColorName = triangleColorName;
	}


	public int getLineName() {
		return lineName;
	}


	public void setLineName(int lineName) {
		this.lineName = lineName;
	}


	public int getLineColorName() {
		return lineColorName;
	}


	public void setLineColorName(int lineColorName) {
		this.lineColorName = lineColorName;
	}
	
	
	public int getAuxLineName() {
		return auxLineName;
	}


	public void setAuxLineName(int auxName) {
		this.auxLineName = auxName;
	}


	public int getAuxLineColorName() {
		return auxLineColorName;
	}


	public void setAuxLineColorName(int auxColorName) {
		this.auxLineColorName = auxColorName;
	}
	
	
}
