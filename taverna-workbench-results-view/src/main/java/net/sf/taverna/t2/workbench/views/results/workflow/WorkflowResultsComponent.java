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
package net.sf.taverna.t2.workbench.views.results.workflow;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static net.sf.taverna.t2.workbench.MainWindow.getMainWindow;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.closeIcon;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.saveAllIcon;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.results.InvocationView;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;

import uk.org.taverna.databundle.DataBundles;
import uk.org.taverna.platform.report.WorkflowReport;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.port.WorkflowPort;

/**
 * This component contains a tabbed pane, where each tab displays results for one of the output
 * ports of a workflow, and a set of 'save results' buttons that save results from all ports in a
 * certain format.
 *
 * @author David Withers
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class WorkflowResultsComponent extends JPanel implements UIComponentSPI, Updatable {
	private static Logger logger = Logger.getLogger(WorkflowResultsComponent.class);

	private InvocationView portValuesComponent;
	private JPanel saveButtonsPanel;
	/**
	 * List of all existing 'save results' actions, each one can save results in
	 * a different format
	 */
	private List<SaveAllResultsSPI> saveActions;
	private final RendererRegistry rendererRegistry;
	private final List<SaveIndividualResultSPI> saveIndividualActions;
	private final WorkflowReport workflowReport;
	private final Workflow workflow;
	private Path inputs, outputs;

	public WorkflowResultsComponent(WorkflowReport workflowReport,
			RendererRegistry rendererRegistry, List<SaveAllResultsSPI> saveActions,
			List<SaveIndividualResultSPI> saveIndividualActions) {
		super(new BorderLayout());
		this.workflowReport = workflowReport;
		this.rendererRegistry = rendererRegistry;
		this.saveActions = saveActions;
		this.saveIndividualActions = saveIndividualActions;

		workflow = workflowReport.getSubject();
		init();
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Results View Component";
	}

	@Override
	public void onDisplay() {
	}

	@Override
	public void onDispose() {
	}

	private void init() {
		saveButtonsPanel = new JPanel(new BorderLayout());
		populateSaveButtonsPanel();
		add(saveButtonsPanel, NORTH);

		portValuesComponent = new InvocationView(workflowReport
				.getInvocations().first(), rendererRegistry,
				saveIndividualActions);
		add(portValuesComponent, CENTER);
	}

	private void populateSaveButtonsPanel() {
		JButton saveButton = new JButton(new SaveAllAction("Save all values", this));
		saveButtonsPanel.add(saveButton, EAST);
	}

	public void selectWorkflowPortTab(WorkflowPort port) {
		portValuesComponent.selectPortTab(port);
	}

	@Override
	public void update() {
		portValuesComponent.update();
	}

	private class SaveAllAction extends AbstractAction {

		public SaveAllAction(String name, WorkflowResultsComponent resultViewComponent) {
			super(name);
			putValue(SMALL_ICON, saveAllIcon);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			saveAll();
		}
	}

	private static final String TITLE = "Workflow run data saver";
	private static final String EXPLANATION = "Select the workflow input and output ports to save the associated data";

	private void saveAll() {
		final JDialog dialog = new HelpEnabledDialog(getMainWindow(), TITLE,
				true);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(getMainWindow());
		JPanel panel = new JPanel(new BorderLayout());
		DialogTextArea explanation = new DialogTextArea();
		explanation.setText(EXPLANATION);
		explanation.setColumns(40);
		explanation.setEditable(false);
		explanation.setOpaque(false);
		explanation.setBorder(new EmptyBorder(5, 20, 5, 20));
		explanation.setFocusable(false);
		explanation.setFont(new JLabel().getFont()); // make the font the same as for other components in the dialog
		panel.add(explanation, NORTH);
		final Map<String, JCheckBox> inputChecks = new HashMap<>();
		final Map<String, JCheckBox> outputChecks = new HashMap<>();
		final Map<JCheckBox, Path> checkReferences = new HashMap<>();
		final Map<String, Path> chosenReferences = new HashMap<>();
		final Set<Action> actionSet = new HashSet<>();

		ItemListener listener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox source = (JCheckBox) e.getItemSelectable();
				if (inputChecks.containsValue(source) && source.isSelected()
						&& outputChecks.containsKey(source.getText()))
					outputChecks.get(source.getText()).setSelected(false);
				if (outputChecks.containsValue(source) && source.isSelected()
						&& inputChecks.containsKey(source.getText()))
					inputChecks.get(source.getText()).setSelected(false);
				chosenReferences.clear();
				for (JCheckBox checkBox : checkReferences.keySet())
					if (checkBox.isSelected())
						chosenReferences.put(checkBox.getText(),
								checkReferences.get(checkBox));
			}
		};
		NavigableMap<String, Path> inputPorts = new TreeMap<>();
		try {
			inputPorts = DataBundles.getPorts(inputs);
		} catch (IOException e1) {
			logger.info("No input ports for worklow " + workflow.getName(), e1);
		}
		NavigableMap<String, Path> outputPorts = new TreeMap<>();
		try {
			outputPorts = DataBundles.getPorts(outputs);
		} catch (IOException e1) {
			logger.info("No output ports for worklow " + workflow.getName(), e1);
		}
		JPanel portsPanel = new JPanel();
		portsPanel.setLayout(new GridBagLayout());
		if (!workflow.getInputPorts().isEmpty())
			createInputPortsPanel(inputChecks, checkReferences, listener,
					inputPorts, outputPorts, portsPanel);
		if (!workflow.getOutputPorts().isEmpty())
			createOutputPortsPanel(outputChecks, checkReferences, listener,
					outputPorts, portsPanel);
		panel.add(portsPanel, CENTER);
		chosenReferences.clear();
		for (JCheckBox checkBox : checkReferences.keySet())
			if (checkBox.isSelected())
				chosenReferences.put(checkBox.getText(),
						checkReferences.get(checkBox));

		JPanel buttonsBar = new JPanel();
		buttonsBar.setLayout(new FlowLayout());
		// Get all existing 'Save result' actions
		for (SaveAllResultsSPI spi : saveActions) {
			AbstractAction action = spi.getAction();
			actionSet.add(action);
			JButton saveButton = new JButton((AbstractAction) action);
			if (action instanceof SaveAllResultsSPI) {
				((SaveAllResultsSPI) action)
						.setChosenReferences(chosenReferences);
				((SaveAllResultsSPI) action).setParent(dialog);
			}
			buttonsBar.add(saveButton);
		}
		JButton cancelButton = new JButton("Cancel", closeIcon);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		buttonsBar.add(cancelButton);
		panel.add(buttonsBar, SOUTH);
		panel.revalidate();
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
	}

	private void createInputPortsPanel(Map<String, JCheckBox> inputChecks,
			Map<JCheckBox, Path> checkReferences, ItemListener listener,
			NavigableMap<String, Path> inputPorts,
			NavigableMap<String, Path> outputPorts, JPanel portsPanel) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 10, 5, 10);
		portsPanel.add(new JLabel("Workflow inputs:"), gbc);

		TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<>();
		for (InputWorkflowPort port : workflowReport.getSubject()
				.getInputPorts()) {
			String portName = port.getName();
			Path value = inputPorts.get(portName);
			if (value != null) {
				JCheckBox checkBox = new JCheckBox(portName);
				checkBox.setSelected(!outputPorts.containsKey(portName));
				checkBox.addItemListener(listener);
				inputChecks.put(portName, checkBox);
				sortedBoxes.put(portName, checkBox);
				checkReferences.put(checkBox, value);
			}
		}
		gbc.insets = new Insets(0, 10, 0, 10);
		for (String portName : sortedBoxes.keySet()) {
			gbc.gridy++;
			portsPanel.add(sortedBoxes.get(portName), gbc);
		}
		gbc.gridy++;
		gbc.fill = BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(5, 10, 5, 10);
		portsPanel.add(new JLabel(""), gbc); // empty space
	}

	private void createOutputPortsPanel(Map<String, JCheckBox> outputChecks,
			Map<JCheckBox, Path> checkReferences, ItemListener listener,
			NavigableMap<String, Path> outputPorts, JPanel portsPanel) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = NONE;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(5, 10, 5, 10);
		portsPanel.add(new JLabel("Workflow outputs:"), gbc);
		TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<>();
		for (OutputWorkflowPort port : workflowReport.getSubject()
				.getOutputPorts()) {
			String portName = port.getName();
			Path value = outputPorts.get(portName);
			if (value != null) {
				JCheckBox checkBox = new JCheckBox(portName);
				checkBox.setSelected(true);

				checkReferences.put(checkBox, value);
				checkBox.addItemListener(listener);
				outputChecks.put(portName, checkBox);
				sortedBoxes.put(portName, checkBox);
			}
		}
		gbc.insets = new Insets(0, 10, 0, 10);
		for (String portName : sortedBoxes.keySet()) {
			gbc.gridy++;
			portsPanel.add(sortedBoxes.get(portName), gbc);
		}
		gbc.gridy++;
		gbc.fill = BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(5, 10, 5, 10);
		portsPanel.add(new JLabel(""), gbc); // empty space
	}
}
