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
package net.sf.taverna.t2.workbench.selection.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.OpenedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.SetCurrentDataflowEvent;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.selection.events.PerspectiveSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.ProfileSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowRunSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowSelectionEvent;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflow.edits.AddChildEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 * Implementation of the {@link SelectionManager}.
 *
 * @author David Withers
 */
public class SelectionManagerImpl implements SelectionManager {
	private static final String RESULTS_PERSPECTIVE_ID = "net.sf.taverna.t2.ui.perspectives.results.ResultsPerspective";

	private static final String DESIGN_PERSPECTIVE_ID = "net.sf.taverna.t2.ui.perspectives.design.DesignPerspective";

	private static final Logger logger = Logger.getLogger(SelectionManagerImpl.class);

	private WorkflowBundle selectedWorkflowBundle;
	private Map<WorkflowBundle, DataflowSelectionModel> workflowSelectionModels = new IdentityHashMap<>();
	private Map<WorkflowBundle, Workflow> selectedWorkflows = new IdentityHashMap<>();
	private Map<WorkflowBundle, Profile> selectedProfiles = new IdentityHashMap<>();
	private String selectedWorkflowRun;
	private Map<String, DataflowSelectionModel> workflowRunSelectionModels = new HashMap<>();
	private PerspectiveSPI selectedPerspective;
	private MultiCaster<SelectionManagerEvent> observers = new MultiCaster<>(this);
	private FileManager fileManager;
	private List<PerspectiveSPI> perspectives;

	@Override
	public DataflowSelectionModel getDataflowSelectionModel(WorkflowBundle dataflow) {
		DataflowSelectionModel selectionModel;
		synchronized (workflowSelectionModels) {
			selectionModel = workflowSelectionModels.get(dataflow);
			if (selectionModel == null) {
				selectionModel = new DataflowSelectionModelImpl();
				workflowSelectionModels.put(dataflow, selectionModel);
			}
		}
		return selectionModel;
	}

	@Override
	public WorkflowBundle getSelectedWorkflowBundle() {
		return selectedWorkflowBundle;
	}

	@Override
	public void setSelectedWorkflowBundle(WorkflowBundle workflowBundle) {
		setSelectedWorkflowBundle(workflowBundle, true);
	}

	private void setSelectedWorkflowBundle(WorkflowBundle workflowBundle, boolean notifyFileManager) {
		if (workflowBundle == null || workflowBundle == selectedWorkflowBundle)
			return;
		if (notifyFileManager) {
			fileManager.setCurrentDataflow(workflowBundle);
			return;
		}
		if (selectedWorkflows.get(workflowBundle) == null)
			selectedWorkflows.put(workflowBundle,
					workflowBundle.getMainWorkflow());
		if (selectedProfiles.get(workflowBundle) == null)
			selectedProfiles.put(workflowBundle,
					workflowBundle.getMainProfile());
		SelectionManagerEvent selectionManagerEvent = new WorkflowBundleSelectionEvent(
				selectedWorkflowBundle, workflowBundle);
		selectedWorkflowBundle = workflowBundle;
		notify(selectionManagerEvent);
		selectDesignPerspective();
	}

	private void removeWorkflowBundle(WorkflowBundle dataflow) {
		synchronized (workflowSelectionModels) {
			DataflowSelectionModel selectionModel = workflowSelectionModels.remove(dataflow);
			if (selectionModel != null)
				for (Observer<DataflowSelectionMessage> observer : selectionModel.getObservers())
					selectionModel.removeObserver(observer);
		}
		synchronized (selectedWorkflows) {
			selectedWorkflows.remove(dataflow);
		}
		synchronized (selectedProfiles) {
			selectedProfiles.remove(dataflow);
		}
	}

	@Override
	public Workflow getSelectedWorkflow() {
		return selectedWorkflows.get(selectedWorkflowBundle);
	}

	@Override
	public void setSelectedWorkflow(Workflow workflow) {
		if (workflow != null) {
			Workflow selectedWorkflow = selectedWorkflows.get(workflow
					.getParent());
			if (selectedWorkflow != workflow) {
				SelectionManagerEvent selectionManagerEvent = new WorkflowSelectionEvent(
						selectedWorkflow, workflow);
				selectedWorkflows.put(workflow.getParent(), workflow);
				notify(selectionManagerEvent);
			}
		}
	}

	@Override
	public Profile getSelectedProfile() {
		return selectedProfiles.get(selectedWorkflowBundle);
	}

	@Override
	public void setSelectedProfile(Profile profile) {
		if (profile != null) {
			Profile selectedProfile = selectedProfiles.get(profile.getParent());
			if (selectedProfile != profile) {
				SelectionManagerEvent selectionManagerEvent = new ProfileSelectionEvent(
						selectedProfile, profile);
				selectedProfiles.put(profile.getParent(), profile);
				notify(selectionManagerEvent);
			}
		}
	}

	@Override
	public String getSelectedWorkflowRun() {
		return selectedWorkflowRun;
	}

	@Override
	public void setSelectedWorkflowRun(String workflowRun) {
		if ((selectedWorkflowRun == null && workflowRun != null)
				|| !selectedWorkflowRun.equals(workflowRun)) {
			SelectionManagerEvent selectionManagerEvent = new WorkflowRunSelectionEvent(
					selectedWorkflowRun, workflowRun);
			selectedWorkflowRun = workflowRun;
			notify(selectionManagerEvent);
			selectResultsPerspective();
		}
	}

	@Override
	public DataflowSelectionModel getWorkflowRunSelectionModel(String workflowRun) {
		DataflowSelectionModel selectionModel;
		synchronized (workflowRunSelectionModels) {
			selectionModel = workflowRunSelectionModels.get(workflowRun);
			if (selectionModel == null) {
				selectionModel = new DataflowSelectionModelImpl();
				workflowRunSelectionModels.put(workflowRun, selectionModel);
			}
		}
		return selectionModel;
	}

	@SuppressWarnings("unused")
	private void removeWorkflowRun(String workflowRun) {
		synchronized (workflowRunSelectionModels) {
			DataflowSelectionModel selectionModel = workflowRunSelectionModels
					.remove(workflowRun);
			if (selectionModel != null)
				for (Observer<DataflowSelectionMessage> observer : selectionModel
						.getObservers())
					selectionModel.removeObserver(observer);
		}
	}

	@Override
	public PerspectiveSPI getSelectedPerspective() {
		return selectedPerspective;
	}

	@Override
	public void setSelectedPerspective(PerspectiveSPI perspective) {
		if (selectedPerspective != perspective) {
			SelectionManagerEvent selectionManagerEvent = new PerspectiveSelectionEvent(
					selectedPerspective, perspective);
			selectedPerspective = perspective;
			notify(selectionManagerEvent);
		}
	}

	private void selectDesignPerspective() {
		for (PerspectiveSPI perspective : perspectives)
			if (DESIGN_PERSPECTIVE_ID.equals(perspective.getID())) {
				setSelectedPerspective(perspective);
				break;
			}
	}

	private void selectResultsPerspective() {
		for (PerspectiveSPI perspective : perspectives)
			if (RESULTS_PERSPECTIVE_ID.equals(perspective.getID())) {
				setSelectedPerspective(perspective);
				break;
			}
	}

	@Override
	public void addObserver(Observer<SelectionManagerEvent> observer) {
		synchronized (observers) {
			WorkflowBundle selectedWorkflowBundle = getSelectedWorkflowBundle();
			Workflow selectedWorkflow = getSelectedWorkflow();
			Profile selectedProfile = getSelectedProfile();
			String selectedWorkflowRun = getSelectedWorkflowRun();
			PerspectiveSPI selectedPerspective = getSelectedPerspective();
			try {
				if (selectedWorkflowBundle != null)
					observer.notify(this, new WorkflowBundleSelectionEvent(
							null, selectedWorkflowBundle));
				if (selectedWorkflow != null)
					observer.notify(this, new WorkflowSelectionEvent(null,
							selectedWorkflow));
				if (selectedProfile != null)
					observer.notify(this, new ProfileSelectionEvent(null,
							selectedProfile));
				if (selectedWorkflowRun != null)
					observer.notify(this, new WorkflowRunSelectionEvent(null,
							selectedWorkflowRun));
				if (selectedPerspective != null)
					observer.notify(this, new PerspectiveSelectionEvent(null,
							selectedPerspective));
			} catch (Exception e) {
				logger.warn("Could not notify " + observer, e);
			}
			observers.addObserver(observer);
		}
	}

	@Override
	public void removeObserver(Observer<SelectionManagerEvent> observer) {
		observers.removeObserver(observer);
	}

	@Override
	public List<Observer<SelectionManagerEvent>> getObservers() {
		return observers.getObservers();
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
		setSelectedWorkflowBundle(fileManager.getCurrentDataflow());
		fileManager.addObserver(new FileManagerObserver());
	}

	public void setEditManager(EditManager editManager) {
		editManager.addObserver(new EditManagerObserver());
	}

	public void setPerspectives(List<PerspectiveSPI> perspectives) {
		this.perspectives = perspectives;
	}

	public class FileManagerObserver implements Observer<FileManagerEvent> {
		@Override
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof ClosedDataflowEvent) {
				WorkflowBundle workflowBundle = ((ClosedDataflowEvent) message).getDataflow();
				removeWorkflowBundle(workflowBundle);
			} else if (message instanceof OpenedDataflowEvent) {
				WorkflowBundle workflowBundle = ((OpenedDataflowEvent) message).getDataflow();
				setSelectedWorkflowBundle(workflowBundle, false);
			} else if (message instanceof SetCurrentDataflowEvent) {
				WorkflowBundle workflowBundle = ((SetCurrentDataflowEvent) message).getDataflow();
				setSelectedWorkflowBundle(workflowBundle, false);
			}
		}
	}

	private class EditManagerObserver implements Observer<EditManagerEvent> {
		@Override
		public void notify(Observable<EditManagerEvent> sender, EditManagerEvent message)
				throws Exception {
			Edit<?> edit = message.getEdit();
			considerEdit(edit, message instanceof DataFlowUndoEvent);
		}

		private void considerEdit(Edit<?> edit, boolean undoing) {
			if (edit instanceof CompoundEdit) {
				CompoundEdit compound = (CompoundEdit) edit;
				for (Edit<?> e : compound.getChildEdits())
					considerEdit(e, undoing);
			} else if (edit instanceof AddChildEdit
					&& edit.getSubject() instanceof Workflow) {
				Workflow subject = (Workflow) edit.getSubject();
				DataflowSelectionModel selectionModel = getDataflowSelectionModel(subject
						.getParent());
				Object child = ((AddChildEdit<?>) edit).getChild();
				if (child instanceof Processor
						|| child instanceof InputWorkflowPort
						|| child instanceof OutputWorkflowPort) {
					if (undoing
							&& selectionModel.getSelection().contains(child))
						selectionModel.clearSelection();
					else {
						Set<Object> selection = new HashSet<>();
						selection.add(child);
						selectionModel.setSelection(selection);
					}
				}
			}
		}
	}

	private void notify(SelectionManagerEvent event) {
		synchronized (observers) {
			observers.notify(event);
		}
	}
}
