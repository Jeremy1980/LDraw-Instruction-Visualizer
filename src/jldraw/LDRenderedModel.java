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


import it.romabrick.ldrawlib.LDrawPart;
import it.romabrick.matrix3d.Matrix3D;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * LDraw model rendered in OpenGL-ready vertex array objects
 * 
 * @author Mario Pascucci
 *
 */
public class LDRenderedModel implements Runnable, UncaughtExceptionHandler {
	
	private LDrawModel mainModel;
	private Map<Integer,LDRenderedPart> parts = new HashMap<Integer,LDRenderedPart>();
	private ProgressUpdater updater;
	private LDrawGLDisplay display;
	private boolean completed;
	
	public static boolean singleStepMode= false;
	public static int selectedStepIndex = 0;
	
	public Map<Integer,Matrix3D> stepMatrix = new HashMap<Integer,Matrix3D>();
	
	private LDRenderedModel(LDrawModel model) {
		
		mainModel = model;
	}
	
	
	
	/**
	 * WARNING! May be a LOOOOONG task
	 * @throws IOException 
	 */
	private void render() throws IOException {
		
		if (updater != null) updater.updateStart();
		parts.clear();
		// for every part in model
		for (LDrawPart p : mainModel.getPartList()) {
		// it is in rendered part cache?
			LDRenderedPart pp = LDRenderedPart.getByGlobalId(p.getId());
			if (pp != null) {
		// part exists already rendered
				parts.put(pp.getId(),pp);
			}
			else {
		// render part
				pp = LDRenderedPart.newRenderedPart(p);
				parts.put(pp.getId(),pp);
			}
		}
		if (updater != null) updater.updateDone();
	}
	
	
	
	public static LDRenderedModel newLDRenderedModel(LDrawModel m, LDrawGLDisplay gldisplay) {
		
		LDRenderedModel model = new LDRenderedModel(m);
		// place model to OpenGL coordinate origin
		model.parts = new HashMap<Integer,LDRenderedPart>();
		model.display = gldisplay;
		return model;
	}

	
	public Collection<LDRenderedPart> getParts() {
		return parts.values();
	}

	
	
	@Override
	public void run() {
		
		completed = false;
		try {
			render();
		} catch (IOException e) {
			throw new IllegalArgumentException("[LDRenderedModel] Unable to render: "+e.getLocalizedMessage());
		}
		display.placeModel(this);
		completed = true;
	}
	
	
	
	public Thread getRenderTask(ProgressUpdater p) {
		
		updater = p;
		Thread t = new Thread(this);
		t.setUncaughtExceptionHandler(this);
		return t;
	}



	public boolean isCompleted() {
		return completed;
	}



	@Override
	public void uncaughtException(Thread t, Throwable e) {
		
		display.placeModel(this);
		updater.updateIncomplete();
		e.printStackTrace();
	}
	
	
	
	/**
	 * Show or hide parts /LDRenderedPart instance/ related to sibling step/
	 * 
	 * @param index			position in internal private LinkedHashMap instance
	 * @param standalone	describe if only part connected to selected step
	 * 						can be visible on scene
	 */
	public void toggleVisibiltyForAllParts(int index, boolean standalone) {
		if (parts.size() > 1) {
			LDrawPart step = mainModel.getStep(index);
			if (step != null) {
				int stepIndex = step.getStepIndex();
				for (LDRenderedPart rp : parts.values()) {
					LDrawPart pp = rp.placedPart;
					boolean state = standalone ? pp.getStepIndex() == stepIndex	: pp.getStepIndex() <= stepIndex;
					rp.unSelect();
					if (state) {
						rp.show();
					}
					if (!state) {
						rp.hide();
					}
				}
			}
		}
	}	

}
