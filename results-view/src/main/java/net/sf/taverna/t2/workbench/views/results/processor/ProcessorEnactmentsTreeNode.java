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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;

/**
 * Node in a processor enactments tree. Contains a particular enactment of the
 * processor.
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorEnactmentsTreeNode extends DefaultMutableTreeNode {
	
	@SuppressWarnings("unused")
	private ProcessorEnactment processorEnactment;
	private List<Integer> iteration;
	@SuppressWarnings("unused")
	private List<Integer> parentIteration;
	private List<Integer> fullIteration;

	
	public ProcessorEnactmentsTreeNode(ProcessorEnactment processorEnactment, List<Integer> parentIteration){
		super(processorEnactment);
		this.processorEnactment = processorEnactment;
		this.parentIteration = parentIteration;
		this.iteration = ProcessorEnactmentsTreeModel.iterationToIntegerList(processorEnactment.getIteration());
		this.fullIteration = new ArrayList<Integer>();
		if (parentIteration != null) {
			fullIteration.addAll(parentIteration);
		}
		fullIteration.addAll(iteration);
	}
	
	public String toString(){
		boolean isNested = getChildCount() > 0;
		StringBuilder sb = new StringBuilder();		
		if (! fullIteration.isEmpty()) {			
			// Iteration 3.1.3
			if (isNested || iteration.isEmpty()) {
				sb.append("Nested iteration ");
			} else {
				sb.append("Iteration ");
			}
			for (Integer index : fullIteration) {				
				sb.append(index+1);
				sb.append(".");
			}
			// Remove last .
			sb.delete(sb.length()-1, sb.length());
		} else {
			sb.append("Invocation");
		}		
		return sb.toString();
	}

	public List<Integer> getFullIteration() {
		return fullIteration;		
	}

}