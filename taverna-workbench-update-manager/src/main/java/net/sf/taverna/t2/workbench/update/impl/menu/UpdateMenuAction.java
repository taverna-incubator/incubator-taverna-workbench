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
package net.sf.taverna.t2.workbench.update.impl.menu;

import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;

import org.apache.log4j.Logger;

import uk.org.taverna.commons.update.UpdateException;
import uk.org.taverna.commons.update.UpdateManager;

public class UpdateMenuAction extends AbstractMenuAction {
	private static final Logger logger = Logger.getLogger(UpdateMenuAction.class);
	private static final URI ADVANCED_MENU_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#advanced");

	private UpdateManager updateManager;

	public UpdateMenuAction() {
		super(ADVANCED_MENU_URI, 1000);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Check for updates") {
			@Override
			public void actionPerformed(ActionEvent e) {
				findUpdates();
			}
		};
	}

	public void setUpdateManager(UpdateManager updateManager) {
		this.updateManager = updateManager;
	}

	private void findUpdates() {
		Component parent = null;
		try {
			if (!areUpdatesAvailable()) {
				showMessageDialog(null, "No update available");
				return;
			}
			if (showConfirmDialog(parent, "Update available. Update Now?") != YES_OPTION)
				return;
			applyUpdates();
			showMessageDialog(parent,
					"Update complete. Restart Taverna to apply update.");
		} catch (UpdateException ex) {
			showMessageDialog(parent, "Update failed: " + ex.getMessage());
			logger.warn("Update failed", ex);
		}
	}

	protected boolean areUpdatesAvailable() throws UpdateException {
		return updateManager.checkForUpdates();
	}

	protected void applyUpdates() throws UpdateException {
		updateManager.update();
	}
}
