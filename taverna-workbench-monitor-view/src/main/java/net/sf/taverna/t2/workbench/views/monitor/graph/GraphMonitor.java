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
package net.sf.taverna.t2.workbench.views.monitor.graph;

import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.closeIcon;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.tickIcon;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.workingIcon;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;

import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.ui.Updatable;
import uk.org.taverna.platform.report.ActivityReport;
import uk.org.taverna.platform.report.ProcessorReport;
import uk.org.taverna.platform.report.State;
import uk.org.taverna.platform.report.WorkflowReport;

/**
 * An implementation of the Updatable interface that updates a Graph.
 * 
 * @author David Withers
 */
public class GraphMonitor implements Updatable {
	private static final String STATUS_RUNNING = "Running";
	private static final String STATUS_FINISHED = "Finished";
	private static final String STATUS_CANCELLED = "Cancelled";

	/**
	 * Workflow run status label - we can only tell of workflow is running or is
	 * finished from inside this monitor. If workfow run is stopped or paused -
	 * this will be updated form the run-ui.
	 */
	private JLabel workflowRunStatusLabel;
	/**
	 * Similarly to {@link #workflowRunStatusLabel} - we disable the pause anc
	 * cancel buttons when workflow runs is finished
	 */
	private JButton workflowRunPauseButton;
	private JButton workflowRunCancelButton;
	private GraphController graphController;
	private Set<GraphMonitorNode> processors = new HashSet<>();
	private final WorkflowReport workflowReport;

	public GraphMonitor(GraphController graphController,
			WorkflowReport workflowReport) {
		this.graphController = graphController;
		this.workflowReport = workflowReport;
		createMonitorNodes(workflowReport.getSubject().getName(),
				workflowReport);
		redraw();
	}

	private void createMonitorNodes(String id, WorkflowReport workflowReport) {
		for (ProcessorReport processorReport : workflowReport
				.getProcessorReports()) {
			String processorId = id + processorReport.getSubject().getName();
			processors.add(new GraphMonitorNode(processorId, processorReport,
					graphController));
			for (ActivityReport activityReport : processorReport
					.getActivityReports()) {
				WorkflowReport nestedWorkflowReport = activityReport
						.getNestedWorkflowReport();
				if (nestedWorkflowReport != null)
					createMonitorNodes(processorId, nestedWorkflowReport);
			}
		}
	}

	public void redraw() {
		for (GraphMonitorNode node : processors)
			node.redraw();
	}

	@Override
	public void update() {
		for (GraphMonitorNode node : processors)
			node.update();
		// updateState();
	}

	@SuppressWarnings("unused")
	private void updateState() {
		State state = workflowReport.getState();
		switch (state) {
		case COMPLETED:
		case FAILED:
			workflowRunStatusLabel.setText(STATUS_FINISHED);
			workflowRunStatusLabel.setIcon(tickIcon);
			workflowRunPauseButton.setEnabled(false);
			workflowRunCancelButton.setEnabled(false);
			break;
		case CANCELLED:
			workflowRunStatusLabel.setText(STATUS_CANCELLED);
			workflowRunStatusLabel.setIcon(closeIcon);
			workflowRunPauseButton.setEnabled(false);
			workflowRunCancelButton.setEnabled(false);
			break;
		case RUNNING:
			workflowRunStatusLabel.setText(STATUS_RUNNING);
			workflowRunStatusLabel.setIcon(workingIcon);
		default:
			break;
		}
	}

	// Set the status label that will be updated from this monitor
	public void setWorkflowRunStatusLabel(JLabel workflowRunStatusLabel) {
		this.workflowRunStatusLabel = workflowRunStatusLabel;
	}

	public void setWorkflowRunPauseButton(JButton workflowRunPauseButton) {
		this.workflowRunPauseButton = workflowRunPauseButton;
	}

	public void setWorkflowRunCancelButton(JButton workflowRunCancelButton) {
		this.workflowRunCancelButton = workflowRunCancelButton;
	}
}
