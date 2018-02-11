/*
	Copyright 2013-2014 Mario Pascucci <mpascucci@gmail.com>
	This file is part of JLDraw.

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

import it.romabrick.ldrawlib.LDPrimitive;
import it.romabrick.ldrawlib.LDrawCommand;
import it.romabrick.ldrawlib.LDrawException;
import it.romabrick.ldrawlib.LDrawParser;
import it.romabrick.ldrawlib.LDrawPart;
import it.romabrick.ldrawlib.LDrawPartType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import javax.swing.SwingWorker;

/*
 * Imports in background an LDraw project file
 * @see javax.swing.SwingWorker
 */
public class ImportLDrawProjectTask extends SwingWorker<Integer, Void> {

	File ldr;
	private LDrawModel mainModel; 		// model imported
	private String internalLog = ""; 	// internal log for errors and warnings
	private boolean warnings = false;

	public ImportLDrawProjectTask(File dat) {

		ldr = dat;
	}

	public LDrawModel getModel() {
		return mainModel;
	}

	public boolean isWarnings() {
		return warnings;
	}

	public String getInternalLog() {
		return internalLog;
	}

	private void addLogLine(String file, int line, String message) {

		warnings = true;
		if (line != 0)
			internalLog += "[" + file + "] line# " + line + "> " + message + "\n";
		else
			internalLog += "[" + file + "] >" + message + "\n";

	}

	@Override
	protected Integer doInBackground() throws IOException {
		
		int count = 0;
		String modelDir, part;
		LDrawPart subModel = null;
		LDPrimitive p = null;
		LDrawPartType partType;

		LineNumberReader lnr = new LineNumberReader(new FileReader(ldr));
		String line;
		int lineNo = 0;
		int currentStepNo = -1;
		while ((line = lnr.readLine()) != null) {
			lineNo++;
		}
		lnr.close();
		// we cheats about complete
		lineNo += 10;
		setProgress(0);
		boolean isMpd = false;
		modelDir = ldr.getParent();
		lnr = new LineNumberReader(new FileReader(ldr));
		while ((line = lnr.readLine()) != null) {
			LDrawCommand type = LDrawParser.parseCommand(line);
			if (type == LDrawCommand.MPDFILE) {
				isMpd = true;
				break;
			}
		}
		lnr.close();
		lnr = new LineNumberReader(new FileReader(ldr));
		boolean isFirstModel = true;
		boolean isMainModel = false;
		boolean isSubModel = false;
		if (isMpd) {
		// gets all sub-model and custom part in MPD
		// and save as LDrawCustomPart
			boolean firstLine = false;
			partType = LDrawPartType.UNKNOWN;
			while ((line = lnr.readLine()) != null) {
				LDrawCommand type = LDrawParser.parseCommand(line);
				try {
					switch (type) {
					case MPDFILE:
					// if a new FILE command is found without NOFILE
					// first...
						if (isSubModel && !isMainModel) {
							subModel.setPartType(partType);
						}
						isMainModel = false;
						isSubModel = false;
						part = LDrawParser.parseMpdFile(line);
						if (isFirstModel) {
							mainModel = LDrawModel.newLDrawModel(part);
							isFirstModel = false;
							isMainModel = true;
						} else {
							isSubModel = true;
							if (!LDrawPart.existsCustomPart(part)) {
								subModel = LDrawPart.newCustomPart(part);
							} else {
					// ------------------- duplicate submodel name
								addLogLine(ldr.getName(), lnr.getLineNumber(),
										"Duplicate sub-model name '" + part + "' in MPD");
								continue;
							}
						}
						firstLine = true;
						if (!isMainModel)
							partType = LDrawPartType.SUBMODEL;
						break;
					case MPDNOFILE:
						if (!isSubModel && !isMainModel) {
							addLogLine(ldr.getName(), lnr.getLineNumber(), "Displaced 'NOFILE' in MPD");
						}
						if (isSubModel) {
							subModel.setPartType(partType);
						}
						isMainModel = false;
						isSubModel = false;
						break;
					case FILETYPE:
						if (isSubModel && !isMainModel) {
							subModel.setPartType(LDrawParser.parsePartType(line));
						}
						break;
					case AUTHOR:
						if (isMainModel) {
							mainModel.setAuthor(LDrawParser.parseAuthor(line));
						} else {
							subModel.setAuthor(LDrawParser.parseAuthor(line));
						}
						break;
					case NAME:
						if (!isMainModel) {
							subModel.setPartName(LDrawParser.parsePartName(line));
						}
						break;
					case TRIANGLE:
					case LINE:
					case AUXLINE:
					case QUAD:
						partType = LDrawPartType.CUSTOM_PART;
						break;
					case COMMENT:
					case EMPTY:
						break;
					case META_UNKNOWN:
						if (firstLine) {
							if (isMainModel) {
								mainModel.setDescription(LDrawParser.parseDescription(line));
							} else if (isSubModel) {
								subModel.setDescription(LDrawParser.parseDescription(line));
							}
							firstLine = false;
						}
						break;
					default:
						if (!isSubModel && !isMainModel) {
							addLogLine(ldr.getName(), lnr.getLineNumber(),
									"Invalid MPD file format: primitive or command outside FILE..NOFILE block:" + line);
						}
						break;
					}
				} catch (LDrawException exc) {
					addLogLine(ldr.getName(), lnr.getLineNumber(), exc.getLocalizedMessage());
				}
				if (isSubModel && !isMainModel) {
					subModel.setPartType(partType);
				}
			}
			lnr.close();
			lnr = new LineNumberReader(new FileReader(ldr));
			// now read model file
			isSubModel = false;
			isFirstModel = true;
			isMainModel = false;
			boolean winding = false;
			boolean invNext = false;
			firstLine = false;
			partType = LDrawPartType.UNKNOWN;
			while ((line = lnr.readLine()) != null) {
				setProgress(lnr.getLineNumber() * 100 / lineNo);
				LDrawCommand type = LDrawParser.parseCommand(line);
				try {
					switch (type) {
					case MPDFILE:
					// in case FILE command is found without NOFILE first
						if (isSubModel) {
					// --------- we are in submodel, close and prepare
					// next submodel
							isSubModel = false;
							if (isMainModel) {
					// end loading main model
								isMainModel = false;
							}
						}
						winding = false;
						invNext = false;
						part = LDrawParser.parseMpdFile(line);
						isSubModel = true;
						if (isFirstModel) {
							isFirstModel = false;
							isMainModel = true;
						} else {
							subModel = LDrawPart.getCustomPart(part);
						}
						firstLine = true;
						break;
					case MPDNOFILE:
						if (isSubModel) {
					// --------- we are in submodel, close and prepare
					// next submodel
							isSubModel = false;
							if (isMainModel) {
						// end loading main model
								isMainModel = false;
							}
						}
						break;	// no parts alone admitted in MPD files, so we
								// are always in submodel
					case BFC_CCW:
						winding = false;
						break;
					case BFC_CW:
						winding = true;
						break;
					case BFC_INVERTNEXT:
						invNext = true;
						break;
					case REFERENCE:
						p = LDrawParser.parseLineType1(line, invNext);
						invNext = false;
						if (LDrawPart.isLdrPart(p.getId()) || LDrawPart.existsCustomPart(p.getId())) {
							if (isMainModel) {
					// add to main model
								mainModel.addPart(LDrawPart.newPlacedPart(p));
								count++;
							} else if (isSubModel) {
					// add to current submodel/custom part
								subModel.addPrimitive(p);
							} else {
								addLogLine(ldr.getName(), lnr.getLineNumber(),
										"Invalid MPD file format: primitive or command outside FILE..NOFILE block:"
												+ line);
							}
						} else {
							addLogLine(subModel.getDescription(), lnr.getLineNumber(),
									"Unknown submodel or part: " + p.getId());
						}
						break;
					case TRIANGLE:
						p = LDrawParser.parseLineType3(line, !winding);
						if (isMainModel) {
					// add to main model
							mainModel.addPart(LDrawPart.newPlacedPart(p));
							count++;
						} else if (isSubModel) {
					// add to current submodel/custom part
							subModel.addPrimitive(p);
						}
						break;
					case AUXLINE:
						p = LDrawParser.parseLineType5(line);
						if (isMainModel) {
					// add to main model
							mainModel.addPart(LDrawPart.newPlacedPart(p));
							count++;
						} else if (isSubModel) {
					// add to current submodel/custom part
							subModel.addPrimitive(p);
						}
						break;
					case LINE:
						p = LDrawParser.parseLineType2(line);
						if (isMainModel) {
					// add to main model
							mainModel.addPart(LDrawPart.newPlacedPart(p));
							count++;
						} else if (isSubModel) {
					// add to current submodel/custom part
							subModel.addPrimitive(p);
						}
						break;
					case QUAD:
						p = LDrawParser.parseLineType4(line, !winding);
						if (isMainModel) {
					// add to main model
							mainModel.addPart(LDrawPart.newPlacedPart(p));
							count++;
						} else if (isSubModel) {
					// add to current submodel/custom part
							subModel.addPrimitive(p);
						}
						break;
					default:
						break;
					}
				} catch (LDrawException exc) {
					addLogLine(ldr.getName(), lnr.getLineNumber(), exc.getLocalizedMessage());
				}
			}
		} else { 
			// it is LDR/DAT format
			mainModel = LDrawModel.newLDrawModel(ldr.getName());
			boolean winding = false;
			boolean invNext = false;
			boolean firstLine = false;
			while ((line = lnr.readLine()) != null) {
				setProgress(lnr.getLineNumber() * 100 / lineNo);
				LDrawCommand type = LDrawParser.parseCommand(line);
				try {
					switch (type) {
					case STEP:
						currentStepNo = lnr.getLineNumber();
						p = LDrawParser.parseStep(line, currentStepNo);
						mainModel.addStep(LDrawPart.newPlacedStep(p));
					case BFC_CCW:
						winding = false;
						break;
					case BFC_CW:
						winding = true;
						break;
					case AUTHOR:
						mainModel.setAuthor(LDrawParser.parseAuthor(line));
						break;
					case BFC_INVERTNEXT:
						invNext = true;
						break;
					case REFERENCE:
						p = LDrawParser.parseLineType1(line, invNext);
						if (!LDrawPart.isLdrPart(p.getId())) {
					// not a LDraw part, checks if it is a submodel
							File ld = new File(modelDir, p.getId());
							if (!ld.exists()) {
					// old part or error in file
								addLogLine(ldr.getName(), lnr.getLineNumber(), "Unknown part: " + p.getId());
							} else {
								count++;
								if (!LDrawPart.existsCustomPart(p.getId())) {
					// a new submodel
									LDrawPart pt = LDrawPart.newCustomPart(p.getId());
									expandSubFile(pt, modelDir, invNext);
								}
								mainModel.addPart(LDrawPart.newPlacedPart(p));
							}
						} else {
							count++;
							LDrawPart ldpart = LDrawPart.newPlacedPart(p);
							ldpart.setStepIndex(currentStepNo);
							mainModel.addPart(ldpart);
						}
						invNext = false;
						break;
					case COLOUR:
						mainModel
								.addPart(LDrawPart.newPlacedPart(LDPrimitive.cmdColour(LDrawParser.parseColour(line))));
						break;
					case AUXLINE:
						p = LDrawParser.parseLineType5(line);
					// add to main model
						mainModel.addPart(LDrawPart.newPlacedPart(p));
						count++;
						break;
					case LINE:
						p = LDrawParser.parseLineType2(line);
					// add to main model
						mainModel.addPart(LDrawPart.newPlacedPart(p));
						count++;
						break;
					case TRIANGLE:
						p = LDrawParser.parseLineType3(line, winding);
						mainModel.addPart(LDrawPart.newPlacedPart(p));
						count++;
						break;
					case QUAD:
						p = LDrawParser.parseLineType4(line, winding);
						mainModel.addPart(LDrawPart.newPlacedPart(p));
						count++;
						break;
					case META_UNKNOWN:
						if (firstLine) {
							mainModel.setDescription(LDrawParser.parseDescription(line));
							firstLine = false;
						}
						break;
					default:
						break;
					}
				} catch (LDrawException exc) {
					addLogLine(ldr.getName(), lnr.getLineNumber(), exc.getLocalizedMessage());
				}
			}
		}
		try {
			lnr.close();
		} catch (IOException ex) {
			;
		}
		return count;
	}

	private void expandSubFile(LDrawPart model, String modelDir, boolean invert) throws IOException {

		String line;

		File ld = new File(modelDir, model.getLdrawid());
		// DBG
		LDPrimitive p;
		boolean winding = false;
		boolean invNext = false;
		boolean firstLine = true;
		LDrawPartType partType = LDrawPartType.SUBMODEL;
		LineNumberReader lnr = new LineNumberReader(new FileReader(ld));
		while ((line = lnr.readLine()) != null) {
			LDrawCommand type = LDrawParser.parseCommand(line);
			try {
				switch (type) {
				case BFC_CCW:
					winding = false;
					break;
				case BFC_CW:
					winding = true;
					break;
				case BFC_INVERTNEXT:
					invNext = true;
					break;
				case REFERENCE:
					p = LDrawParser.parseLineType1(line, invert ^ invNext);
					if (!LDrawPart.isLdrPart(p.getId())) {
				// not a LDraw part, checks if it is a submodel
						File subFile = new File(modelDir, p.getId());
						if (!subFile.exists() || !subFile.canRead()) {
				// old part or error in file
							addLogLine(ldr.getName(), lnr.getLineNumber(), "Unknown part: " + p.getId());
						} else {
							model.addPrimitive(p);
							if (!LDrawPart.existsCustomPart(p.getId())) {
				// a new submodel
								LDrawPart pt = LDrawPart.newCustomPart(p.getId());
								expandSubFile(pt, modelDir, invNext);
							}
						}
					} else {
						model.addPrimitive(p);
					}
					invNext = false;
					break;
				case AUXLINE:
					p = LDrawParser.parseLineType5(line);
				// add to main model
					partType = LDrawPartType.CUSTOM_PART;
					model.addPrimitive(p);
					break;
				case LINE:
					p = LDrawParser.parseLineType2(line);
				// add to main model
					partType = LDrawPartType.CUSTOM_PART;
					model.addPrimitive(p);
					break;
				case TRIANGLE:
					p = LDrawParser.parseLineType3(line, !winding);
					partType = LDrawPartType.CUSTOM_PART;
					model.addPrimitive(p);
					break;
				case QUAD:
					p = LDrawParser.parseLineType4(line, !winding);
					partType = LDrawPartType.CUSTOM_PART;
					model.addPrimitive(p);
					break;
				case META_UNKNOWN:
					if (firstLine) {
						model.setDescription(LDrawParser.parseDescription(line));
						firstLine = false;
					}
					break;
				default:
					break;
				}
			} catch (LDrawException exc) {
				addLogLine(ldr.getName(), lnr.getLineNumber(), exc.getLocalizedMessage());
			}
		}
		model.setPartType(partType);
		lnr.close();
	}

}
