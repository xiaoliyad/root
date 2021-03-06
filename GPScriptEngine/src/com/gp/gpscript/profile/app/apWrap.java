package com.gp.gpscript.profile.app;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gp.gpscript.profile.ProfileNode;
import com.gp.gpscript.profile.xPathNode;
/**
 The Wrap contains a script. The script operates on the object and has access to the keys and data created for the
 Application Profile in the scripting environment. For example, if the Application Profile identifies a
 GlobalPlatform Security Domain (Type = OP, Subtype = SD or CM), then the script is written for this object.
 */

public class apWrap extends ProfileNode {
	private Logger log = Logger.getLogger(apWrap.class);
	public String Param;

	/**
	 * @see apDeclaration
	 */
	public apDeclaration Declaration[];
	/**
	 * @see apKey
	 */
	public apKey Key[];
	/**
	 * @see apScript
	 */
	public apScript Script;

	public apWrap(Node node) {
		super(node);
		if (node.hasAttributes()) {
			Node attr;
			NamedNodeMap map = node.getAttributes();
			attr = map.getNamedItem("Param");
			if (attr != null)
				Param = attr.getNodeValue();
		}
		int i;
		String xpString;
		NodeList nl;

		xpString = "Declaration";
		try {
			nl = xPathNode.getNodeList(xpString, node);

			if (nl.getLength() > 0)

			{
				Declaration = new apDeclaration[nl.getLength()];
				for (i = 0; i < nl.getLength(); i++) {
					Declaration[i] = new apDeclaration(nl.item(i));
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		}

		xpString = "Key";
		try {
			nl = xPathNode.getNodeList(xpString, node);

			if (nl.getLength() > 0) {
				Key = new apKey[nl.getLength()];
				for (i = 0; i < nl.getLength(); i++) {
					Key[i] = new apKey(nl.item(i));
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		}

		xpString = "Script";
		try {
			nl = xPathNode.getNodeList(xpString, node);

			if (nl.getLength() > 0) {
				Script = new apScript(nl.item(0));
			}
		} catch (Exception e) {
			// e.printStackTrace();
			log.error(e.getMessage());
		}
	}

}