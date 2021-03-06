package net.sf.taverna.t2.workbench.updatemanager;

import static java.awt.event.KeyEvent.VK_F12;
import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

public class PluginMenuAction extends AbstractMenuAction {
	private static final String UPDATES_AND_PLUGINS = "Updates and plugins";

	@SuppressWarnings("serial")
	protected class SoftwareUpdates extends AbstractAction {
		public SoftwareUpdates() {
			super(UPDATES_AND_PLUGINS, null/*UpdatesAvailableIcon.updateRecommendedIcon*/);
			putValue(ACCELERATOR_KEY, getKeyStroke(VK_F12, 0));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			@SuppressWarnings("unused")
			Component parent = null;
			if (e.getSource() instanceof Component) {
				parent = (Component) e.getSource();
			}
			//FIXME this does nothing!
			//final PluginManagerFrame pluginManagerUI = new PluginManagerFrame(
			//		PluginManager.getInstance());
			//if (parent != null) {
			//	pluginManagerUI.setLocationRelativeTo(parent);
			//}
			//pluginManagerUI.setVisible(true);
		}
	}

	public PluginMenuAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#advanced"),
				100);
	}

	@Override
	protected Action createAction() {
		//return new SoftwareUpdates();
		return null;
	}
}
