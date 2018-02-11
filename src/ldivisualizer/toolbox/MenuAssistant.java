package ldivisualizer.toolbox;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class MenuAssistant implements Serializable {

	private static final char[] keyCode = { KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
			KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9 };

	private static final long serialVersionUID = 39503414202748884L;

	public static final int POPUPMENU_ITEM_MAX = 30;

	private MenuAssistant() {
	}

	// Wrapped in a inner static class so that loaded only when required
	private static class AssistantLoader {

		// And no more fear of threads
		private static final MenuAssistant INSTANCE = new MenuAssistant();
	}

	public static MenuAssistant sharedInstance() {
		return AssistantLoader.INSTANCE;
	}

	@SuppressWarnings("unused")
	private MenuAssistant readResolve() {
		return AssistantLoader.INSTANCE;
	}

	public static String[] propertyList(JMenu menu) {
		String[] result = new String[menu.getItemCount()];
		for (int k = 0; k < menu.getItemCount(); k++) {
			result[k] = menu.getItem(k).getClientProperty("entrusted").toString();
		}
		return result;
	}

	public static JMenuItem insertFileMenuItem(JMenu menu, File resource, ActionListener action) {
		for (int m = 0; m < menu.getItemCount(); m++) {
			Object res = menu.getItem(m).getClientProperty("entrusted");
			if (res instanceof File) {
				if (((File) res).getAbsolutePath().equals(resource.getAbsolutePath()))
					return menu.getItem(m);
			}
		}

		JMenuItem item = new JMenuItem(resource.getName());
		item.putClientProperty("entrusted", resource);

		if (menu.getItemCount() >= keyCode.length)
			menu.remove(menu.getItemCount()-1);
		menu.insert(item, 0);
		
		for (int n = 0; n < menu.getItemCount(); n++) {
			menu.getItem(n).setAccelerator(KeyStroke.getKeyStroke(keyCode[n]));
			try {
				item.setToolTipText(resource.getCanonicalPath());
			} catch (IOException ex) {
				item.setToolTipText(resource.getName());
			}
		}
		return item;
	}

	public static void updateFileMenu(JMenu menu, String[] list) {
		menu.removeAll();
		String added = "";
		for (String s : list) {
			if (menu.getItemCount() >= keyCode.length) break;
			File resource = new File(s);
			if (resource.exists() && !added.contains(s)) {
				JMenuItem item = new JMenuItem(resource.getName());
				item.putClientProperty("entrusted", resource);
				item.setAccelerator(KeyStroke.getKeyStroke(keyCode[menu.getItemCount()]));
				try {
					item.setToolTipText(resource.getCanonicalPath());
				} catch (IOException ex) {
					item.setToolTipText(resource.getName());
				}
				menu.add(item);
				added += s;
			}
		}
	}
}
