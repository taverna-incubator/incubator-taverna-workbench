/**
 *
 */
package net.sf.taverna.t2.activities.unrecognized.views;

import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.UnrecognizedActivity;

import org.apache.log4j.Logger;

/**
 * A UnrecognizedContextualView displays information about a UnrecognizedActivity
 *
 * @author alanrw
 *
 */
public class UnrecognizedContextualView extends
		HTMLBasedActivityContextualView<Object> {

	private static Logger logger = Logger
			.getLogger(UnrecognizedContextualView.class);

	public UnrecognizedContextualView(UnrecognizedActivity activity, ColourManager colourManager) {
		super(activity, colourManager);
		init();
	}

	private void init() {
	}

	/**
	 * The table for the UnrecognizedActivity shows its ports and the information
	 * within the offline Activity's configuration.
	 *
	 * @return
	 */
	@Override
	protected String getRawTableRowsHtml() {
		String html = "";
		html = html + "<tr><th>Input Port Name</th><th>Port depth</th>"
				+ "</tr>";
		for (ActivityInputPort aip : getActivity().getInputPorts()) {
			html = html + "<tr><td>" + aip.getName() + "</td><td>"
					+ aip.getDepth() + "</td></tr>";
		}
		html = html + "<tr><th>Output Port Name</th><th>Port depth</th>"
				+ "</tr>";
		for (OutputPort aop : getActivity().getOutputPorts()) {
			html = html + "<tr><td>" + aop.getName() + "</td><td>"
					+ aop.getDepth() + "</td></tr>";
		}

		return html;
	}

	@Override
	public String getViewTitle() {
		return "Unrecognized service";
	}

	@Override
	public int getPreferredPosition() {
		return 100;
	}

}
