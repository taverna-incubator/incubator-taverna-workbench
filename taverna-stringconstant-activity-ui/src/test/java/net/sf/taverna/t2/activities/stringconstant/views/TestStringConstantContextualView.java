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
package net.sf.taverna.t2.activities.stringconstant.views;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.org.taverna.scufl2.api.activity.Activity;

public class TestStringConstantContextualView {
	Activity activity;

	@Before
	public void setup() {
		activity = new Activity();
	}

	@Test
	@Ignore
	public void testGetConfigureAction() throws Exception {
		ContextualView view = new StringConstantActivityContextualView(
				activity, null, null, null, null, null, null);
		assertNotNull("The action should not be null",
				view.getConfigureAction(null));
		assertTrue(
				"Should be a StringConstantActivityConfigurationAction",
				view.getConfigureAction(null) instanceof StringConstantActivityConfigurationAction);
	}
}
