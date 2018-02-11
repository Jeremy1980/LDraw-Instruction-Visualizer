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

/**
 * Part picking mode
 * 
 * NONE: selected part replace current selection. If no part selected, clear current selection
 * ADD: add selected part to current selection
 * TOGGLE: if part is in current selection remove it, else add
 * 
 * @author Mario Pascucci
 *
 */
public enum PickMode {
	NONE, ADD, TOGGLE, POPUP_MENU
}
