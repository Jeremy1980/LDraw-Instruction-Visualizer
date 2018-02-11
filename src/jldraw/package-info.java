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

/**
 * Java implementation of LDraw library parsing and visualization
 * <p>
 * Includes support for OpenGL through JOGL 
 * 
 * @author Mario Pascucci
 */
package jldraw;

 /*
  * TODO: a check for scene rendering speed. Purpose: if drawing is too slow allow 
  * 	client program to switch to low-res primitives; if drawing is fast, allow 
  * 	client program to use hi-res primitives
  * TODO: little editing for part (change color, delete)
  * TODO: use of !COLOUR LDraw command
  * TODO: move LDrawModel in LDrawLib package
  * TODO: make an independent package for 3D rendering
  * 
  * After all components are ready, operations will be: 
  * - read part from file if not exists in part cache
  * - get a copy from part cache and place in space with transformation matrix
  * - store colored and transformed part in an array to speed up iteration for display and part changing
  * - give the array to the OpenGL renderer  
  */


