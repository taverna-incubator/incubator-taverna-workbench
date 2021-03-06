/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.servicepanel;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;
import static net.sf.taverna.t2.servicedescriptions.ServiceDescription.LOCAL_SERVICES;
import static net.sf.taverna.t2.servicedescriptions.ServiceDescription.SERVICE_TEMPLATES;

import java.awt.BorderLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.events.AbstractProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.AbstractProviderNotification;
import net.sf.taverna.t2.servicedescriptions.events.PartialServiceDescriptionsNotification;
import net.sf.taverna.t2.servicedescriptions.events.ProviderErrorNotification;
import net.sf.taverna.t2.servicedescriptions.events.RemovedProviderEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionProvidedEvent;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeModel;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.FilterTreeNode;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

import org.apache.log4j.Logger;

import uk.org.taverna.commons.services.ServiceRegistry;

/**
 * A panel of available services
 *
 * @author Stian Soiland-Reyes
 */
@SuppressWarnings("serial")
public class ServicePanel extends JPanel implements UIComponentSPI {
	private static Logger logger = Logger.getLogger(ServicePanel.class);
	private static final int INITIAL_BLANK_OUT_COUNTER = 2;
	public static final String AVAILABLE_SERVICES = "Available services";
	public static final String MATCHING_SERVIES = "Matching services";
	public static final String NO_MATCHING_SERVICES = "No matching services";
	public static final String MOBY_OBJECTS = "MOBY Objects";
	/**
	 * A Comparable constant to be used with buildPathMap
	 */
	private static final String SERVICES = "4DA84170-7746-4817-8C2E-E29AF8B2984D";
	private static final int STATUS_LINE_MESSAGE_MS = 600;
	private static ServicePathElementComparator servicePathElementComparator = new ServicePathElementComparator();

	public int blankOutCounter = 0;
	private TreeUpdaterThread updaterThread;
	private RootFilterTreeNode root = new RootFilterTreeNode(AVAILABLE_SERVICES);
	private ServiceTreePanel serviceTreePanel;
	private JLabel statusLine;
	private FilterTreeModel treeModel;
	protected Timer statusUpdateTimer;

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;
	protected final ServiceDescriptionRegistryObserver serviceDescriptionRegistryObserver = new ServiceDescriptionRegistryObserver();
	protected final Object updateLock = new Object();
	private final EditManager editManager;
	private final MenuManager menuManager;
	private final SelectionManager selectionManager;
	private final ServiceRegistry serviceRegistry;

	public ServicePanel(ServiceDescriptionRegistry serviceDescriptionRegistry,
			EditManager editManager, MenuManager menuManager,
			SelectionManager selectionManager, ServiceRegistry serviceRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
		this.editManager = editManager;
		this.menuManager = menuManager;
		this.selectionManager = selectionManager;
		this.serviceRegistry = serviceRegistry;
		serviceDescriptionRegistry.addObserver(serviceDescriptionRegistryObserver);
		initialise();
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Service panel";
	}

	@Override
	public void onDisplay() {
	}

	@Override
	public void onDispose() {
	}

	public void providerStatus(ServiceDescriptionProvider provider, String message) {
		logger.info(message + " " + provider);
		final String htmlMessage = "<small>" + message + " [" + provider + "]</small>";

		invokeLater(new Runnable() {
			@Override
			public void run() {
				blankOutCounter = INITIAL_BLANK_OUT_COUNTER;
				statusLine.setText("<html>" + htmlMessage + "</html>");
				statusLine.setVisible(true);
			}
		});
	}

	protected void initialise() {
		removeAll();
		setLayout(new BorderLayout());
		treeModel = new FilterTreeModel(root);
		serviceTreePanel = new ServiceTreePanel(treeModel, serviceDescriptionRegistry, editManager, menuManager, selectionManager, serviceRegistry);
		serviceTreePanel.setAvailableObjectsString(AVAILABLE_SERVICES);
		serviceTreePanel.setMatchingObjectsString(MATCHING_SERVIES);
		serviceTreePanel.setNoMatchingObjectsString(NO_MATCHING_SERVICES);
		add(serviceTreePanel);
		statusLine = new JLabel();
		add(statusLine, BorderLayout.SOUTH);
		if (statusUpdateTimer != null)
			statusUpdateTimer.cancel();
		statusUpdateTimer = new Timer("Clear status line", true);
		statusUpdateTimer
				.scheduleAtFixedRate(new UpdateStatusLineTask(), 0, STATUS_LINE_MESSAGE_MS);
		updateTree();
	}

	protected void updateTree() {
		synchronized (updateLock) {
			if (updaterThread != null && updaterThread.isAlive()) {
				return;
			}
			updaterThread = new TreeUpdaterThread();
			updaterThread.start();
		}
	}

	protected static class ServicePathElementComparator implements Comparator<Object> {
		@Override
		public int compare(Object o1, Object o2) {
			if ((o1 instanceof String) && (o2 instanceof String)) {
				if (o1.equals(SERVICE_TEMPLATES))
					return -1;
				else if (o2.equals(SERVICE_TEMPLATES))
					return 1;
				if (o1.equals(LOCAL_SERVICES))
					return -1;
				else if (o2.equals(LOCAL_SERVICES))
					return 1;
				if (o1.equals(MOBY_OBJECTS))
					return -1;
				else if (o2.equals(MOBY_OBJECTS))
					return 1;
			}
			return o1.toString().compareToIgnoreCase(o2.toString());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	//FIXME this class is type-disastrous! Really bad.
	public class TreeUpdaterThread extends Thread {
		private boolean aborting = false;

		private TreeUpdaterThread() {
			super("Updating service panel");
			setDaemon(true);
		}

		public void abort() {
			aborting = true;
			interrupt();
		}

		@Override
		public void run() {
			Map<Comparable, Map> pathMap = buildPathMap();
			populateChildren(root, pathMap);
			invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						serviceTreePanel.runFilter();
					} catch (InterruptedException | InvocationTargetException e) {
						logger.error("failed to filter", e);
					}
				}
			});
		}

		protected Map<Comparable, Map> buildPathMap() {
			Map<Comparable, Map> paths = new TreeMap<>();
			for (ServiceDescription serviceDescription : serviceDescriptionRegistry
					.getServiceDescriptions()) {
				if (aborting)
					return paths;
				Map currentPath = paths;
				for (Object pathElem : serviceDescription.getPath()) {
					Map pathEntry = (Map) currentPath.get(pathElem);
					if (pathEntry == null) {
						pathEntry = new TreeMap();
						currentPath.put(pathElem, pathEntry);
					}
					currentPath = pathEntry;
				}
				TreeMap<String, Set<ServiceDescription>> services = (TreeMap) currentPath
						.get(SERVICES);
				if (services == null) {
					services = new TreeMap<>();
					currentPath.put(SERVICES, services);
				}
				String serviceDescriptionName = serviceDescription.getName();
				if (!services.containsKey(serviceDescriptionName)) {
					Set<ServiceDescription> serviceSet = new HashSet<>();
					services.put(serviceDescriptionName, serviceSet);
				}
				services.get(serviceDescriptionName).add(serviceDescription);
			}
			return paths;
		}

		protected void populateChildren(FilterTreeNode node, Map pathMap) {
			if (aborting)
				return;
			if (node == root) {
				// Clear top root
				invokeLater(new Runnable() {
					@Override
					public void run() {
						if (aborting)
							return;
						serviceTreePanel.setFilter(null);
						root.removeAllChildren();
					}
				});
			}

			Set<Comparable> paths = new TreeSet<>(servicePathElementComparator);
			Map<String, Set<ServiceDescription>> services = (Map) pathMap
					.get(SERVICES);
			if (services == null)
				services = new TreeMap<>();
			paths.addAll(pathMap.keySet());
			paths.addAll(services.keySet());

			for (Comparable pathElement : paths) {
				if (aborting)
					return;
				if (pathElement.equals(SERVICES))
					continue;
				Set<FilterTreeNode> childNodes = new HashSet<>();
				if (services.containsKey(pathElement)) {
					for (ServiceDescription sd : services.get(pathElement))
						childNodes.add(new ServiceFilterTreeNode(sd));
				} else
					childNodes.add(new PathElementFilterTreeNode((String) pathElement));
				invokeLater(new AddNodeRunnable(node, childNodes));
				if ((pathMap.containsKey(pathElement)) && !childNodes.isEmpty())
					populateChildren(childNodes.iterator().next(), (Map) pathMap.get(pathElement));
			}
			// if (!services.isEmpty()) {
			// Collections.sort(services, serviceComparator);
			// for (String serviceName : services.keySet()) {
			// if (aborting) {
			// return;
			// }
			// if (pathMap.containsKey(serviceName)) {
			// continue;
			// }
			// SwingUtilities.invokeLater(new AddNodeRunnable(node,
			// new ServiceFilterTreeNode(services.get(serviceName))));
			// }
			// }
		}

		public class AddNodeRunnable implements Runnable {
			private final Set<FilterTreeNode> nodes;
			private final FilterTreeNode root;

			public AddNodeRunnable(FilterTreeNode root, Set<FilterTreeNode> nodes) {
				this.root = root;
				this.nodes = nodes;
			}

			@Override
			public void run() {
				if (aborting)
					return;
				for (FilterTreeNode n : nodes)
					root.add(n);
			}
		}
	}

	public static class RemoveNodeRunnable implements Runnable {
		private final FilterTreeNode root;

		public RemoveNodeRunnable(FilterTreeNode root) {
			this.root = root;
		}

		@Override
		public void run() {
			root.removeFromParent();
		}
	}

	private final class ServiceDescriptionRegistryObserver implements
			Observer<ServiceDescriptionRegistryEvent> {
		Set<ServiceDescriptionProvider> alreadyComplainedAbout = new HashSet<>();

		@Override
		public void notify(Observable<ServiceDescriptionRegistryEvent> sender,
				ServiceDescriptionRegistryEvent message) throws Exception {
			if (message instanceof ProviderErrorNotification)
				reportServiceProviderError((ProviderErrorNotification) message);
			else if (message instanceof ServiceDescriptionProvidedEvent
					|| message instanceof RemovedProviderEvent) {
				AbstractProviderEvent ape = (AbstractProviderEvent) message;
				alreadyComplainedAbout.remove(ape.getProvider());
			}

			if (message instanceof AbstractProviderNotification) {
				AbstractProviderNotification abstractProviderNotification = (AbstractProviderNotification) message;
				providerStatus(abstractProviderNotification.getProvider(),
						abstractProviderNotification.getMessage());
			}
			if (message instanceof PartialServiceDescriptionsNotification)
				/*
				 * TODO: Support other events and only update relevant parts of
				 * tree, or at least select the recently added provider
				 */
				updateTree();
			else if (message instanceof RemovedProviderEvent)
				updateTree();
		}

		private void reportServiceProviderError(
				final ProviderErrorNotification pen) {
			ServiceDescriptionProvider provider = pen.getProvider();
			if (serviceDescriptionRegistry
					.getDefaultServiceDescriptionProviders().contains(provider))
				return;
			if (alreadyComplainedAbout.contains(provider))
				return;

			alreadyComplainedAbout.add(provider);
			invokeLater(new Runnable() {
				@Override
				public void run() {
					showMessageDialog(ServicePanel.this, pen.getMessage()
							+ "\n" + pen.getProvider(), "Import service error",
							ERROR_MESSAGE);
				}
			});
		}
	}

	private final class UpdateStatusLineTask extends TimerTask {
		@Override
		public void run() {
			if (blankOutCounter < 0 || blankOutCounter-- > 0)
				// Only clear it once
				return;
			invokeLater(new Runnable() {
				@Override
				public void run() {
					if (blankOutCounter < 0)
						statusLine.setVisible(false);
				}
			});
		}
	}
}
