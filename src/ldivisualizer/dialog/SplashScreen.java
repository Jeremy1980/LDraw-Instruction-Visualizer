package ldivisualizer.dialog;

import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.EmptyBorder;

import ldivisualizer.toolbox.IOAssistant;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.SpringLayout;
import javax.swing.ImageIcon;
import javax.swing.border.EtchedBorder;

public class SplashScreen extends JWindow {

	private static final long serialVersionUID = 8403699460693122817L;
	private JPanel contentPane;

	/**
	 * Create the frame.
	 */
	public SplashScreen() {
		// Set the window's bounds, centering the window
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int width = 350;
	    int height = 200;		
	    int x = (screen.width - width) / 2;
	    int y = (screen.height - height) / 2;
	    
		setBounds(x, y, width, height);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(192, 192, 192), new Color(64, 64, 64)));
		contentPane.setBackground(Color.WHITE);
		setContentPane(contentPane);
		
		JLabel lblAppName = new JLabel(IOAssistant.appLongName);
		lblAppName.setBorder(new EmptyBorder(20, 0, 0, 0));
		lblAppName.setFont(new Font("Times New Roman", Font.BOLD, 24));
		lblAppName.setForeground(Color.RED);
		lblAppName.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lblAppVersion = new JLabel("v "+IOAssistant.appVersion);
		lblAppVersion.setBorder(new EmptyBorder(0, 0, 0, 0));
		lblAppVersion.setFont(new Font("Times New Roman", Font.BOLD, 22));
		lblAppVersion.setForeground(Color.RED);
		lblAppVersion.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		contentPane.add(lblAppName);
		contentPane.add(lblAppVersion);
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		SpringLayout sl_panel = new SpringLayout();
		panel.setLayout(sl_panel);
		
		JLabel lblLDrawLogo = new JLabel("LDraw.org");
		sl_panel.putConstraint(SpringLayout.WEST, lblLDrawLogo, 10, SpringLayout.WEST, panel);
		lblLDrawLogo.setIcon(new ImageIcon(SplashScreen.class.getResource("/images/logotyp/64x64/ldraw-logotyp-64.png")));
		panel.add(lblLDrawLogo);
		
		JLabel lblJavaLogo = new JLabel(" ");
		sl_panel.putConstraint(SpringLayout.NORTH, lblLDrawLogo, 0, SpringLayout.NORTH, lblJavaLogo);
		sl_panel.putConstraint(SpringLayout.NORTH, lblJavaLogo, 0, SpringLayout.NORTH, panel);
		sl_panel.putConstraint(SpringLayout.EAST, lblJavaLogo, 0, SpringLayout.EAST, panel);
		lblJavaLogo.setIcon(new ImageIcon(SplashScreen.class.getResource("/images/logotyp/64x64/java-logotyp-64.png")));
		panel.add(lblJavaLogo);
	}
}
