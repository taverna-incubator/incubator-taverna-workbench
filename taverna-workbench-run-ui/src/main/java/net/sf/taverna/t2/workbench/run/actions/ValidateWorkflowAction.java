/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.run.actions;

import static java.awt.event.KeyEvent.VK_V;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.searchIcon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.Workbench;

@SuppressWarnings("serial")
public class ValidateWorkflowAction extends AbstractAction {
	private static final String VALIDATE_WORKFLOW = "Validate workflow";

	protected Action subAction;

	public ValidateWorkflowAction(EditManager editManager,
			FileManager fileManager, ReportManager reportManager,
			Workbench workbench) {
		super(VALIDATE_WORKFLOW, searchIcon);
		putValue(MNEMONIC_KEY, VK_V);
		// subAction = new ReportOnWorkflowAction("", true, false, editManager,
		// fileManager, reportManager, workbench);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if (subAction != null)
			subAction.actionPerformed(ev);
	}
}
