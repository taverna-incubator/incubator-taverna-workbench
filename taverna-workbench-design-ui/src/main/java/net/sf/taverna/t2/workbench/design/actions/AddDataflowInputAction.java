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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.design.ui.DataflowInputPortPanel;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.AddWorkflowInputPortEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;

/**
 * Action for adding an input port to the dataflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class AddDataflowInputAction extends DataflowEditAction {
	private static final Logger logger = Logger
			.getLogger(AddDataflowInputAction.class);

	public AddDataflowInputAction(Workflow dataflow, Component component,
			EditManager editManager, SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		putValue(SMALL_ICON, WorkbenchIcons.inputIcon);
		putValue(NAME, "Workflow input port");
		putValue(SHORT_DESCRIPTION, "Add workflow input port");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			Set<String> usedInputPorts = new HashSet<>();
			for (InputWorkflowPort inputPort : dataflow.getInputPorts())
				usedInputPorts.add(inputPort.getName());

			DataflowInputPortPanel inputPanel = new DataflowInputPortPanel();

			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Add Workflow Input Port", inputPanel);
			vuid.addTextComponentValidation(inputPanel.getPortNameField(),
					"Set the workflow input port name.", usedInputPorts,
					"Duplicate workflow input port name.",
					"[\\p{L}\\p{Digit}_.]+",
					"Invalid workflow input port name.");
			vuid.addMessageComponent(inputPanel.getSingleValueButton(),
					"Set the input port type.");
			vuid.addMessageComponent(inputPanel.getListValueButton(),
					"Set the input port list depth.");
			vuid.setSize(new Dimension(400, 250));

			inputPanel.setPortDepth(0);

			if (vuid.show(component)) {
				InputWorkflowPort dataflowInputPort = new InputWorkflowPort();
				dataflowInputPort.setName(inputPanel.getPortName());
				dataflowInputPort.setDepth(inputPanel.getPortDepth());
				editManager.doDataflowEdit(dataflow.getParent(),
						new AddWorkflowInputPortEdit(dataflow,
								dataflowInputPort));
			}
		} catch (EditException e) {
			logger.warn("Adding a new workflow input port failed");
		}
	}
}
