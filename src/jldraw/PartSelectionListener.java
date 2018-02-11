/*
	Copyright 2014 Mario Pascucci <mpascucci@gmail.com>
	Copyright 2017 Jeremy Czakowski <jeremy.cz@wp.pl>
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

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;

/**
 * Interface to allow part selection handling from user application 
 * 
 * @author Mario Pascucci
 *
 */
public interface PartSelectionListener {

	public Collection<Integer> selectedParts = new HashSet<Integer>();
	
	public void partPicked(int partId, PickMode mode, MouseEvent event);
	
}
