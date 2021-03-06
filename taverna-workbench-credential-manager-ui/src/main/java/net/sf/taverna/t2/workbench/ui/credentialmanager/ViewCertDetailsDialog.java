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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.GridBagConstraints.LINE_START;
import static javax.security.auth.x500.X500Principal.RFC2253;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.SwingUtilities.invokeLater;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.DistinguishedNameParser;
import net.sf.taverna.t2.security.credentialmanager.ParsedDistinguishedName;
import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Displays the details of a X.509 certificate.
 * 
 * Inspired by the Portlecle tool (http://portecle.sourceforge.net/). and the
 * view certificate dialog from Firefox's Certificate Manager.
 */
@SuppressWarnings("serial")
public class ViewCertDetailsDialog extends NonBlockedHelpEnabledDialog {
	// Logger
	//private static Logger logger = Logger.getLogger(ViewCertDetailsDialog.class);
	
	/** Stores certificate to display*/
	private X509Certificate cert;
	/** Stores list of serviceURLs to display*/
	private ArrayList<String> serviceURLs;
	private final DistinguishedNameParser dnParser;

    /**
     * Creates new ViewCertDetailsDialog dialog where the parent is a frame.
     */
	public ViewCertDetailsDialog(JFrame parent, String title, boolean modal,
			X509Certificate crt, ArrayList<String> serviceURLs,
			DistinguishedNameParser dnParser) throws CMException {
		super(parent, title, modal);
		this.cert = crt;
		this.serviceURLs = serviceURLs;
		this.dnParser = dnParser;
		initComponents();
	}

	/**
	 * Creates new ViewCertDetailsDialog dialog where the parent is a dialog.
	 */
	public ViewCertDetailsDialog(JDialog parent, String title, boolean modal,
			X509Certificate crt, ArrayList<String> urlList,
			DistinguishedNameParser dnParser) throws CMException {
		super(parent, title, modal);
		cert = crt;
		serviceURLs = urlList;
		this.dnParser = dnParser;
		initComponents();
	}

	/**
	 * Initialise the dialog's GUI components.
	 * 
	 * @throws CMException
	 *             A problem was encountered getting the certificates' details
	 */
	private void initComponents() throws CMException {
        // Certificate details:

        // Grid Bag Constraints templates for labels (column 1) and 
    	// values (column 2) of certificate details
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.ipadx = 20;
        gbcLabel.gridwidth = 1;
        gbcLabel.gridheight = 1;
        gbcLabel.insets = new Insets(2, 15, 2, 2);
        gbcLabel.anchor = LINE_START;

        GridBagConstraints gbcValue = new GridBagConstraints();
        gbcValue.gridx = 1;
        gbcValue.gridwidth = 1;
        gbcValue.gridheight = 1;
        gbcValue.insets = new Insets(2, 5, 2, 2);
        gbcValue.anchor = LINE_START;

        /*
		 * Netscape Certificate Type non-critical extension (if any) defines the
		 * intended uses of the certificate - to make it look like firefox's
		 * view certificate dialog. From openssl's documentation: "The [above]
		 * extension is non standard, Netscape specific and largely obsolete.
		 * Their use in new applications is discouraged."
		 * 
		 * TODO replace with "basicConstraints, keyUsage and extended key usage
		 * extensions which are now used instead."
		 */
//        byte[] intendedUses = cert.getExtensionValue("2.16.840.1.113730.1.1"); //Netscape Certificate Type OID/*
//        JLabel jlIntendedUses = null;
//        JTextField jtfIntendedUsesValue = null;
//        JPanel jpUses = null;
//        GridBagConstraints gbc_jpUses = null;
//        if (intendedUses != null)
//        {
//         	jlIntendedUses = new JLabel("This certificate has been approved for the following uses:");
//         	jlIntendedUses.setFont(new Font(null, Font.BOLD, 11));
//         	jlIntendedUses.setBorder(new EmptyBorder(5,5,5,5));
//         	
//         	jtfIntendedUsesValue = new JTextField(45);
//         	jtfIntendedUsesValue.setText(CMUtils.getIntendedCertificateUses(intendedUses));
//        	jtfIntendedUsesValue.setEditable(false);
//        	jtfIntendedUsesValue.setFont(new Font(null, Font.PLAIN, 11));
//             
//        	jpUses = new JPanel(new BorderLayout()); 
//        	jpUses.add(jlIntendedUses, BorderLayout.NORTH);
//        	jpUses.add(jtfIntendedUsesValue, BorderLayout.CENTER);
//        	JSeparator jsp = new JSeparator(JSeparator.HORIZONTAL);
//        	jpUses.add(jsp, BorderLayout.SOUTH);
//        	
//        	gbc_jpUses = (GridBagConstraints) gbcLabel.clone();
//        	gbc_jpUses.gridy = 0;
//        	gbc_jpUses.gridwidth = 2; //takes two columns
//        	gbc_jpUses.insets = new Insets(5, 5, 5, 5);//has slightly bigger insets
//
//        }

        //Issued To
        JLabel jlIssuedTo = new JLabel("Issued To");
        jlIssuedTo.setFont(new Font(null, Font.BOLD, 11));
        GridBagConstraints gbc_jlIssuedTo = (GridBagConstraints) gbcLabel.clone();
        gbc_jlIssuedTo.gridy = 1;
        gbc_jlIssuedTo.gridwidth = 2; //takes two columns
        gbc_jlIssuedTo.insets = new Insets(5, 5, 5, 5);//has slightly bigger insets

        // Distinguished Name (DN)
        String sDN = cert.getSubjectX500Principal().getName(RFC2253);
        ParsedDistinguishedName parsedDN = dnParser.parseDN(sDN);       
        // Extract the CN, O, OU and EMAILADDRESS fields
        String sCN = parsedDN.getCN();
        String sOrg = parsedDN.getO();
        String sOU = parsedDN.getOU();
        //String sEMAILADDRESS = CMX509Util.getEmilAddress();

        // Common Name (CN)
        JLabel jlCN = new JLabel("Common Name (CN)");
        jlCN.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlCN = (GridBagConstraints) gbcLabel.clone();
        gbc_jlCN.gridy = 2;
        JLabel jlCNValue = new JLabel(sCN);
        jlCNValue.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlCNValue = (GridBagConstraints) gbcValue.clone();
        gbc_jlCNValue.gridy = 2;

        // Organisation (O)
        JLabel jlOrg = new JLabel("Organisation (O)");
        jlOrg.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlOrg = (GridBagConstraints) gbcLabel.clone();
        gbc_jlOrg.gridy = 3;
        JLabel jlOrgValue = new JLabel(sOrg);
        jlOrgValue.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlOrgValue = (GridBagConstraints) gbcValue.clone();
        gbc_jlOrgValue.gridy = 3;

        // Organisation Unit (OU)
        JLabel jlOU = new JLabel("Organisation Unit (OU)");
        jlOU.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlOU = (GridBagConstraints) gbcLabel.clone();
        gbc_jlOU.gridy = 4;
        JLabel jlOUValue = new JLabel(sOU);
        jlOUValue.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlOUValue = (GridBagConstraints) gbcValue.clone();
        gbc_jlOUValue.gridy = 4;

        // E-mail Address
        //JLabel jlEmail = new JLabel("E-mail Address");
        //jlEmail.setFont(new Font(null, PLAIN, 11));
        //GridBagConstraints gbc_jlEmail = (GridBagConstraints) gbcLabel.clone();
        //gbc_jlEmail.gridy = 5;
        //JLabel jlEmailValue = new JLabel(sEMAILADDRESS);
        //jlEmailValue.setFont(new Font(null, PLAIN, 11));
        //GridBagConstraints gbc_jlEmailValue = (GridBagConstraints) gbcValue.clone();
        //gbc_jlEmailValue.gridy = 5;

        // Serial Number
        JLabel jlSN = new JLabel("Serial Number");
        jlSN.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlSN = (GridBagConstraints) gbcLabel.clone();
        gbc_jlSN.gridy = 6;
        JLabel jlSNValue = new JLabel();
        // Get the hexadecimal serial number
        StringBuilder strBuff = new StringBuilder(new BigInteger(1,
                cert.getSerialNumber().toByteArray()).toString(16).toUpperCase());
        // Place colons at every two hexadecimal characters
        if (strBuff.length() > 2)
            for (int iCnt = 2; iCnt < strBuff.length(); iCnt += 3)
                strBuff.insert(iCnt, ':');
        jlSNValue.setText(strBuff.toString());
        jlSNValue.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlSNValue = (GridBagConstraints) gbcValue.clone();
        gbc_jlSNValue.gridy = 6;

        // Version
        JLabel jlVersion = new JLabel("Version");
        jlVersion.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlVersion = (GridBagConstraints) gbcLabel.clone();
        gbc_jlVersion.gridy = 7;
        JLabel jlVersionValue = new JLabel(Integer.toString(cert.getVersion()));
        jlVersionValue.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlVersionValue = (GridBagConstraints) gbcValue.clone();
        gbc_jlVersionValue.gridy = 7;

        // Issued By
        JLabel jlIssuedBy = new JLabel("Issued By");
        jlIssuedBy.setFont(new Font(null, BOLD, 11));
        GridBagConstraints gbc_jlIssuedBy = (GridBagConstraints) gbcLabel.clone();
        gbc_jlIssuedBy.gridy = 8;
        gbc_jlIssuedBy.gridwidth = 2; //takes two columns 
        gbc_jlIssuedBy.insets = new Insets(5, 5, 5, 5);//has slightly bigger insets        

        // Distinguished Name (DN)
		String iDN = cert.getIssuerX500Principal().getName(RFC2253);
		parsedDN = dnParser.parseDN(iDN);
		// Extract the CN, O and OU fields
		String iCN = parsedDN.getCN();
		String iOrg = parsedDN.getO();
		String iOU = parsedDN.getOU();

		// Common Name (CN)
		JLabel jlICN = new JLabel("Common Name (CN)");
		jlICN.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlICN = (GridBagConstraints) gbcLabel.clone();
		gbc_jlICN.gridy = 9;
		JLabel jlICNValue = new JLabel(iCN);
		jlICNValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlICNValue = (GridBagConstraints) gbcValue
				.clone();
		gbc_jlICNValue.gridy = 9;

		// Organisation (O)
		JLabel jlIOrg = new JLabel("Organisation (O)");
		jlIOrg.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlIOrg = (GridBagConstraints) gbcLabel.clone();
		gbc_jlIOrg.gridy = 10;
		JLabel jlIOrgValue = new JLabel(iOrg);
		jlIOrgValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlIOrgValue = (GridBagConstraints) gbcValue
				.clone();
		gbc_jlIOrgValue.gridy = 10;

		// Organisation Unit (OU)
		JLabel jlIOU = new JLabel("Organisation Unit (OU)");
		jlIOU.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlIOU = (GridBagConstraints) gbcLabel.clone();
		gbc_jlIOU.gridy = 11;
		JLabel jlIOUValue = new JLabel(iOU);
		jlIOUValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlIOUValue = (GridBagConstraints) gbcValue
				.clone();
		gbc_jlIOUValue.gridy = 11;

		// Validity
		JLabel jlValidity = new JLabel("Validity");
		jlValidity.setFont(new Font(null, BOLD, 11));
		GridBagConstraints gbc_jlValidity = (GridBagConstraints) gbcLabel
				.clone();
		gbc_jlValidity.gridy = 12;
		gbc_jlValidity.gridwidth = 2; // takes two columns
		gbc_jlValidity.insets = new Insets(5, 5, 5, 5);// has slightly bigger insets

		// Issued On
		JLabel jlIssuedOn = new JLabel("Issued On");
		jlIssuedOn.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlIssuedOn = (GridBagConstraints) gbcLabel
				.clone();
		gbc_jlIssuedOn.gridy = 13;
		JLabel jlIssuedOnValue = new JLabel(cert.getNotBefore().toString());
		jlIssuedOnValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlIssuedOnValue = (GridBagConstraints) gbcValue
				.clone();
		gbc_jlIssuedOnValue.gridy = 13;

		// Expires On
		JLabel jlExpiresOn = new JLabel("Expires On");
		jlExpiresOn.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlExpiresOn = (GridBagConstraints) gbcLabel
				.clone();
		gbc_jlExpiresOn.gridy = 14;
		JLabel jlExpiresOnValue = new JLabel(cert.getNotAfter().toString());
		jlExpiresOnValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_jlExpiresOnValue = (GridBagConstraints) gbcValue
				.clone();
		gbc_jlExpiresOnValue.gridy = 14;

		// Fingerprints
		byte[] certBinaryEncoding;
		try {
			certBinaryEncoding = cert.getEncoded();
		} catch (CertificateEncodingException ex) {
			throw new CMException(
					"Could not get the encoded form of the certificate.", ex);
		}
        JLabel jlFingerprints = new JLabel("Fingerprints");
        jlFingerprints.setFont(new Font(null, BOLD, 11));
        GridBagConstraints gbc_jlFingerprints = (GridBagConstraints) gbcLabel.clone();
        gbc_jlFingerprints.gridy = 15;
        gbc_jlFingerprints.gridwidth = 2; //takes two columns  
        gbc_jlFingerprints.insets = new Insets(5, 5, 5, 5);//has slightly bigger insets

        // SHA-1 Fingerprint
        JLabel jlSHA1Fingerprint = new JLabel("SHA1 Fingerprint");
        jlSHA1Fingerprint.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlSHA1Fingerprint = (GridBagConstraints) gbcLabel.clone();
        gbc_jlSHA1Fingerprint.gridy = 16;
        JLabel jlSHA1FingerprintValue = new JLabel(dnParser.getMessageDigestAsFormattedString(certBinaryEncoding, "SHA1"));
        jlSHA1FingerprintValue.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlSHA1FingerprintValue = (GridBagConstraints) gbcValue.clone();
        gbc_jlSHA1FingerprintValue.gridy = 16;

        // MD5 Fingerprint
        JLabel jlMD5Fingerprint = new JLabel("MD5 Fingerprint");
        jlMD5Fingerprint.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlMD5Fingerprint = (GridBagConstraints) gbcLabel.clone();
        gbc_jlMD5Fingerprint.gridy = 17;
        JLabel jlMD5FingerprintValue = new JLabel(dnParser.getMessageDigestAsFormattedString(certBinaryEncoding, "MD5"));
        jlMD5FingerprintValue.setFont(new Font(null, PLAIN, 11));
        GridBagConstraints gbc_jlMD5FingerprintValue = (GridBagConstraints) gbcValue.clone();
        gbc_jlMD5FingerprintValue.gridy = 17;
        
		/*
		 * Empty label to add a bit space at the bottom of the panel to make it
		 * look like firefox's view certificate dialog
		 */
        JLabel jlEmpty = new JLabel("");
		GridBagConstraints gbc_jlEmpty = (GridBagConstraints) gbcLabel.clone();
		gbc_jlEmpty.gridy = 18;
		gbc_jlEmpty.gridwidth = 2; // takes two columns
		gbc_jlEmpty.ipady = 40;

		JPanel jpCertificate = new JPanel(new GridBagLayout());
		jpCertificate.setBorder(new CompoundBorder(new EmptyBorder(15, 15, 15,
				15), new EtchedBorder()));

//        if (intendedUses != null){
//        	jpCertificate.add(jpUses, gbc_jpUses);
//        }
        jpCertificate.add(jlIssuedTo, gbc_jlIssuedTo); // Issued To
        jpCertificate.add(jlCN, gbc_jlCN);
        jpCertificate.add(jlCNValue, gbc_jlCNValue);
        jpCertificate.add(jlOrg, gbc_jlOrg);
        jpCertificate.add(jlOrgValue, gbc_jlOrgValue);        
        jpCertificate.add(jlOU, gbc_jlOU);
        jpCertificate.add(jlOUValue, gbc_jlOUValue);
        //jpCertificate.add(jlEmail, gbc_jlEmail);
        //jpCertificate.add(jlEmailValue, gbc_jlEmailValue);
        jpCertificate.add(jlSN, gbc_jlSN);
        jpCertificate.add(jlSNValue, gbc_jlSNValue);
        jpCertificate.add(jlVersion, gbc_jlVersion);
        jpCertificate.add(jlVersionValue, gbc_jlVersionValue);
        jpCertificate.add(jlIssuedBy, gbc_jlIssuedBy); //Issued By
        jpCertificate.add(jlICN, gbc_jlICN);
        jpCertificate.add(jlICNValue, gbc_jlICNValue);
        jpCertificate.add(jlIOrg, gbc_jlIOrg);
        jpCertificate.add(jlIOrgValue, gbc_jlIOrgValue);        
        jpCertificate.add(jlIOU, gbc_jlIOU);
        jpCertificate.add(jlIOUValue, gbc_jlIOUValue);
        jpCertificate.add(jlValidity, gbc_jlValidity); //Validity
        jpCertificate.add(jlIssuedOn, gbc_jlIssuedOn);
        jpCertificate.add(jlIssuedOnValue, gbc_jlIssuedOnValue);
        jpCertificate.add(jlExpiresOn, gbc_jlExpiresOn);
        jpCertificate.add(jlExpiresOnValue, gbc_jlExpiresOnValue); 
        jpCertificate.add(jlFingerprints, gbc_jlFingerprints); //Fingerprints
        jpCertificate.add(jlSHA1Fingerprint, gbc_jlSHA1Fingerprint);
        jpCertificate.add(jlSHA1FingerprintValue, gbc_jlSHA1FingerprintValue);
        jpCertificate.add(jlMD5Fingerprint, gbc_jlMD5Fingerprint);
        jpCertificate.add(jlMD5FingerprintValue, gbc_jlMD5FingerprintValue);
        jpCertificate.add(jlEmpty, gbc_jlEmpty); //Empty label to get some vertical space on the frame

        // List of serviceURLs
        JPanel jpURLs  = null; // Panel to hold the URL list
		if (serviceURLs != null) { //if service serviceURLs are not null (even if empty - show empty list)

        	jpURLs = new JPanel(new BorderLayout());
        	jpURLs.setBorder(new CompoundBorder(
                    new EmptyBorder(0, 15, 0, 15), new EtchedBorder()));
            // Label
            JLabel jlServiceURLs = new JLabel ("Service URLs this key pair will be used for:");
            jlServiceURLs.setFont(new Font(null, Font.BOLD, 11));
            jlServiceURLs.setBorder(new EmptyBorder(5,5,5,5));    
      
            // New empty service serviceURLs list
			DefaultListModel<String> jltModel = new DefaultListModel<>();
			JList<String> jltServiceURLs = new JList<>(jltModel);
			for (String url : serviceURLs)
				jltModel.addElement(url);
			// don't show more than 5 otherwise the window is too big
            jltServiceURLs.setVisibleRowCount(5);
            
			// Scroll pane for service serviceURLs
			JScrollPane jspServiceURLs = new JScrollPane(jltServiceURLs,
					VERTICAL_SCROLLBAR_AS_NEEDED,
					HORIZONTAL_SCROLLBAR_AS_NEEDED);
			jspServiceURLs.getViewport().setBackground(
					jltServiceURLs.getBackground());

			jpURLs.add(jlServiceURLs, NORTH);
			jpURLs.add(jspServiceURLs, CENTER);

			// Put it on the main content pane
			getContentPane().add(jpURLs, CENTER);
		}

		// OK button
		JPanel jpOK = new JPanel(new FlowLayout(FlowLayout.CENTER));

		final JButton jbOK = new JButton("OK");
		jbOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				okPressed();
			}
		});

		jpOK.add(jbOK);

		/*
		 * Put it all together (panel with URL list is already added, if it was
		 * not null)
		 */
		getContentPane().add(jpCertificate, NORTH);
		getContentPane().add(jpOK, SOUTH);

		// Resizing wreaks havoc
		setResizable(false);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		getRootPane().setDefaultButton(jbOK);

		pack();

		invokeLater(new Runnable() {
			@Override
			public void run() {
				jbOK.requestFocus();
			}
		});
	}

	private void okPressed() {
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}
}
