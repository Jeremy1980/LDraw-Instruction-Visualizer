/*
	Copyright 2014 Mario Pascucci <mpascucci@gmail.com>
	This file is part of Matrix3D

	Matrix3D is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	Matrix3D is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with Matrix3D.  If not, see <http://www.gnu.org/licenses/>.

*/


package it.romabrick.matrix3d;

/**
 * @author Mario Pascucci
 *
 */

/* utility for 3D matrix transformation */
public class Matrix3D {

	// 3D transformation matrix initialized to "identity" (NOP)
	private float x=0,y=0,z=0;		// 3D displacement
	private float a=1,b=0,c=0,
				d=0,e=1,f=0,
				g=0,h=0,i=1;	// transformation matrix 3x3
	
	/*
	 * 
	 * LDraw uses this matrix:
	 * 
	 *  a b c x 		u
	 *  d e f y   *   	v
	 *  g h i z			w
	 *  0 0 0 1			1
	 *  
	 *  and point(u,v,w) calculation is (multiplied as 4 component vertical vector):
	 *  x' = au+bv+cw+x
	 *  y' = du+ev+fw+y
	 *  z' = gu+hv+iw+z
	 *  
	 *  all matrix operation MUST be pre-multiplied
	 *  
	 */

	
	
	/*
	 *  pre-computed matrix
	 *  where:
	 *  
	 *  a b c x
	 *  d e f y
	 *  g h i z
	 *  0 0 0 1
	 */
	public Matrix3D(
			float a, float b, float c,
			float d, float e, float f,
			float g, float h, float i,
			float x, float y, float z) {
		
		this.x = x;
		this.y = y;
		this.z = z;
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		this.f = f;
		this.g = g;
		this.h = h;
		this.i = i;
		
	}
	
	
	public float[] getAsOpenGLMatrix() {
		
		return new float[] {a,d,g,0f,
							b,e,h,0f,
							c,f,i,0f,
							x,y,z,1f};
	}

	
	// simple translation matrix or starting point
	public Matrix3D(float x, float y, float z) {
		
		this.x = x;
		this.y = y;
		this.z = z;

	}

	
	// origin matrix
	public Matrix3D() {
		
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	
	@Override
	public String toString() {
		return "Matrix3D\n[a=" + a + ",\tb=" + b + ",\tc=" + c + ",\tx=" + x +"\t]\n"
				+ "|d=" + d + ",\te=" + e + ",\tf=" + f + ",\ty=" + y +  "\t|\n" +
				"[g=" + g + ",\th=" + h + ",\ti=" + i + ",\tz=" + z + "\t]";
	}


	/* 
	 * move to a new coord
	 * base matrix:  
	 *  1   0   0  tx
     *  0   1   0  ty
     *  0   0   1  tz
     *  0   0   0  1
	 *  
	 * resulting matrix:
	 * 
	 *   a    b    c
	 *   d    e    f
	 *   g    h    i
	 *  tx+x ty+y tz+z
	 */
	public Matrix3D moveTo(float tx, float ty, float tz) {
		
		return new Matrix3D(
				a, b, c,
				d, e, f,
				g, h, i,
				tx + x, ty + y, tz + z
				);
	}
	

	/* 
	 * fast scaling
	 * resulting matrix:
	 * 
	 *   a*sx b*sx c*sx x*sx 
	 *   d*sy e*sy f*sy y*sy
	 *   g*sz h*sz i*sz z*sz
	 *   
	 */
	public Matrix3D scale(float sx, float sy, float sz) {

		return new Matrix3D(
				a * sx, b * sx, c * sx,
				d * sy, e * sy, f * sy,
				g * sz, h * sz, i * sz,
				x*sx, y*sy, z*sz
				);
	}

	
	/* 
	 * fast X rotation
	 * base matrix:  
	 *  1   0   0
     *  0   C   S
     *  0  -S   C
	 *  
	 */
	public Matrix3D rotateX(float angle) {
		
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		return new Matrix3D(
				a, b, c,
				 d*cos+g*sin,  e*cos+h*sin,  f*cos+i*sin,
				-d*sin+g*cos, -e*sin+h*cos, -f*sin+i*cos,
				x, y*cos+z*sin, -y*sin+z*cos
				);
	}
	
	
	/*
	 * matrix computation for 90 degree rotation on X axis
	 * base matrix:  
	 *  1   0   0
     *  0   0   1
     *  0  -1   0
	 */
	public Matrix3D rotateX90() {
		
		return new Matrix3D(
				a, b, c,
				g, h, i,
				-d, -e, -f,
				x, z, -y
				);
	}
	
	
	/*
	 * matrix computation for 180 degree rotation on X axis
	 * base matrix:  
	 *  1   0   0
     *  0  -1   0
     *  0   0  -1
	 */
	public Matrix3D rotateX180() {
		
		return new Matrix3D(
		a,  b,  c,
		-d, -e, -f,
		-g, -h, -i,
		x, -y, -z
		);
	}
	
	
	/*
	 * matrix computation for 270 degree rotation on X axis
	 * base matrix:  
	 *  1   0   0
     *  0   0  -1
     *  0   1   0
	 *  
	 */
	public Matrix3D rotateX270() {
		
		return new Matrix3D(
				a, b, c,
				-g, -h, -i,
				d, e, f,
				x, -z, y
				);
	}

	
	/*
	 * fast Y rotation
	 * base matrix:  
	 *  C  0  -S
     *  0  1   0
     *  S  0   C
	 */
	public Matrix3D rotateY(float angle) {
		
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		return new Matrix3D(
				a*cos-g*sin, b*cos-h*sin, c*cos-i*sin,
				d, e, f,
				a*sin+g*cos, b*sin+h*cos, c*sin+i*cos,
				x*cos-z*sin, y, x*sin+z*cos
				);
	}
	
	
	/* 
	 * matrix computation for 90 degree rotation on Y axis
	 * base matrix:  
	 *  0  0  -1
     *  0  1   0
     *  1  0   0
	 */
	public Matrix3D rotateY90() {
		
		return new Matrix3D(
				-g, -h, -i,
				d, e, f,
				a, b, c,
				-z, y, x
				);
	}
	
	
	/* 
	 * matrix computation for 180 degree rotation on Y axis
	 * base matrix:  
	 * -1  0   0
     *  0  1   0
     *  0  0  -1
	 *  
	 */
	public Matrix3D rotateY180() {
		
		return new Matrix3D(
				-a, -b, -c,
				d, e, f,
				-g, -h, -i,
				-x, y, -z
				);
	}
	
	
	/* 
	 * matrix computation for 270 degree rotation on Y axis
	 * base matrix:  
	 *  0  0   1
     *  0  1   0
     * -1  0   0
	 *  
	 */
	public Matrix3D rotateY270() {
		
		return new Matrix3D(
				g, h, i,
				d, e, f,
				-a, -b, -c,
				z, y, -x
				);
	}
	
	
	
	/*
	 * fast Z rotation
	 * base matrix:  
	 *  C   S   0
     * -S   C   0
     *  0   0   1
	 */
	public Matrix3D rotateZ(float angle) {
		
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		return new Matrix3D(
				a*cos+d*sin, b*cos+e*sin, c*cos+f*sin,
				-a*sin+d*cos, -b*sin+e*cos, -c*sin+f*cos,
				g, h, i,
				x*cos+y*sin, -x*sin+y*cos, z
				);
	}
	
	
	/* 
	 * matrix computation for 90 degree rotation on Z axis
	 * base matrix:  
	 *  0   1   0
     * -1   0   0
     *  0   0   1
	 */
	public Matrix3D rotateZ90() {
		
		return new Matrix3D(
				d, e, f,
				-a, -b, -c,
				g, h, i,
				y, -x, z
				);		
	}

	
	/* 
	 * matrix computation for 180 degree rotation on Z axis
	 * base matrix:  
	 * -1   0   0
     *  0  -1   0
     *  0   0   1
	 */ 
	public Matrix3D rotateZ180() {
		
		return new Matrix3D(
				-a, -b, -c,
				-d, -e, -f,
				g, h, i,
				-x, -y, z
				);
	}
	
	
	/* 
	 * matrix computation for 270 degree rotation on Z axis
	 * base matrix:  
	 *  0  -1   0
     *  1   0   0
     *  0   0   1
	 *  
	 * resulting matrix:
	 *  b  -a   c
	 *  e  -d   f
	 *  h  -g   i
	 *  y  -x   z 
	 */
	public Matrix3D rotateZ270() {
		
		return new Matrix3D(
				-d, -e, -f,
				a, b, c,
				g, h, i,
				-y, x, z
				);
	}
	
	
	// applies a generic transformation to the matrix, 
	// specified by the matrix m
	// using pre-multiplication i.e.:
	// A * B ->  B.transform(A)
	public Matrix3D transform(Matrix3D m) {
		
		return new Matrix3D(

				a*m.a + d*m.b + g*m.c,
				b*m.a + e*m.b + h*m.c,
				c*m.a + f*m.b + i*m.c,
				a*m.d + d*m.e + g*m.f,
				b*m.d + e*m.e + h*m.f,
				c*m.d + f*m.e + i*m.f,
				a*m.g + d*m.h + g*m.i,
				b*m.g + e*m.h + h*m.i,
				c*m.g + f*m.h + i*m.i,
				x*m.a+y*m.b+z*m.c+m.x,
				x*m.d+y*m.e+z*m.f+m.y,
				x*m.g+y*m.h+z*m.i+m.z 
				);
	}
	

	
	public float[] transformPoint(float px, float py, float pz) {
		
		float tx,ty,tz;
		
		tx = a * px + b * py + c * pz + x;
		ty = d * px + e * py + f * pz + y;
		tz = g * px + h * py + i * pz + z;
		
		return new float[] {tx,ty,tz};
	}
	
	
	public float[] transformNormal(float px, float py, float pz) {
		
		float tx,ty,tz;
		
		tx = a * px + b * py + c * pz;
		ty = d * px + e * py + f * pz;
		tz = g * px + h * py + i * pz;
		
		return new float[] {tx,ty,tz};
	}
	
	
	//////////////////////////////////////
	// static methods returns most used
	// basic transform matrix 
	
	// x-axis rotation 
	static Matrix3D getRotateX(float angle) {
		
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		
		Matrix3D m = new Matrix3D(
				1,0,0,
				0,cos,sin,
				0,-sin,cos,
				0,0,0
				);
		return m;
	}
	
	// y-axis rotation
	static Matrix3D getRotateY(float angle) {
		
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		
		Matrix3D m = new Matrix3D(
				cos,0,-sin,
				0,1,0,
				sin,0,cos,
				0,0,0
				);
		return m;
	}
	
	
	// z-axis rotation 
	static Matrix3D getRotateZ(float angle) {
		
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);
		
		Matrix3D m = new Matrix3D(
				cos,sin,0,
				-sin,cos,0,
				0,0,1,
				0,0,0
				);
		return m;
	}
	
	
	// get a scaling matrix (complete matrix) 
	static Matrix3D getScale(float sx, float sy, float sz) {
		
		Matrix3D m = new Matrix3D(
				sx,0f,0f,
				0f,sy,0f,
				0f,0f,sz,
				0f,0f,0f
				);
		return m;
	}
	
	
	/////////////////////////
	// some frequently used matrices
	
	static Matrix3D rotX90() {
		
		return new Matrix3D(
				1,0,0,
				0,0,1,
				0,-1,0,
				0,0,0
				);
	}

	static Matrix3D rotX180() {
		
		return new Matrix3D(
				1,0,0,
				0,-1,0,
				0,0,-1,			
				0,0,0
				);
	}

	static Matrix3D rotX270() {
		
		return new Matrix3D(
				1,0,0,
				0,0,-1,
				0,1,0,
				0,0,0
				);
	}

	static Matrix3D rotY90() {
		
		return new Matrix3D(
				0,0,-1,
				0,1,0,
				1,0,0,			
				0,0,0
				);
	}

	static Matrix3D rotY180() {
		
		return new Matrix3D(
				-1,0,0,
				0,1,0,
				0,0,-1,			
				0,0,0
				);
	}

	static Matrix3D rotY270() {
		
		return new Matrix3D(
				0,0,1,
				0,1,0,
				-1,0,0,			
				0,0,0
				);
	}

	static Matrix3D rotZ90() {
		
		return new Matrix3D(
				0,1,0,
				-1,0,0,
				0,0,1,
				0,0,0
				);
	}

	static Matrix3D rotZ180() {
		
		return new Matrix3D(
				-1,0,0,
				0,-1,0,
				0,0,1,
				0,0,0
				);
	}

	static Matrix3D rotZ270() {
		
		return new Matrix3D(
				0,-1,0,
				1,0,0,
				0,0,1,
				0,0,0
				);
	}


	public Matrix3D getCopy() {
		
		return new Matrix3D(a,b,c,d,e,f,g,h,i,x,y,z);
	}

	

}
