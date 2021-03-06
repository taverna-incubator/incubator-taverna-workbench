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
package net.sf.taverna.t2.reference.ui.tree;

import static java.awt.datatransfer.DataFlavor.javaFileListFlavor;
import static java.awt.dnd.DnDConstants.ACTION_COPY;
import static java.awt.dnd.DnDConstants.ACTION_MOVE;
import static java.awt.dnd.DragSource.DefaultMoveDrop;
import static net.sf.taverna.t2.reference.ui.tree.PreRegistrationTreeDnDHandler.InternalNodeDragTransferable.INTERNAL_NODE_FLAVOR;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetContext;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

/**
 * An abstract handler class for drags within a {@link JTree} backed by a
 * {@link DefaultTreeModel}. Implement the (@link
 * {@link #handleNodeMove(MutableTreeNode, MutableTreeNode)} method to determine
 * the behaviour when a node is dragged to another node in the tree. Additional
 * methods are called when drags corresponding to URL, List&lt;File&gt; and
 * String are made. Node drags have precedence, only one of the abstract methods
 * is called for any given drop event.
 * 
 * @author Tom Oinn
 */
public abstract class PreRegistrationTreeDnDHandler implements
		DropTargetListener, DragGestureListener, DragSourceListener {
	private static Logger logger = Logger
			.getLogger(PreRegistrationTreeDnDHandler.class);

	private JTree tree;
	private MutableTreeNode draggedNode = null;
	private DragSource source = new DragSource();

	public PreRegistrationTreeDnDHandler(JTree tree) {
		this.tree = tree;
		new DropTarget(tree, this);
		source.createDefaultDragGestureRecognizer(tree, ACTION_MOVE, this);
	}
	
	/**
	 * Called when a node has been dragged onto another node (nodes could be the
	 * same, client code should handle this correctly).
	 * 
	 * @param source
	 *            the node the drag was initiated from
	 * @param target
	 *            the node the drag ended on
	 */
	public abstract void handleNodeMove(MutableTreeNode source,
			MutableTreeNode target);

	/**
	 * Called when a file is dragged from the native OS to the tree.
	 * 
	 * @param target
	 *            the node the drag ended on
	 * @param fileList
	 *            a list of File objects corresponding to the selection dragged.
	 *            Files may be directories, remember to check!
	 */
	public abstract void handleFileDrop(MutableTreeNode target,
			List<File> fileList);

	/**
	 * Called when a URL is dropped, on my system at least Firefox presents this
	 * type if a link or similar is dragged to the tree.
	 * 
	 * @param target
	 *            the node the drag ended on
	 * @param url
	 *            a java URL object
	 */
	public abstract void handleUrlDrop(MutableTreeNode target, URL url);

	/**
	 * Called when a textual type is dragged to the tree
	 * 
	 * @param target
	 *            the node the drag ended on
	 * @param string
	 *            a java String object containing the dragged text
	 */
	public abstract void handleStringDrop(MutableTreeNode target, String string);

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		dtde.acceptDrag(ACTION_MOVE);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		dtde.acceptDrag(ACTION_MOVE);
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		Point pt = dtde.getLocation();
		DropTargetContext dtc = dtde.getDropTargetContext();
		JTree tree = (JTree) dtc.getComponent();
		TreePath targetPath = tree.getClosestPathForLocation(pt.x, pt.y);
		MutableTreeNode target = null;
		if (targetPath != null)
			target = (MutableTreeNode) targetPath.getLastPathComponent();
		Transferable tr = dtde.getTransferable();
		if (tr.isDataFlavorSupported(INTERNAL_NODE_FLAVOR)) {
			dtde.acceptDrop(ACTION_MOVE);
			handleNodeMove(draggedNode, target);
			draggedNode = null;
			dtde.dropComplete(true);
			return;
		} else if (tr.isDataFlavorSupported(javaFileListFlavor)) {
			dtde.acceptDrop(ACTION_COPY);
			try {
				@SuppressWarnings("unchecked")
				List<File> fileList = (List<File>) tr
						.getTransferData(javaFileListFlavor);
				handleFileDrop(target, fileList);
				dtde.dropComplete(true);
			} catch (Exception ex) {
				dtde.dropComplete(false);
			}
			return;
		}

		for (DataFlavor flavor : tr.getTransferDataFlavors()) {
			String mimeType = flavor.getMimeType();
			if (mimeType.startsWith("application/x-java-url")) {
				dtde.acceptDrop(ACTION_COPY);
				URL url;
				try {
					url = (URL) tr.getTransferData(flavor);
					handleUrlDrop(target, url);
					dtde.dropComplete(true);
				} catch (UnsupportedFlavorException|IOException e) {
					dtde.dropComplete(false);
					logger.error("Cannot import data", e);
				}
				return;
			} else if (mimeType.contains("class=java.lang.String;")) {
				dtde.acceptDrop(ACTION_COPY);
				String string;
				try {
					string = (String) tr.getTransferData(flavor);
					handleStringDrop(target, string);
					dtde.dropComplete(true);
				} catch (UnsupportedFlavorException | IOException e) {
					dtde.dropComplete(false);
					logger.error("Cannot import data", e);
				}
				return;
			}
		}
		dtde.rejectDrop();
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		TreePath path = tree.getSelectionPath();
		if (path == null || path.getPathCount() <= 1)
			// We can't move the root node or an empty selection
			return;
		draggedNode = (MutableTreeNode) path.getLastPathComponent();
		source.startDrag(dge, DefaultMoveDrop,
				new InternalNodeDragTransferable(), this);
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	@Override
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	@Override
	public void dragExit(DragSourceEvent dse) {
	}

	@Override
	public void dragOver(DragSourceDragEvent dsde) {
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}

	/**
	 * Dummy data transfer flavor used to check that the drag and drop are
	 * managing the same events.
	 * 
	 * @author Tom Oinn
	 */
	static class InternalNodeDragTransferable implements Transferable {
		static DataFlavor INTERNAL_NODE_FLAVOR = new DataFlavor(
				InternalNodeDragTransferable.class, "Internal node move");

		private DataFlavor flavors[] = { INTERNAL_NODE_FLAVOR };

		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			// There is no data for this, so return null
			return null;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.getRepresentationClass() == InternalNodeDragTransferable.class;
		}
	}
}
