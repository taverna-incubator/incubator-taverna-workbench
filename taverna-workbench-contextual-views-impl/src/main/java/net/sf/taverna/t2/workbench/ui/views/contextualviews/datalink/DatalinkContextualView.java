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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.datalink;

import static java.awt.FlowLayout.LEFT;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uk.org.taverna.scufl2.api.core.DataLink;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

/**
 * Contextual view for dataflow's datalinks.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 */
class DatalinkContextualView extends ContextualView {
	private static final long serialVersionUID = -5031256519235454876L;

	private DataLink datalink;
	private JPanel datalinkView;
	@SuppressWarnings("unused")
	private final FileManager fileManager;

	public DatalinkContextualView(DataLink datalink, FileManager fileManager) {
		this.datalink = datalink;
		this.fileManager = fileManager;
		initView();
	}

	@Override
	public JComponent getMainFrame() {
		refreshView();
		return datalinkView;
	}

	@Override
	public String getViewTitle() {
		return "Data link: " + datalink.getReceivesFrom().getName() + " -> " + datalink.getSendsTo().getName();
	}

	@Override
	public void refreshView() {
		datalinkView = new JPanel(new FlowLayout(LEFT));
		datalinkView.setBorder(new EmptyBorder(5,5,5,5));
		JLabel label = new JLabel (getTextForLabel());
		datalinkView.add(label);
	}

	private String getTextForLabel() {
		//FIXME
		// return getTextFromDepth("link", datalink.getResolvedDepth());
		return "Fix DataLink resolved depth";
	}

	private void updatePrediction() {
		//FIXME
		// fileManager.getCurrentDataflow().checkValidity();
	}

	@Override
	@SuppressWarnings("serial")
	public Action getConfigureAction(Frame owner) {
		return new AbstractAction("Update prediction") {
			@Override
			public void actionPerformed(ActionEvent e) {
				updatePrediction();
				refreshView();
			}
		};
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}
}
