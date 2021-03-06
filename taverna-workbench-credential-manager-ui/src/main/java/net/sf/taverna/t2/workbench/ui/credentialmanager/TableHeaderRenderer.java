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
package net.sf.taverna.t2.workbench.ui.credentialmanager;

import static javax.swing.border.BevelBorder.RAISED;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import net.sf.taverna.t2.workbench.ui.credentialmanager.KeyPairsTableModel;
import net.sf.taverna.t2.workbench.ui.credentialmanager.PasswordsTableModel;
import net.sf.taverna.t2.workbench.ui.credentialmanager.TableHeaderRenderer;
import net.sf.taverna.t2.workbench.ui.credentialmanager.TrustedCertsTableModel;

/**
 * Custom cell renderer for the headers of the tables displaying 
 * the Keystore/Truststore contents.
 */
@SuppressWarnings("serial")
public class TableHeaderRenderer extends DefaultTableCellRenderer {
	private final ImageIcon entryTypeIcon = new ImageIcon(
			TableHeaderRenderer.class
					.getResource("/images/table/entry_heading.png"));

	@Override
	public Component getTableCellRendererComponent(JTable jtKeyStoreTable,
			Object value, boolean bIsSelected, boolean bHasFocus, int iRow,
			int iCol) {
        // Get header renderer
        JLabel header = (JLabel) jtKeyStoreTable.getColumnModel().getColumn(iCol).getHeaderRenderer();

        // The entry type header contains an icon for every table
        if (iCol == 0) {
            header.setText("");
            header.setIcon(entryTypeIcon); // entry type icon (header for the first column of the table)
            header.setHorizontalAlignment(CENTER);
            header.setVerticalAlignment(CENTER);
            header.setToolTipText("Entry type");
        }
        // All other headers contain text
        else {
            header.setText((String) value);
            header.setHorizontalAlignment(LEFT);
            
            // Passwords table
            if (jtKeyStoreTable.getModel() instanceof PasswordsTableModel){
                if (iCol == 1) //Service URL column
					header.setToolTipText("URL of the service username and password will be used for");
				else if (iCol == 2) // Username column
					header.setToolTipText("Username for the service");
			}
            // Key pairs table
			else if (jtKeyStoreTable.getModel() instanceof KeyPairsTableModel) {
				if (iCol == 1) // Owner
					header.setToolTipText("Certificate's owner");
				else if (iCol == 2) // Issuer
					header.setToolTipText("Certificate's issuer");
				else if (iCol == 3) // Serial number
					header.setToolTipText("Certificate's serial number");
            }       
            // Trusted certs table
			else if (jtKeyStoreTable.getModel() instanceof TrustedCertsTableModel) {
				if (iCol == 1) // Owner
					header.setToolTipText("Certificate's owner");
				else if (iCol == 2) // Issuer
					header.setToolTipText("Certificate's issuer");
				else if (iCol == 3) // Serial number
					header.setToolTipText("Certificate's serial number");
            }         
        }
		header.setBorder(new CompoundBorder(new BevelBorder(RAISED),
				new EmptyBorder(0, 5, 0, 5)));
		return header;
	}
}
