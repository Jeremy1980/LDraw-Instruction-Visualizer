package ldivisualizer.dialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Paths;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import jldraw.MyPreferences;
import ldivisualizer.toolbox.IOAssistant;

import java.awt.Font;
import javax.swing.border.EmptyBorder;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Component;
import javax.swing.JSeparator;

public class PreferencesDialog extends BaseDialog {

	private static final long serialVersionUID = 7728146952149371257L;
	private JTextField txtMainPath;
	private JTextField txtAdditionalPath;
	private JButton btnMainPath;
	private JButton btnAdditionalPath;
	private JLabel lblNotice;
	private JPanel panel;
	private JPanel panel_1;
	private JLabel lblDialogTitle;
	private JLabel spacer;
	private JLabel spacer_1;
	private JSeparator separator;
	private MyPreferences prefsObject = null;


	public PreferencesDialog() {
		contentPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

		lblDialogTitle = new JLabel("Preferences");
		lblDialogTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblDialogTitle.setHorizontalAlignment(SwingConstants.LEFT);
		lblDialogTitle.setFont(new Font("Arial", Font.BOLD, 16));
		contentPanel.add(lblDialogTitle);

		separator = new JSeparator();
		contentPanel.add(separator);

		panel = new JPanel();
		panel.setRequestFocusEnabled(false);
		panel.setVerifyInputWhenFocusTarget(false);
		contentPanel.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		JLabel lblMainPath = new JLabel("Main LDraw storage");
		lblMainPath.setMinimumSize(new Dimension(162, 14));
		lblMainPath.setMaximumSize(new Dimension(162, 14));
		lblMainPath.setPreferredSize(new Dimension(162, 14));
		lblMainPath.setFocusable(false);
		lblMainPath.setFocusTraversalPolicyProvider(true);
		panel.add(lblMainPath);
		lblMainPath.setBorder(new EmptyBorder(0, 10, 0, 10));

		txtMainPath = new JTextField();
		lblMainPath.setLabelFor(txtMainPath);
		txtMainPath.setColumns(2);
		panel.add(txtMainPath);
		txtMainPath.setPreferredSize(new Dimension(100, 20));
		txtMainPath.setMinimumSize(new Dimension(100, 20));

		btnMainPath = new JButton("");
		panel.add(btnMainPath);
		btnMainPath.setIcon(new ImageIcon(
				PreferencesDialog.class.getResource("/com/sun/java/swing/plaf/motif/icons/TreeOpen.gif")));
		btnMainPath.setActionCommand("CHOOSE_DIRECTORY_M");

		spacer = new JLabel(" ");
		spacer.setEnabled(false);
		panel.add(spacer);
		spacer.setBorder(new EmptyBorder(0, 0, 0, 5));
		btnMainPath.addActionListener(this);

		panel_1 = new JPanel();
		panel_1.setVerifyInputWhenFocusTarget(false);
		panel_1.setRequestFocusEnabled(false);
		contentPanel.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.LINE_AXIS));

		JLabel lblAdditionalPath = new JLabel("Additional LDraw storage");
		lblAdditionalPath.setFocusable(false);
		panel_1.add(lblAdditionalPath);
		lblAdditionalPath.setBorder(new EmptyBorder(0, 10, 0, 10));

		txtAdditionalPath = new JTextField();
		lblAdditionalPath.setLabelFor(txtAdditionalPath);
		panel_1.add(txtAdditionalPath);
		txtAdditionalPath.setMinimumSize(new Dimension(100, 20));
		txtAdditionalPath.setPreferredSize(new Dimension(100, 20));

		btnAdditionalPath = new JButton("");
		panel_1.add(btnAdditionalPath);
		btnAdditionalPath.setIcon(new ImageIcon(
				PreferencesDialog.class.getResource("/com/sun/java/swing/plaf/motif/icons/TreeOpen.gif")));
		btnAdditionalPath.setActionCommand("CHOOSE_DIRECTORY_A");

		spacer_1 = new JLabel(" ");
		spacer_1.setEnabled(false);
		spacer_1.setBorder(new EmptyBorder(0, 0, 0, 5));
		panel_1.add(spacer_1);
		btnAdditionalPath.addActionListener(this);

		lblNotice = new JLabel(
				"Keep in mind, that any changes you make here, require that you restart the application.");
		lblNotice.setBorder(new EmptyBorder(10, 10, 10, 10));
		lblNotice.setAlignmentX(Component.CENTER_ALIGNMENT);
		contentPanel.add(lblNotice);
		lblNotice.setIcon(new ImageIcon(PreferencesDialog.class.getResource("/icons/about/icon-about-32.png")));
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		boolean maindir = false;
		switch (event.getActionCommand()) {
		case "CANCEL":
			dispose();
		case "OK":
			if (prefsObject != null) {
				prefsObject.put(IOAssistant.KEY_LDRAWPATH_1, txtMainPath.getText());
				prefsObject.put(IOAssistant.KEY_LDRAWPATH_2, txtAdditionalPath.getText());
			}
			dispose();
			break;
		case "CHOOSE_DIRECTORY_M":
			maindir = true;
		case "CHOOSE_DIRECTORY_A":
			File directory = IOAssistant.getOpenFileName("Choose LDraw Library directory", ".",
					JFileChooser.DIRECTORIES_ONLY);
			if (directory != null) {
				if (maindir)
					txtMainPath.setText(directory.getAbsolutePath());
				else
					txtAdditionalPath.setText(directory.getAbsolutePath());
				
				boolean sub_p = Paths.get(directory.toString(),"P").toFile().exists();
				boolean sub_parts = Paths.get(directory.toString(),"Parts").toFile().exists();
				boolean result = !sub_p && !sub_parts;
				lblWarningArea.setVisible(result);
				if (result)	lblWarningArea.setText("P or Parts directory not found.");
			}
			break;
		}
	}

	public void propagate(MyPreferences prefsObject) {
		this.prefsObject = prefsObject;
		if (prefsObject != null && prefsObject.isStored()) {
			txtMainPath.setText(prefsObject.get(IOAssistant.KEY_LDRAWPATH_1));
			txtAdditionalPath.setText(prefsObject.get(IOAssistant.KEY_LDRAWPATH_2));
			lblWarningArea.setVisible(false);
		} else {
			lblWarningArea.setText("Preferences instance is not accessible.");
			lblWarningArea.setVisible(true);
			lblNotice.setVisible(false);
		}
	}
}
