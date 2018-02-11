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

import java.util.logging.Level;
import java.util.logging.Logger;

public class LDlogger {

	private static Logger l = null;

	public static void setLogger(Logger l) {

		LDlogger.l = l;

	}

	public static void warn(String file, int line, String msg) {

		if (l != null) {
			l.log(Level.WARNING, "[LDrawLib] WARN (" + file + ") line#" + line + " -> " + msg);
		}
	}

	public static void warn(String msg) {

		if (l != null) {
			l.log(Level.WARNING, "[LDrawLib] WARN -> " + msg);
		}
	}

	public static void error(String file, int line, String msg) {

		if (l != null) {
			l.log(Level.SEVERE, "[LDrawLib] ERROR (" + file + ") line#" + line + " -> " + msg);
		}
	}

	public static void error(String msg) {

		if (l != null) {
			l.log(Level.SEVERE, "[LDrawLib] ERROR -> " + msg);
		}
	}

	public static void info(String file, int line, String msg) {

		if (l != null) {
			l.log(Level.INFO, "[LDrawLib] INFO (" + file + ") line#" + line + " -> " + msg);
		}
	}

	public static void info(String msg) {

		if (l != null) {
			l.log(Level.INFO, "[LDrawLib] INFO -> " + msg);
		}
	}

}
