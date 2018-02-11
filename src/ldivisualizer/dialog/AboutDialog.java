package ldivisualizer.dialog;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JSeparator;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.UIManager;

import ldivisualizer.toolbox.IOAssistant;

import javax.swing.ImageIcon;

public class AboutDialog extends BaseDialog {

	private static final long serialVersionUID = 7728146952149371257L;


	public AboutDialog() {
		lblWarningArea.setVisible(false);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		JLabel lblDialogTitle = new JLabel(IOAssistant.appLongName);
		lblDialogTitle.setIcon(new ImageIcon(AboutDialog.class.getResource("/icons/about/icon-about-24.png")));
		lblDialogTitle.setFont(new Font("Arial", Font.BOLD, 16));
		contentPanel.add(lblDialogTitle);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setPreferredSize(new Dimension(1, 2));
		contentPanel.add(separator_1);
		
		JTextPane txtpnLegalInformation = new JTextPane();
		txtpnLegalInformation.setForeground(UIManager.getColor("Label.foreground"));
		txtpnLegalInformation.setBackground(UIManager.getColor("Label.background"));
		txtpnLegalInformation.setText("Developed by:\r\n\tCopyright 2013-2014 Mario Pascucci <mpascucci@gmail.com>\r\n\tCopyright 2017-2018 Jeremy Czajkowski <jeremy.cz@wp.pl>\r\n\r\nProudly powered by:\r\n       JLDraw \tas starting platform\r\n       JOGL \tas render engine\r\n\r\n\r\n\t\tUses the LDraw Parts Library\r\n");
		txtpnLegalInformation.setEditable(false);
		contentPanel.add(txtpnLegalInformation);
		
		JSeparator separator_2 = new JSeparator();
		contentPanel.add(separator_2);
		
		JTextPane txtpnLegoCopyright = new JTextPane();
		txtpnLegoCopyright.setForeground(UIManager.getColor("List.foreground"));
		txtpnLegoCopyright.setBackground(UIManager.getColor("Label.background"));
		txtpnLegoCopyright.setEditable(false);
		txtpnLegoCopyright.setText("LEGO(R) and the LEGO logo are registered trademarks of the LEGO Group,\r\nwhich does not sponsor, endorse, or authorize this program.");
		contentPanel.add(txtpnLegoCopyright);
		pack();
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		dispose();
	}


}
