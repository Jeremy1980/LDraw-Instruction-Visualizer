/*
Copyright 2017-2018 Jeremy Czajkowski <jeremy.cz@wp.pl>
	This file is part of LDIVisualizer

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

package ldivisualizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import it.romabrick.ldrawlib.LDPrimitive;
import it.romabrick.ldrawlib.LDrawException;
import it.romabrick.ldrawlib.LDrawLib;
import it.romabrick.ldrawlib.LDrawPart;
import jldraw.BusyDialog;
import jldraw.ImportLDrawProjectTask;
import jldraw.LDRenderedModel;
import jldraw.LDRenderedPart;
import jldraw.LDrawGLDisplay;
import jldraw.LDrawModel;
import jldraw.MyPreferences;
import jldraw.PartSelectionListener;
import jldraw.PickMode;
import jldraw.StatusIcon;
import ldivisualizer.dialog.AboutDialog;
import ldivisualizer.dialog.PreferencesDialog;
import ldivisualizer.dialog.SplashScreen;
import ldivisualizer.toolbox.IOAssistant;
import ldivisualizer.toolbox.MenuAssistant;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.InputEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JList;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.Cursor;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyAdapter;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Panel;
import java.awt.Point;

import java.awt.FlowLayout;
import javax.swing.JSeparator;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;

public class MainWindow implements PartSelectionListener {

	protected static final String appShortName = "LDIVisualizer";

	private static MyPreferences prefsObject;

	private JFrame frame;
	private JList<String> stepList;

	private JMenuBar menuBar;
	private static JMenu mnRecent;

	protected LDrawModel currentModel;
	protected LDRenderedModel renderedModel;
	protected StatusIcon status;

	private Collection<Integer> hiddenParts = new ArrayList<Integer>();
	
	private ButtonGroup axisButtonGroup = new ButtonGroup();

	private JLabel lblNotificationArea;
	private JLabel lblPartInformation;
	private JLabel lblListNotice;

	protected static LDrawGLDisplay glDisplay;
	protected static LDrawLib ldrawFolder;

	/**
	 * Create the application. Initialize the contents of the frame.
	 */
	public MainWindow() {
		frame = new JFrame(appShortName);
		frame.setIconImage(
				Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/icons/about/icon-about-32.png")));

		String[] geometry = prefsObject.getList(IOAssistant.KEY_WINDOWGEOMETRY);
		int x, y;
		try {
			x = Integer.valueOf(geometry[0]);
			y = Integer.valueOf(geometry[1]);
		} catch (Exception ex) {
			x = 10;
			y = 10;
		}

		frame.setMinimumSize(new Dimension(800, 650));
		frame.setBounds(new Rectangle(x, y, 800, 600));
		frame.addWindowListener(new WindowAdapter() {
			/**
			 * @category JFrame
			 */
			@Override
			public void windowClosing(WindowEvent event) {
				Window win = event.getWindow();
				JFrame jfrm = (JFrame) event.getSource();
				if (win.isVisible() && jfrm.getExtendedState() == JFrame.NORMAL) {
					Rectangle bounds = win.getBounds();
					int[] geometry = { bounds.x, bounds.y, bounds.width, bounds.height };

					prefsObject.putList(IOAssistant.KEY_WINDOWGEOMETRY, geometry);
				}
				prefsObject.putList(IOAssistant.KEY_RECENTPATHS, MenuAssistant.propertyList(mnRecent));
				prefsObject.savePreferences();
			}
		});
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// glDisplay
		frame.getContentPane().add(glDisplay.getCanvas(), BorderLayout.CENTER);

		// stepList
		stepList = new JList<String>();
		frame.getContentPane().add(stepList, BorderLayout.NORTH);
		stepList.addMouseListener(new MouseAdapter() {
			/**
			 * @category JList
			 */
			@Override
			public void mouseReleased(MouseEvent event) {
				updateWorkspace();
			}
			@Override
			public void mousePressed(MouseEvent e) {
				updateWorkspace();
			}
		});
		stepList.addKeyListener(new KeyAdapter() {
			/**
			 * @category JList
			 */
			@Override
			public void keyReleased(KeyEvent event) {
				updateWorkspace();		
			}
		});
		stepList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		stepList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		// pnlToolBar
		Panel pnlToolBar = new Panel();
		frame.getContentPane().add(pnlToolBar, BorderLayout.EAST);
		GridBagLayout gbl_pnlToolBar = new GridBagLayout();
		gbl_pnlToolBar.columnWidths = new int[] {30, 130};
		gbl_pnlToolBar.rowHeights = new int[] { 560, 0 };
		gbl_pnlToolBar.columnWeights = new double[] { 0.0, 0.0 };
		gbl_pnlToolBar.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		pnlToolBar.setLayout(gbl_pnlToolBar);
		
		// scroll Panel
		JScrollPane scrollPane = new JScrollPane(stepList);
		scrollPane.setPreferredSize(new Dimension(130, 130));
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 0;
		gbc_scrollPane.anchor = GridBagConstraints.NORTH;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		pnlToolBar.add(scrollPane, gbc_scrollPane);
		
		// scroll Panel Header
		Panel panel = new Panel();
		scrollPane.setColumnHeaderView(panel);
						panel.setLayout(new BoxLayout(panel, 0));
						
								lblListNotice = new JLabel(" ");
								lblListNotice.setHorizontalAlignment(SwingConstants.LEFT);
								lblListNotice.setHorizontalTextPosition(SwingConstants.LEFT);
								lblListNotice.setMaximumSize(new Dimension(80, 14));
								lblListNotice.setMinimumSize(new Dimension(80, 14));
								lblListNotice.setPreferredSize(new Dimension(80, 14));
								lblListNotice.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
								panel.add(lblListNotice);
						
						JSeparator separator = new JSeparator();
						separator.setRequestFocusEnabled(false);
						separator.setPreferredSize(new Dimension(7, 14));
						separator.setOrientation(SwingConstants.VERTICAL);
						separator.setMinimumSize(new Dimension(7, 14));
						panel.add(separator);
						
						

		Panel pnlStatusBar = new Panel();
		FlowLayout flowLayout = (FlowLayout) pnlStatusBar.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		frame.getContentPane().add(pnlStatusBar, BorderLayout.SOUTH);

		lblNotificationArea = new JLabel("Started.");
		lblNotificationArea.setMinimumSize(new Dimension(100, 14));
		lblNotificationArea.setMaximumSize(new Dimension(400, 14));
		lblNotificationArea.setPreferredSize(new Dimension(400, 14));
		pnlStatusBar.add(lblNotificationArea);
		lblNotificationArea.setFont(new Font("Arial Black", Font.PLAIN, 12));
		lblNotificationArea.setHorizontalAlignment(SwingConstants.LEFT);

		JSeparator separator_1 = new JSeparator();
		separator_1.setRequestFocusEnabled(false);
		separator_1.setMinimumSize(new Dimension(7, 14));
		separator_1.setPreferredSize(new Dimension(7, 14));
		separator_1.setOrientation(SwingConstants.VERTICAL);
		pnlStatusBar.add(separator_1);

		lblPartInformation = new JLabel("---");
		lblPartInformation.setMinimumSize(new Dimension(300, 14));
		lblPartInformation.setPreferredSize(new Dimension(300, 14));
		lblPartInformation.setFont(new Font("Arial", Font.PLAIN, 12));
		pnlStatusBar.add(lblPartInformation);

		JSeparator separator_2 = new JSeparator();
		separator_2.setRequestFocusEnabled(false);
		separator_2.setPreferredSize(new Dimension(7, 14));
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setMinimumSize(new Dimension(7, 14));
		pnlStatusBar.add(separator_2);
		
		// Left tool bar
		JToolBar toolBar = new JToolBar();
		toolBar.setMaximumSize(new Dimension(45, 2));
		toolBar.setMinimumSize(new Dimension(45, 2));
		toolBar.setPreferredSize(new Dimension(45, 2));
		toolBar.setOrientation(SwingConstants.VERTICAL);
		frame.getContentPane().add(toolBar, BorderLayout.WEST);
		
		JToggleButton tglbtnX = new JToggleButton("X");
		tglbtnX.setMaximumSize(new Dimension(45, 23));
		tglbtnX.setMinimumSize(new Dimension(45, 23));
		tglbtnX.setActionCommand("rotateX");
		axisButtonGroup.add(tglbtnX);
		toolBar.add(tglbtnX);
		
		JToggleButton tglbtnY = new JToggleButton("Y");
		tglbtnY.setMaximumSize(new Dimension(45, 23));
		tglbtnY.setMinimumSize(new Dimension(45, 23));
		tglbtnY.setActionCommand("rotateY");
		axisButtonGroup.add(tglbtnY);
		toolBar.add(tglbtnY);
		
		JToggleButton tglbtnYX = new JToggleButton("YX");
		tglbtnYX.setSelected(true);
		tglbtnYX.setMinimumSize(new Dimension(45, 23));
		tglbtnYX.setMaximumSize(new Dimension(45, 23));
		tglbtnYX.setActionCommand("rotateYX");
		axisButtonGroup.add(tglbtnYX);
		toolBar.add(tglbtnYX);

		// Main menu bar
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		// File Menu
		JMenu mnFile = new JMenu("File");
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);

		JMenuItem mntmImport = new JMenuItem("Open . . .");
		mntmImport.setActionCommand("Open");
		mntmImport.setToolTipText("Open an existing Model as Instruction book.");
		mntmImport.setMnemonic('O');
		mntmImport.addActionListener(new ImportEvent());
		mntmImport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		mnFile.add(mntmImport);

		mnRecent = new JMenu("Open Recent");
		mnRecent.setActionCommand("OpenRecent");
		mnRecent.setMnemonic('R');
		mnFile.add(mnRecent);

		MenuAssistant.updateFileMenu(mnRecent, prefsObject.getList(IOAssistant.KEY_RECENTPATHS));

		JMenuItem mntmLocations = new JMenuItem("Preferences . . .");
		mntmLocations.setActionCommand("Preferences");
		mntmLocations.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				PreferencesDialog dialog = new PreferencesDialog();
				dialog.setLocation(getOriginPoint(0));
				dialog.propagate(prefsObject);
				dialog.setVisible(true);
			}
		});
		mntmLocations.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, InputEvent.CTRL_MASK));
		mntmLocations.setToolTipText("Set paths to parts library and external tools.");
		mntmLocations.setMnemonic('P');
		mnFile.add(mntmLocations);

		// View Menu
		JMenu mnView = new JMenu("View");
		mnView.setEnabled(false);
		mnView.setMnemonic('V');
		menuBar.add(mnView);

		JCheckBoxMenuItem chckbxmntmSingleStep = new JCheckBoxMenuItem("Single Step");
		chckbxmntmSingleStep.setToolTipText("Show parts for current step only, or all above.");
		chckbxmntmSingleStep.setMnemonic('S');
		chckbxmntmSingleStep.setActionCommand("SingleStep");
		mnView.add(chckbxmntmSingleStep);

		JMenuItem mntmResetZoom = new JMenuItem("Reset Zoom");
		mntmResetZoom.setToolTipText("Show actual step size.");
		mntmResetZoom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK));
		mntmResetZoom.setMnemonic('R');
		mntmResetZoom.setActionCommand("ResetZoom");
		mnView.add(mntmResetZoom);

		JMenuItem mntmHideSelected = new JMenuItem("Hide Selected");
		mntmHideSelected.setToolTipText("Hide selected parts on workspace.");
		mntmHideSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK));
		mntmHideSelected.setMnemonic('H');
		mntmHideSelected.setActionCommand("HideSelected");
		mnView.add(mntmHideSelected);

		JMenuItem mntmUnhideAll = new JMenuItem("Unhide All");
		mntmUnhideAll.setToolTipText("Show all hidden by user parts.");
		mntmUnhideAll.setMnemonic('U');
		mntmUnhideAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK));
		mntmUnhideAll.setActionCommand("UnhideAll");
		mnView.add(mntmUnhideAll);

		// Export Menu
		JMenu mnExport = new JMenu("Export");
		mnExport.setEnabled(false);
		mnExport.setMnemonic('x');
		menuBar.add(mnExport);

		JMenuItem mntmGenerateFinalImages = new JMenuItem("Generate Final Images");
		mntmGenerateFinalImages.setActionCommand("GenerateFinalImages");
		mntmGenerateFinalImages.setToolTipText("Generate final images of each step in this Instruction book.");
		mntmGenerateFinalImages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				exportImages();
			}
		});
		mntmGenerateFinalImages.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		mntmGenerateFinalImages.setMnemonic('F');
		mnExport.add(mntmGenerateFinalImages);

		JMenuItem mntmExploreCache = new JMenuItem("Explore Cache");
		mntmExploreCache.setActionCommand("ExploreCache");
		mntmExploreCache.setToolTipText("Opens cache directory for this Instruction.");
		mntmExploreCache.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String[] cmdarray = { IOAssistant.getFileManager(), IOAssistant.modelCachePath().toString() };
				try {
					Runtime.getRuntime().exec(cmdarray);
				} catch (IOException ex) {
					lblNotificationArea.setText(ex.getLocalizedMessage());
				}
			}
		});
		mntmExploreCache.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		mntmExploreCache.setMnemonic('l');
		mnExport.add(mntmExploreCache);

		// Help Menu
		JMenu mnHelp = new JMenu("Help");
		mnHelp.setMnemonic('H');
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About LDIVisualizer . . .");
		mntmAbout.setActionCommand("About");
		mntmAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				AboutDialog dialog = new AboutDialog();
				dialog.setLocation(getOriginPoint(0));
				dialog.setVisible(true);
			}
		});
		mntmAbout.setMnemonic('A');
		mnHelp.add(mntmAbout);

		// One place for access to resource from them all
		ActionListener actionImport = new ImportEvent();
		for (int k = 0; k < mnRecent.getItemCount(); k++) {
			mnRecent.getItem(k).addActionListener(actionImport);
		}

		// One place for display them all
		MouseListener actionMouse = new MenuMouseEvent();
		KeyListener actionKey = new MenuKeyEvent();

		ActionListener actionView = new ViewMenuEvent();

		for (int m = 0; m < menuBar.getMenuCount(); m++) {
			JMenu menu = menuBar.getMenu(m);
			menu.addMouseListener(actionMouse);
			menu.addKeyListener(actionKey);
			for (int n = 0; n < menu.getItemCount(); n++) {
				menu.getItem(n).addMouseListener(actionMouse);
				switch (menu.getText()) {
				case "View":
					menu.getItem(n).addActionListener(actionView);
					break;
				}
			}
		}
		
		// Left tool bar action
		ActionListener actionPerspective = new PerspectiveEvent(); 
		Enumeration<AbstractButton> buttons = axisButtonGroup.getElements();
		while(buttons.hasMoreElements()) {
			AbstractButton button = buttons.nextElement();
			button.addActionListener(actionPerspective);
		}

		// Causes this Window to be sized to fit the preferred size and layouts
		// of its subcomponents.
		frame.pack();
	}

	protected void updateWorkspace() {
		if (stepList.getModel().getSize() > 1) {
			renderedModel.stepMatrix.put(LDRenderedModel.selectedStepIndex, glDisplay.getMatrix());
			LDRenderedModel.selectedStepIndex = stepList.getSelectedIndex() + 1;
			
			renderedModel.toggleVisibiltyForAllParts(LDRenderedModel.selectedStepIndex,
					LDRenderedModel.singleStepMode);

			glDisplay.applyMatrix( renderedModel.stepMatrix.get(LDRenderedModel.selectedStepIndex) );
			
			hiddenParts.clear();
			updateInformations();
		}	
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		ldrawFolder = null;

		final SplashScreen splash = new SplashScreen();
		splash.setVisible(true);
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					glDisplay = new LDrawGLDisplay();
				} catch (Exception ex) {
					JOptionPane
							.showMessageDialog(null,
									ex.getClass().getSimpleName() + " : "
											+ "There is a problem with your graphic card:\n" + ex.getLocalizedMessage(),
									"OpenGL Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					System.exit(1);
				} 

				File pf = Paths.get(IOAssistant.appDataPath().toString(), IOAssistant.prefsFileName).toFile();
				prefsObject = new MyPreferences(Preferences.userNodeForPackage(MainWindow.class), pf);

				if (prefsObject.get(IOAssistant.KEY_LDRAWPATH_1).length() > 0) {
					try {
						File pathObj_1 = new File(prefsObject.get(IOAssistant.KEY_LDRAWPATH_1));
						if (pathObj_1.isDirectory())
							ldrawFolder = new LDrawLib(pathObj_1,
									new File(prefsObject.get(IOAssistant.KEY_LDRAWPATH_2)));
						else
							ldrawFolder = null;
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(null, "Unable to read your LDraw library directory.\n"
								+ "If you moved your library, delete preference file '" + IOAssistant.prefsFileName
								+ "' " + "and restart program to specify a new position.\n" + "Original error was:\n"
								+ ex.getLocalizedMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
						ldrawFolder = null;
					}
				}

				if (ldrawFolder == null) {
					JFileChooser jfc = new JFileChooser(".");
					jfc.setDialogTitle("Choose LDraw Library directory");
					jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int dialogResponse = jfc.showOpenDialog(null);
					if (dialogResponse == JFileChooser.APPROVE_OPTION) {
						File official = jfc.getSelectedFile();
						try {
							ldrawFolder = new LDrawLib(official, null);
							prefsObject.put(IOAssistant.KEY_LDRAWPATH_1, official.getAbsolutePath());
							prefsObject.put(IOAssistant.KEY_LDRAWPATH_2, "");
							prefsObject.savePreferences();
						} catch (IOException ex) {
							JOptionPane.showMessageDialog(null, ex.getClass().getSimpleName() + " : "
									+ "Unable to read your LDraw Library directory:\n" + ex.getLocalizedMessage(),
									"I/O Error", JOptionPane.ERROR_MESSAGE);
							ldrawFolder = null;
						}
					} else
						System.exit(1);
				}

				MainWindow window = null;
				try {
					window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception ex) {
					boolean messageExist = ex.getLocalizedMessage() == null || ex.getLocalizedMessage().isEmpty();
					String message = messageExist ? "503 Internal Error -- Service Temporarily Unavailable"
							: ex.toString();
					JOptionPane.showMessageDialog(null, message, "Fatal Error", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					System.exit(1);
				} finally {
					splash.setVisible(false);
					if (window != null) {
						glDisplay.enableSelection(true);
						glDisplay.addPickListener(window);
					}
				}
			}
		});
	}

	/**
	 * @category File
	 */
	private void fileOpen(File resource) {
		File response;
		if (resource != null)
			response = resource;
		else
			response = IOAssistant.getOpenFileName("Import Model", prefsObject.get(IOAssistant.KEY_IMPORTPATH),
					JFileChooser.FILES_AND_DIRECTORIES);

		if (response != null) {
			// Store model path
			prefsObject.put(IOAssistant.KEY_IMPORTPATH, response.getParent());

			// Clean-up duty
			LDrawModel.clearModels();
			glDisplay.resetView();
			glDisplay.resetZoom();
			stepList.removeAll();

			selectedParts.clear();
			hiddenParts.clear();

			updateInformations();

			// Initiate new project task
			BusyDialog busyDialog = new BusyDialog(frame, "Reading project", true, true);
			ImportLDrawProjectTask ldrawProject = new ImportLDrawProjectTask(response);
			busyDialog.setTask(ldrawProject);

			// Start task
			Timer timer = new Timer(200, busyDialog);
			ldrawProject.execute();
			timer.start();
			busyDialog.setVisible(true);

			// After completing task return here
			timer.stop();
			busyDialog.dispose();

			// Waits if necessary for at most the given time for the computation
			// to complete
			try {
				ldrawProject.get(100, TimeUnit.MILLISECONDS);
				if (ldrawProject.isWarnings()) {
					JOptionPane.showMessageDialog(frame, ldrawProject.getInternalLog(), "Import Model",
							JOptionPane.WARNING_MESSAGE);
				}
			} catch (InterruptedException | ExecutionException | TimeoutException ex) {
			}

			// Show result
			currentModel = ldrawProject.getModel();
			renderedModel = LDRenderedModel.newLDRenderedModel(currentModel, glDisplay);
			Thread t = renderedModel.getRenderTask(status);
			t.start();

			// Store model name
			IOAssistant.modelName = currentModel.getName();

			// Enable all JMenu instances in main menu
			for (int k = 0; k < menuBar.getMenuCount(); k++) {
				menuBar.getMenu(k).setEnabled(true);
			}

			// Update Menus
			MenuAssistant.insertFileMenuItem(mnRecent, response, new ImportEvent());

			// Update Title
			frame.setTitle(
					String.format("%s :: %s", appShortName, IOAssistant.getfileNameWithOutExt(response.getName())));

			// Update list view
			stepList.setListData(getStepLabels());
			stepList.setSelectedIndex(stepList.getModel().getSize() - 1);
			LDRenderedModel.selectedStepIndex = stepList.getSelectedIndex() +1;
			
			/**
			 * If source file do not have STEP Command existence. Create The One.
			 * If after parsing is done. Some LDrawPart do not have ancestor /StepNo == -1/   
			 * 		assign them to fist step on list.
			 */
			if (currentModel.stepCount()==0) {
				LDrawPart part = null;
				try {
					LDPrimitive prim = LDPrimitive.newStep("1000");
					part = LDrawPart.newPlacedStep(prim);
				} catch (IOException | LDrawException ex) {
				} finally {
					currentModel.addStep(part);
				}
			}
			LDrawPart step = currentModel.getStep(1);
			
			if (step != null)
			for (LDrawPart part: currentModel.getPartList()) {
				if (part.getStepIndex() < 1) part.setStepIndex(step.getStepIndex());
			}
			
			// Display loading time
			final double seconds = ((double)glDisplay.getDrawTimeMs() / 1000000000);
			lblNotificationArea.setText("Done. Drawing time: "+new DecimalFormat("#.##########").format(seconds)+"s");
		}
	}

	/**
	 * 
	 * @category JList
	 */
	private String[] getStepLabels() {
		String[] steps = new String[currentModel.stepCount() == 0 ? 1 : currentModel.stepCount()];

		for (int k = 0; k < steps.length; k++) {
			String label = String.format("Step %d", k + 1);
			steps[k] = label;
		}
		return steps;
	} 

	/**
	 * @category JFrame
	 */
	private void updateInformations() {
		int count = hiddenParts.size();
		lblListNotice.setText(count == 0 ? "" : String.format("%d  hidden", count));
		lblPartInformation.setText("---");
	}

	/**
	 * @category File
	 */
	protected void exportImages() {
		Path storage = IOAssistant.finalImagePath();
		String message = "Done. Images were exported.";
		String modelName = currentModel.getName();

		for (int k = 0; k < stepList.getModel().getSize(); k++) {
			renderedModel.toggleVisibiltyForAllParts(k+1, LDRenderedModel.singleStepMode);
			glDisplay.applyMatrix(renderedModel.stepMatrix.get(k+1));
			
			BufferedImage imgObj = glDisplay.getScreenShot();

			String baseName = String.format("%s-%d.png", modelName, k);
			File fileObj = storage.resolve(baseName).toFile();
			try {
				ImageOutputStream imgFileName = ImageIO.createImageOutputStream(fileObj);
				ImageIO.write(imgObj, "PNG", imgFileName);
			} catch (IOException ex) {
				message = ex.getLocalizedMessage();
				break;
			}
		}
		lblNotificationArea.setText(message);

		renderedModel.toggleVisibiltyForAllParts(LDRenderedModel.selectedStepIndex, LDRenderedModel.singleStepMode);
		glDisplay.applyMatrix(renderedModel.stepMatrix.get(LDRenderedModel.selectedStepIndex));
	}

	/**
	 * Starting X-Y coordinates for displaying dialogs and sub-windows in main
	 * application window, bellow main menu bar
	 * 
	 * @return an instance of <i>Point</i> representing the top-left corner,
	 *         what components should obtaining
	 * @category JDialog
	 */
	protected Point getOriginPoint(int offsetX) {
		Point p = menuBar.getRootPane().getLocation();
		p.translate(frame.getX() + offsetX, frame.getY() + menuBar.getHeight());
		return p;
	}

	
	/**
	 * @category JFrame
	 */
	class PerspectiveEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			switch(event.getActionCommand()){
			case "rotateX":
				glDisplay.setAxisRotation(LDrawGLDisplay.AXIS_X);
				break;
			case "rotateY":
				glDisplay.setAxisRotation(LDrawGLDisplay.AXIS_Y);
				break;
			default:
				glDisplay.setAxisRotation(LDrawGLDisplay.AXIS_YX);
			};
		}
		
	}
	
	/**
	 * @category File
	 */
	class ImportEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			Object obj = event.getSource();
			File res;
			if (obj instanceof JMenuItem)
				res = (File) ((JMenuItem) obj).getClientProperty("entrusted");
			else
				res = null;
			fileOpen(res);
		}

	}
	
	
	/**
	 * Handle action for menu item of 'View' menu
	 * 
	 * @category menuBar
	 *
	 */
	class ViewMenuEvent implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			JMenuItem mntm = ((JMenuItem) event.getSource());
			switch (mntm.getActionCommand()) {
			case "SingleStep":
				LDRenderedModel.singleStepMode = ((JCheckBoxMenuItem) event.getSource()).isSelected();
				renderedModel.toggleVisibiltyForAllParts(LDRenderedModel.selectedStepIndex, LDRenderedModel.singleStepMode);
			case "ResetZoom":
				glDisplay.resetZoom();
				break;
			case "HideSelected":
				for (int index : selectedParts) {
					LDRenderedPart rp = LDRenderedPart.getByGlobalId(index);
					rp.unSelect();
					rp.hide();
					hiddenParts.add(index);
				}
				updateInformations();
				break;
			case "UnhideAll":
				for (int index : hiddenParts) {
					LDRenderedPart.getByGlobalId(index).show();
				}
				hiddenParts.clear();
				updateInformations();
				break;
			}
			glDisplay.update();
		}

	}

	
	/**
	 * Global listener for key action on top-level JMenu
	 * 
	 * @category menuBar
	 */
	class MenuKeyEvent implements KeyListener {
		@Override
		public void keyTyped(KeyEvent event) {
			JMenu menu = (JMenu) event.getSource();
			menu.updateUI();
		}

		@Override
		public void keyPressed(KeyEvent event) {
			// ignored
		}

		@Override
		public void keyReleased(KeyEvent event) {
			// ignored
		}
	}

	/**
	 * Global listener for mouse action taken on JMenuItem
	 *
	 * @category menuBar
	 */
	class MenuMouseEvent implements MouseListener {
		@Override
		public void mouseEntered(MouseEvent event) {
			JMenuItem mntm = ((JMenuItem) event.getSource());
			String toolTip = mntm.getToolTipText();

			lblNotificationArea.setText(toolTip);
		}

		@Override
		public void mouseClicked(MouseEvent event) {
			lblNotificationArea.setText("");
		}

		@Override
		public void mouseExited(MouseEvent event) {
			lblNotificationArea.setText("");
		}

		@Override
		public void mousePressed(MouseEvent event) {
			// ignored
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			// ignored
		}
	}

	/**
	 * Actions taken on selected instances of LDRenderedPart class
	 * 
	 * @param partId
	 *            global part identificator /0 if no part selected/
	 * @param mode
	 *            mode of picking
	 * 
	 * @category LDRenderedPart
	 */
	@Override
	public void partPicked(int partId, PickMode mode, MouseEvent event) {
		boolean needRefresh = true;

		// Clear selection of any parts
		if (mode == PickMode.NONE) {
			if (selectedParts.size() > 0) {
				for (int id : selectedParts) {
					LDRenderedPart.getByGlobalId(id).unSelect();
				}
			}
			selectedParts.clear();
		}

		if (partId != 0) {
			LDRenderedPart currentSelected = LDRenderedPart.getByGlobalId(partId);
			// DO IT action corresponded to label
			switch (mode) {
			case ADD:
				if (!selectedParts.contains(partId)) {
					selectedParts.add(partId);
					currentSelected.select();
				}
				break;
			case TOGGLE:
				if (!selectedParts.contains(partId)) {
					selectedParts.add(partId);
					currentSelected.select();
				} else {
					selectedParts.remove(partId);
					currentSelected.unSelect();
				}
				break;
			case NONE:
				selectedParts.add(partId);
				currentSelected.select();
				break;
			default:
				break;
			}

			// Show information about... selected part, or number of chosen
			// parts
			String description = "";
			try {
				description = currentSelected.placedPart.getCanonicalDescription();
			} catch (IOException ex) {
				description = ex.getClass().getSimpleName();
			}
			lblPartInformation.setText(selectedParts.size() == 1 ? description
					: String.format("%d object were selected.", selectedParts.size()));
		}

		if (selectedParts.size() == 0)
			lblPartInformation.setText("---");

		// Refresh content, if necessary
		if (needRefresh)
			glDisplay.update();
	}
}
