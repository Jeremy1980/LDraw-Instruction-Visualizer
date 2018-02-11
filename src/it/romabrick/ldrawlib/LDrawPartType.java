/*
	Copyright 2014 Mario Pascucci <mpascucci@gmail.com>
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

/**
 * Type of LDraw Part from library or custom/subpart/primitive
 * @author Mario Pascucci
 *
 */
public enum LDrawPartType {
	/** official part from library */
	OFFICIAL,			
	/** official subpart from library (in s\ folder) */
	SUBPART,
	/** official primitive from library (in p\ folder) */
	PRIMITIVE,
	/** unofficial part from unofficial library */
	UNOFFICIAL,			
	/** unofficial subpart from unofficial library (in s\ folder) */
	UNOFF_SUB,			
	/** unofficial primitive from unofficial library (in p\ folder) */
	UNOFF_PRIM,			
	/** basic geometric primitive (line, triangle, quad...) */
	GEOM_PRIMITIVE,		
	/** rendering comand/option (STEP, !COLOUR, SAVE, ecc...) */
	COMMAND,			
	/** user subpart with primitives */
	CUSTOM_PART,		
	/** unknown part type */
	UNKNOWN,			
	/** an LDraw model */
	MODEL,
	/** a submodel/subfile */
	SUBMODEL,
	/** a shortcut, i.e. a grouped parts in one main part like 73200.dat */
	SHORTCUT,
	/** unofficial shortcut */
	UNOFF_SHORTCUT		
}
