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

/**
 * Low level access to LDraw part file libraries
 * <p>
 * Opens and gets parts from library files (official and unofficial) 
 * right from ZIP file.
 * See http://www.ldraw.org/ for LDraw file specifications
 * 
 * @author Mario Pascucci
 *
 */
package it.romabrick.ldrawlib;



/*
 * LDraw parts with anomalies
 * 
 * 3842a.dat  reports a BFC CERTIFY INVERTNEXT (illegal)
 * 3842b.dat  reports a BFC CERTIFY INVERTNEXT (illegal)
 * 
 * s/3068s101.dat  	uses color 391 (unknown)
 * 973psk.dat		uses color 391 (unknown)
 * 
 * 4150p02.dat		uses color 354 (unknown)
 * 973p7b.dat		uses color 354 (unknown)
 * 
 */

