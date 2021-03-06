package com.gp.gpscript.profile.loadfile;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gp.gpscript.profile.ProfileNode;
import com.gp.gpscript.profile.xPathNode;

//import com.watchdata.wdcams.loader.Loader;
/**
 * Details for one particular revision of the profile. Used by all profiles to track ownership and modification of the profile.
 */
public class lpRevision extends ProfileNode {
	private Logger log = Logger.getLogger(lpRevision.class);
	/**
	 * UniqueID of the Profile that was utilized as a source for the current Profile.
	 */
	public String ProfileID;
	/**
	 * Version of Load File Profile conforming to GP versioning specification. Version number is x.x.x where each x is a decimal number. If the version of the specification has no last x value, then version number is x.x.0. Example: 1.0.0
	 */
	public String Version;
	/**
	 * Date when the Profile was modified.
	 */
	public String Date;
	/**
	 * Time when the Profile was modified.
	 */
	public String Time;
	/**
	 * Who modified the Profile?
	 */
	public String By;
	/**
	 * Digest of Profile that was modified. This is the MD5 digest for the complete DOM representation of the XML document with the Digest attribute left empty.
	 */
	public String Digest;
	/**
	 * @see lpDescription
	 */
	public lpDescription Description;
	/**
	 * @see lpSignature
	 */
	public lpSignature Signature;

	public lpRevision(Node node) {
		super(node);
		if (node.hasAttributes()) {
			Node attr;
			NamedNodeMap map = node.getAttributes();

			attr = map.getNamedItem("ProfileID");
			if (attr != null)
				ProfileID = attr.getNodeValue();

			attr = map.getNamedItem("Version");
			if (attr != null)
				Version = attr.getNodeValue();

			attr = map.getNamedItem("Date");
			if (attr != null)
				Date = attr.getNodeValue();

			attr = map.getNamedItem("Time");
			if (attr != null)
				Time = attr.getNodeValue();

			attr = map.getNamedItem("By");
			if (attr != null)
				By = attr.getNodeValue();

			attr = map.getNamedItem("Digest");
			if (attr != null)
				Digest = attr.getNodeValue();
		}

		String xpString;
		NodeList nl;
		try {
			xpString = "Description";
			nl = xPathNode.getNodeList(xpString, node);
			if (nl.getLength() > 0)
				Description = new lpDescription(nl.item(0));
		} catch (Exception e) {
			// e.printStackTrace();
			log.error("Description " + e.getMessage());
		}
		try {
			xpString = "Signature";
			nl = xPathNode.getNodeList(xpString, node);
			if (nl.getLength() > 0)
				Signature = new lpSignature(nl.item(0));
		} catch (Exception e) {
			// e.printStackTrace();
			log.error("Signature " + e.getMessage());
		}
	}
}