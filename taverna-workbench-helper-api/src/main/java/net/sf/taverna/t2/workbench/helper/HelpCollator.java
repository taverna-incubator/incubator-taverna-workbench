package net.sf.taverna.t2.workbench.helper;

import java.awt.Component;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.help.BadIDException;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.help.Map.ID;
import javax.help.TryMap;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * This class loads the {@link HelpSet} and also deals with the registration of
 * ids and the decoding from a {@link Component} to the corresponding id. These
 * two sets of functionality should possibly be separated.
 * 
 * @author alanrw
 */
// TODO Convert to a bean
public final class HelpCollator {
	private static Logger logger = Logger.getLogger(HelpCollator.class);
	/**
	 * The HelpSet that is being used.
	 */
	private static HelpSet hs = null;
	/**
	 * The mapping from components to ids. This is used because of problems with
	 * CSH throwing exceptions because it tried to use ids that were not in the
	 * map.
	 */
	private static Map<Component, String> idMap;
	/**
	 * Indicates whether the HelpCollator has been initialized.
	 */
	private static boolean initialized = false;
	/**
	 * A Pattern for normalizing the ids.
	 */
	private static Pattern nonAlphanumeric;
	/**
	 * The emptyHelp is set if the HelpCollator was unable to read the
	 */
	private static boolean emptyHelp = true;
	private static int TIMEOUT = 5000;

	private static String externalHelpSetURL = "http://www.mygrid.org.uk/taverna/helpset/"
			+ version() + "/helpset.hs";

	// private static Profile profile = ProfileFactory.getInstance().getProfile();
	private static String version() {
		return "NO-VERSION";//profile.getVersion();
		// TODO find a better way to find the version
	}

	/**
	 * Attempt to read the up-to-date HelpSet from the web
	 */
	private static void readExternalHelpSet() {
		try {
			URL url = new URL(externalHelpSetURL);
			checkConnection(url);
			hs = new HelpSet(null, url);
			if (hs.getLocalMap() == null) {
			    hs = null;
				logger.error("Helpset from " + externalHelpSetURL
						+ " local map was null");
			} else
				logger.info("Read external help set from " + externalHelpSetURL);
		} catch (MissingResourceException e) {
		    logger.error("No external HelpSet URL specified", e);
		} catch (MalformedURLException e) {
		    logger.error("External HelpSet URL is malformed", e);
		} catch (HelpSetException e) {
		    logger.error("External HelpSet could not be read", e);
		} catch (IOException e) {
			logger.error("IOException reading External HelpSet", e);
		}
	}

	private static void checkConnection(URL url) throws IOException {
		if (!url.getProtocol().startsWith("http"))
			return;
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setReadTimeout(TIMEOUT);
		connection.setConnectTimeout(TIMEOUT);
		connection.setRequestMethod("HEAD");
		connection.getInputStream().close();
		connection.disconnect();
	}

	/**
	 * This methods creates a HelpSet based upon, in priority, the external
	 * HelpSet, then a newly created empty HelpSet.
	 */
	private static void initialize() {
		if (initialized)
			return;
		readExternalHelpSet();
		if (hs == null) {
			hs = new HelpSet();
			hs.setLocalMap(new TryMap());
		} else {
			logger.trace("EmptyHelp set to false");
			emptyHelp = false;
		}
		idMap = new HashMap<>();
		nonAlphanumeric = Pattern.compile("[^a-z0-9\\.]");
		initialized = true;
	}

	/**
	 * Indicates if an empty HelpSet is being used
	 *
	 * @return
	 */
	public static boolean isEmptyHelp() {
		return emptyHelp;
	}

	public static URL getURLFromID(String id) throws BadIDException,
			MalformedURLException {
		initialize();
		logger.trace("Looking for id: " + id);
		ID theId = ID.create(id, hs);
		if (theId == null)
			return null;
		return hs.getCombinedMap().getURLFromID(theId);
	}

	/**
	 * Register a component under the specified id. The method checks that the
	 * id is known to the HelpSet's map.
	 * 
	 * @param component
	 * @param id
	 */
	public static void registerComponent(Component component, String id) {
		logger.trace("Attempting to register " + id);
		initialize();
		String normalizedId = normalizeString(id.toLowerCase());
		if (idMap.containsKey(component)) {
			logger.info("Registered " + normalizedId);
			return;
		}

		/*
		 * If Workbench is started up while there is no network connection -
		 * hs.getLocalMap() is null for some reason
		 */
		if (hs != null && hs.getLocalMap() != null
				&& hs.getLocalMap().isValidID(normalizedId, hs)) {
			idMap.put(component, normalizedId);
			logger.info("Registered " + normalizedId);
		} else
			logger.warn("Refused to register component as " + normalizedId
					+ " not in map");
	}

	/**
	 * Register a component. Since no id is specified, the HelpCollator takes
	 * the canonical name of the component's class. This is useful when an
	 * explicit hierarchy-based approach has been taken.
	 *
	 * @param component
	 */
	public static void registerComponent(Component component) {
		String canonicalName = component.getClass().getCanonicalName();
		if (canonicalName != null)
			registerComponent(component, canonicalName);
	}

	/**
	 * Register a component based upon its parent's class and a suffix
	 * indicating the component's purpose in the parent.
	 *
	 * @param component
	 * @param parent
	 * @param suffix
	 */
	public static void registerComponent(Component component, Object parent,
			String suffix) {
		String canonicalName = parent.getClass().getCanonicalName();
		if (canonicalName != null)
			registerComponent(component, canonicalName + "-" + suffix);
	}

	/**
	 * Try to find an id for the Component. This code should be re-written when
	 * we have more experience in how to couple the UI and HelpSets.
	 *
	 * @param c
	 * @return
	 */
	static String getHelpID(Component c) {
		initialize();
		boolean found = false;
		String result = null;
		if (c instanceof JTree) {
			String idInTree = getHelpIDInTree((JTree) c);
			if (idInTree != null) {
				found = true;
				result = idInTree;
			}
		}
		Component working = c;
		if (c != null)
			logger.trace("Starting at a " + working.getClass());
		while (!found && (working != null)) {
			if (idMap.containsKey(working)) {
				result = idMap.get(working);
				found = true;
				logger.trace("Found component id " + result);
			} else {
				String className = working.getClass().getCanonicalName();
				if (hs.getLocalMap().isValidID(className, hs)) {
					result = className;
					found = true;
					logger.trace("Found class name " + result);
				}
			}
			if (!found) {
				working = working.getParent();
				if (working != null)
					logger.trace("Moved up to a " + working.getClass());
			}
		}
		return result;
	}

	/**
	 * Change the input String into an id that contains only alphanumeric
	 * characters or hyphens.
	 *
	 * @param input
	 * @return
	 */
	private static String normalizeString(String input) {
		Matcher m = nonAlphanumeric.matcher(input);
		return m.replaceAll("-");
	}

	/**
	 * If help is sought on part of a JTree, then this method attempts to find a
	 * node of the tree that can be mapped to an id. The possibilities are ad
	 * hoc and should be re-examined when more experience is gained.
	 * 
	 * @param c
	 * @return
	 */
	private static String getHelpIDInTree(JTree c) {
		initialize();

		TreePath tp = c.getSelectionPath();
		if (tp == null)
			return null;

		Object o = tp.getLastPathComponent();
		if (o == null)
			return null;

		if (o instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) o;
			if (dmtn.getUserObject() != null)
				o = dmtn.getUserObject();
		}

		String className = o.getClass().getCanonicalName();

		logger.trace("Tree node as a string is " + o);

		String possibility = normalizeString(o.toString().toLowerCase());

		logger.trace("Normalized is " + possibility);
		logger.trace("Tree node class name is " + className);

		possibility = className + "-" + possibility;

		logger.trace("Possibility is " + possibility);

		String result;
		if (hs.getLocalMap().isValidID(possibility, hs)) {
			result = possibility;
			logger.trace("Accepted tree node " + result);
		} else if (hs.getLocalMap().isValidID(className, hs)) {
			result = className;
			logger.trace("Found tree node class name " + result);
		} else {
			result = null;
		}

		logger.debug("Tree node is a " + o.getClass());
		return result;
	}
}
