/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
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
package net.sf.taverna.t2.workbench.report.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

public class ValidateObjectSwingWorker extends SwingWorker<String, String>{

	private static Logger logger = Logger.getLogger(ValidateObjectSwingWorker.class);

	private Dataflow df;
	private Processor p;

	private final EditManager editManager;
	private final ReportManager reportManager;

	public ValidateObjectSwingWorker(Dataflow df, Processor p, EditManager editManager, ReportManager reportManager){
	    this.df = df;
	    this.p = p;
		this.editManager = editManager;
		this.reportManager = reportManager;
	}

	@Override
	protected String doInBackground() throws Exception {
	    reportManager.updateObjectReport(df, p);
	    List<Edit<?>> editList = new ArrayList<Edit<?>>();
	    try {
		if (ValidateSwingWorker.checkProcessorDisability(p, reportManager.getReports(df).get(p), editList, editManager, reportManager)) {
			editManager.doDataflowEdit(df, new CompoundEdit(editList));
		    reportManager.updateObjectReport(df, p);
		}
	    }
	    catch (EditException ex) {
		logger.error("Enabled of disabled activity failed", ex);
	    }
	    return "done";
	}

}
