package ldivisualizer.toolbox;

import javax.swing.Action;
import it.romabrick.ldrawlib.LDrawColor;

import java.awt.Button;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class LDrawColorPanel extends Panel implements MouseListener {

	private static final long serialVersionUID = 7728146952149371257L;
	private boolean contentIsReady = false;
	private Action outputAction = null;

	
	public LDrawColorPanel() {
		setLayout(new GridLayout(0, 3, 0, 0));
		if (!contentIsReady)
			for(LDrawColor color: LDrawColor.getColorList()) {
				String text = color.getName();
				if (text.toLowerCase().startsWith("trans")) continue;
				if (text.toLowerCase().contains("color")) continue;
				Button colorHolder = new Button(" ");
				
				colorHolder.setActionCommand(String.valueOf(color.getId()));
				colorHolder.setBackground(color.getColor());
				colorHolder.addMouseListener(this);
				add(colorHolder);
			}
			contentIsReady = true;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if (outputAction != null) {
			int id = Integer.valueOf(((Button)event.getSource()).getActionCommand());
			outputAction.putValue("color", id);
			outputAction.actionPerformed(getEvent(event));
		}		
	}

	@Override
	public void mouseEntered(MouseEvent event) {
		if (outputAction != null) {
			int id = Integer.valueOf(((Button)event.getSource()).getActionCommand());
			outputAction.putValue("color", id);
			outputAction.actionPerformed(getEvent(event));
		}
	}

	@Override
	public void mouseExited(MouseEvent event) {
		if (outputAction != null) {
			int id = Integer.valueOf(((Button)event.getSource()).getActionCommand());
			outputAction.putValue("color", id);
			outputAction.actionPerformed(getEvent(event));
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		// ignored
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		// ignored
	}
	
	public void setActionOutput(Action output) {
		outputAction  = output;
	}

	private ActionEvent getEvent(MouseEvent me) {
		return new ActionEvent(me.getSource(), me.getID(), me.paramString());
	}

}
