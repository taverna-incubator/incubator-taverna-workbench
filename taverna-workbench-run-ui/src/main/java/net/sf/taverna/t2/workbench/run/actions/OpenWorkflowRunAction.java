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
package net.sf.taverna.t2.workbench.run.actions;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.apache.log4j.Logger;

import uk.org.taverna.platform.run.api.RunService;

/**
 * An action for opening a workflow run from a file.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class OpenWorkflowRunAction extends AbstractAction {
	private static Logger logger = Logger.getLogger(OpenWorkflowRunAction.class);
	private static final String OPEN_WORKFLOW_RUN = "Open workflow run...";

	private final RunService runService;
	private final File runStore;

	public OpenWorkflowRunAction(RunService runService, File runStore) {
		super(OPEN_WORKFLOW_RUN, WorkbenchIcons.openIcon);
		this.runService = runService;
		this.runStore = runStore;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component)
			parentComponent = (Component) e.getSource();
		else
			parentComponent = null;
		openWorkflowRuns(parentComponent);
	}

	public void openWorkflowRuns(final Component parentComponent) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle(OPEN_WORKFLOW_RUN);

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Workflow Run";
			}

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".wfRun");
			}
		});

		fileChooser.setCurrentDirectory(runStore);
		fileChooser.setMultiSelectionEnabled(true);

		if (fileChooser.showOpenDialog(parentComponent) == APPROVE_OPTION) {
			final File[] selectedFiles = fileChooser.getSelectedFiles();
			if (selectedFiles.length == 0) {
				logger.warn("No files selected");
				return;
			}
			new SwingWorker<Void, Void>() {
				@Override
				public Void doInBackground() {
					for (File file : selectedFiles)
						try {
							runService.open(file);
						} catch (IOException e) {
							showErrorMessage(parentComponent, file, e);
						}
					return null;
				}
			}.execute();
		}
	}

	/**
	 * Show an error message if a file could not be opened
	 * 
	 * @param parentComponent
	 * @param file
	 * @param throwable
	 */
	protected void showErrorMessage(final Component parentComponent,
			final File file, Throwable throwable) {
		Throwable cause = throwable;
		while (cause.getCause() != null)
			cause = cause.getCause();
		final String message = cause.getMessage();
		invokeLater(new Runnable() {
			@Override
			public void run() {
				showMessageDialog(parentComponent,
						"Failed to open workflow from " + file + ":\n"
								+ message, "Warning", WARNING_MESSAGE);
			}
		});
	}
}
