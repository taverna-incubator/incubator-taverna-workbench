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
package net.sf.taverna.t2.workbench.design.actions;

import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.deleteIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.RemoveDataLinkEdit;
import net.sf.taverna.t2.workflow.edits.RemoveWorkflowOutputPortEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;

/**
 * Action for removing an output port from the dataflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class RemoveDataflowOutputPortAction extends DataflowEditAction {
	private static final Logger logger = Logger
			.getLogger(RemoveDataflowOutputPortAction.class);

	private OutputWorkflowPort port;

	public RemoveDataflowOutputPortAction(Workflow dataflow,
			OutputWorkflowPort port, Component component,
			EditManager editManager, SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		this.port = port;
		putValue(SMALL_ICON, deleteIcon);
		putValue(NAME, "Delete workflow output port");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			dataflowSelectionModel.removeSelection(port);
			List<DataLink> datalinks = scufl2Tools.datalinksTo(port);
			if (datalinks.isEmpty())
				editManager.doDataflowEdit(dataflow.getParent(),
						new RemoveWorkflowOutputPortEdit(dataflow, port));
			else {
				List<Edit<?>> editList = new ArrayList<>();
				for (DataLink datalink : datalinks)
					editList.add(new RemoveDataLinkEdit(dataflow, datalink));
				editList.add(new RemoveWorkflowOutputPortEdit(dataflow, port));
				editManager.doDataflowEdit(dataflow.getParent(),
						new CompoundEdit(editList));
			}
		} catch (EditException ex) {
			logger.debug("Delete workflow output port failed", ex);
		}
	}
}
