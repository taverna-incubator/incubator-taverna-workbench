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
package net.sf.taverna.t2.activities.dataflow.views;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.dataflow.actions.EditNestedDataflowAction;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.actions.ReplaceNestedWorkflowAction;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.activity.Activity;

@SuppressWarnings("serial")
public class DataflowActivityContextualView extends HTMLBasedActivityContextualView {

	static Logger logger = Logger.getLogger(DataflowActivityContextualView.class);

	private final EditManager editManager;
	private final FileManager fileManager;
	private final MenuManager menuManager;
	private final ActivityIconManager activityIconManager;
	private final ColourManager colourManager;
	private final WorkbenchConfiguration workbenchConfiguration;
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	private final SelectionManager selectionManager;

	public DataflowActivityContextualView(Activity activity, EditManager editManager,
			FileManager fileManager, MenuManager menuManager,
			ActivityIconManager activityIconManager, ColourManager colourManager,
			ServiceDescriptionRegistry serviceDescriptionRegistry,
			WorkbenchConfiguration workbenchConfiguration, SelectionManager selectionManager) {
		super(activity, colourManager);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.menuManager = menuManager;
		this.activityIconManager = activityIconManager;
		this.colourManager = colourManager;
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		this.workbenchConfiguration = workbenchConfiguration;
		this.selectionManager = selectionManager;
		addEditButtons();
	}

	@Override
	public Activity getActivity() {
		return super.getActivity();
	}

	public void addEditButtons() {
		JComponent mainFrame = getMainFrame();
		JButton viewWorkflowButton = new JButton("Edit workflow");
		viewWorkflowButton.addActionListener(new EditNestedDataflowAction(getActivity(),
				selectionManager));
		JButton configureButton = new JButton(new ReplaceNestedWorkflowAction(getActivity(),
				editManager, fileManager, menuManager, activityIconManager, colourManager,
				serviceDescriptionRegistry, workbenchConfiguration, selectionManager));
		configureButton.setIcon(null);
		JPanel flowPanel = new JPanel(new FlowLayout());
		flowPanel.add(viewWorkflowButton);
		flowPanel.add(configureButton);
		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		mainFrame.revalidate();
	}

//	@Override
//	public JComponent getMainFrame() {
//		JComponent mainFrame = super.getMainFrame();
//		JButton viewWorkflowButton = new JButton("Edit workflow");
//		viewWorkflowButton.addActionListener(new EditNestedDataflowAction(getActivity(),
//				selectionManager));
//		JButton configureButton = new JButton(new ReplaceNestedWorkflowAction(getActivity(),
//				editManager, fileManager, menuManager, activityIconManager, colourManager,
//				serviceDescriptionRegistry, workbenchConfiguration, selectionManager));
//		configureButton.setIcon(null);
//		JPanel flowPanel = new JPanel(new FlowLayout());
//		flowPanel.add(viewWorkflowButton);
//		flowPanel.add(configureButton);
//		mainFrame.add(flowPanel, BorderLayout.SOUTH);
//		return mainFrame;
//	}

	@Override
	protected String getRawTableRowsHtml() {
		return ("<tr><td colspan=2>" + getActivity().getName() + "</td></tr>");
	}

	@Override
	public String getViewTitle() {
		return "Nested workflow";
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return null;
		// return new OpenNestedDataflowFromFileAction(
		// (DataflowActivity) getActivity(), owner);
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
