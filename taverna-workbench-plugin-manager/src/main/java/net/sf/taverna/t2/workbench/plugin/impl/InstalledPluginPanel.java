/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.plugin.impl;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import uk.org.taverna.commons.plugin.Plugin;
import uk.org.taverna.commons.plugin.PluginException;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public class InstalledPluginPanel extends PluginPanel {
	private final Plugin plugin;

	public InstalledPluginPanel(Plugin plugin) {
		super(plugin.getName(), plugin.getOrganization(), plugin.getVersion()
				.toString(), plugin.getDescription());
		this.plugin = plugin;
	}

	@Override
	public Action getPluginAction() {
		return new PluginAction();
	}

	class PluginAction extends AbstractAction {
		public PluginAction() {
			putValue(NAME, "Uninstall");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setEnabled(false);
			putValue(NAME, "Uninstalling");
			try {
				plugin.uninstall();
			} catch (PluginException ex) {
				ex.printStackTrace();
			}
			putValue(NAME, "Uninstalled");
		}
	}
}
