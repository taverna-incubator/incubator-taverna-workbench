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
package net.sf.taverna.t2.activities.stringconstant.servicedescriptions;

import java.net.URI;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.AbstractTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import uk.org.taverna.scufl2.api.configurations.Configuration;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class StringConstantTemplateService extends AbstractTemplateService {
	private static final URI ACTIVITY_TYPE = URI
			.create("http://ns.taverna.org.uk/2010/activity/constant");
	private static final URI providerId = URI
			.create("http://taverna.sf.net/2010/service-provider/stringconstant");
	public static final String DEFAULT_VALUE = "Add your own value here";
	private static final String STRINGCONSTANT = "Text constant";

	@Override
	public URI getActivityType() {
		return ACTIVITY_TYPE;
	}

	@Override
	public Configuration getActivityConfiguration() {
		Configuration configuration = new Configuration();
		configuration.setType(ACTIVITY_TYPE.resolve("#Config"));
		((ObjectNode) configuration.getJson()).put("string", DEFAULT_VALUE);
		return configuration;
	}

	@Override
	public Icon getIcon() {
		return StringConstantActivityIcon.getStringConstantIcon();
	}

	@Override
	public String getName() {
		return STRINGCONSTANT;
	}

	@Override
	public String getDescription() {
		return "A string value that you can set";
	}

	public static ServiceDescription getServiceDescription() {
		StringConstantTemplateService scts = new StringConstantTemplateService();
		return scts.templateService;
	}

	@Override
	public String getId() {
		return providerId.toString();
	}

	@Override
	public ServiceDescriptionProvider newInstance() {
		return new StringConstantTemplateService();
	}
}
