
package common;

import org.w3c.dom.*;
import javax.xml.xpath.*;
import java.util.*;


public class Xml {

	public static String getAttrText (Node node, String attrName)
	{
		NamedNodeMap attrs = node.getAttributes();
		for (int i= 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			if (attr.getNodeName().equals(attrName))
				return attr.getNodeValue();
		}
		return "";
	}

	public static String getNodeText (Node parent, String name)
	{
		Node child = getNode(parent, name);
		if (child != null) {
			//return child.getTextContent();
			NodeList childNodes = child.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node data = childNodes.item(i);
				if (data.getNodeType() == Node.TEXT_NODE)
					return data.getNodeValue();
			}
		}
		return "";
	}

	public static Node getNode (Node parent, String name)
	{
		if (parent != null) {
			Node child = parent.getFirstChild();
			while (child != null) {
				if (child.getNodeType()==Node.ELEMENT_NODE && name.equals(child.getNodeName()))	{
					return child;
				}
				child = child.getNextSibling();
			}
		}
		return null;
	}

	/*
	<parent>
	 <name>txt</name>
	</parent>
	*/
	public static Node addNode (Node parent, String name, String txt)
	{
		Document dom = parent.getOwnerDocument();
		Node node = dom.createElement(name);
		node.appendChild(dom.createTextNode(txt==null?"":txt));
		parent.appendChild(node);
		return node;
	}

	/*
	<parent>
	 <name>
	  <name2>txt</name2>
	 </name>
	</parent>
	*/
	public static Node addNode2 (Node parent, String name, String name2, String txt)
	{
		Document dom = parent.getOwnerDocument();
		Node node = dom.createElement(name);
		addNode(node, name2, txt);
		parent.appendChild(node);
		return node;
	}

	/*
	<parent>
	 <name1>
	  <name2>
	   <name3>txt</name3> 
	  </name2>
	 <name1>
	</parent>
	*/
	public static Node addNodeList (Node parent, String name1, String name2, String name3, List<String> txt)
	{
		Document dom = parent.getOwnerDocument();
		Node node = dom.createElement(name1);
		if (txt!=null)
			for (String s : txt) {
				addNode2(node, name2, name3, s);
			}
		parent.appendChild(node);
		return node;
	}

	public static Node addNodeListPair (Node parent, String name1, String name2, String name3, String name4, List<StringIntPair> paList)
	{
		Document dom = parent.getOwnerDocument();
		Node node = dom.createElement(name1);
		if (paList!=null)
			for (StringIntPair pa : paList) {
				Node n=addNode2(node, name2, name3, pa.getString());
				addNode(n, name4, pa.getIntStr());
			}
		parent.appendChild(node);
		return node;
	}

	/*
	<parent>
	 <name/>
	</parent> 
	*/
	public static Node addElement (Node parent, String name)
	{
		Document dom = parent.getOwnerDocument();
		Node ele = dom.createElement(name);
		parent.appendChild(ele);
		return ele;
	}

	public static String getNodeText (Document dom, String xpathStr)
	{
		//String txt = null;
		String txt = "";
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xp = xpf.newXPath();
		try {
			txt = (String)xp.evaluate(xpathStr, dom);
			//System.out.format("getNodeText %s=%s%n", xpathStr, txt);
		}
		catch (Exception e) {
			System.err.format("getNodeText %s%n", xpathStr);
			e.printStackTrace();
		}
		return txt;
	}

	public static boolean addNode (Document dom, String xpathStr, String txt)
	{
		boolean rc = false;
		Element ele = dom.getDocumentElement();
		if (xpathStr.charAt(0)=='/') xpathStr = xpathStr.substring(1);
		String[] sa = xpathStr.split("/");
		int cnt = sa.length;
		for (int i=0; i<cnt && ele != null; i++) {
			//System.out.println(sa[i]);
			if (i==cnt-1) {
				Element ele2 = dom.createElement(sa[i]);
				ele2.appendChild(dom.createTextNode(txt==null?"":txt));
				Node n = ele.getFirstChild();
				if (n==null) ele.appendChild(ele2); else ele.insertBefore(ele2, n);
				rc = true;
				//System.out.format("addNode(%s=%s) ok%n", xpathStr, txt);
			}
			//ignore 1st level node
			else if (i > 0) {
				Element eleFound = null;
				for (Node child = ele.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getNodeType()==Node.ELEMENT_NODE && sa[i].equals(child.getNodeName())) {
						eleFound = (Element)child;
						break;
					}
				}
				ele = eleFound;
			}
		}
		if (!rc)
			System.err.format("addNode(%s, %s) error%n", xpathStr, txt);
		return rc;
	}

	public static Node getNode (Document dom, String xpathStr)
	{
		Node node = null;
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xp = xpf.newXPath();
		try {
			XPathExpression expr = xp.compile(xpathStr);
			NodeList nl = (NodeList) expr.evaluate(dom, XPathConstants.NODESET);
			if (nl != null && nl.getLength() > 0) {
				node = nl.item(0);
				//System.out.format("%s=%s%n", node.getNodeName(), node.getTextContent());
			}
		}
		catch (Exception e) {
			System.err.format("getNode %s%n", xpathStr);
			e.printStackTrace();
		}
		return node;
	}

	public static Node getFstNode (Element el, String name) { 
		NodeList nl = el.getElementsByTagName(name);
		if (nl != null && nl.getLength() > 0) 
				return nl.item(0);
		return null;
	}

	public static String getFstNodeText (Element el, String name) { 
		Node node = getFstNode(el,name);
		if (node != null) return node.getTextContent();
		return null;
	}

	public static void maskPolicyCode (Document dom, String xpathStr) {
		Node node = getNode(dom, xpathStr);
		maskPolicyCode(dom, node);
	}
	public static void maskPolicyCode (Document dom, Node node) 
	{
		if (node != null) {
			Node parent = node.getParentNode();
			String name = node.getNodeName();
			String str = node.getTextContent();
			if (!Utils.isEmpty(str)) {
				int len = str.length();
				StringBuilder sb = new StringBuilder(str);
				sb.setCharAt(len-2, '*');
				sb.setCharAt(len-3, '*');
				sb.setCharAt(len-4, '*');
				sb.setCharAt(len-5, '*');
				String nstr = sb.toString();
				//System.out.format("%s->%s%n", str, nstr);
				dom.renameNode(node, null, name+"Org");
				addNode(parent, name, nstr);
			}
		}
	}

	public static void maskID (Document dom, String xpathStr) {
		Node node = getNode(dom, xpathStr);
		maskID(dom, node);
	}
	public static void maskID (Document dom, Node node) 
	{
		if (node != null) {
			Node parent = node.getParentNode();
			String name = node.getNodeName();
			String str = node.getTextContent();
			if (!Utils.isEmpty(str)) {
				int len = str.length();
				StringBuilder sb = new StringBuilder(str);
				for (int i=3; i<len-3; i++)
					sb.setCharAt(i, '*');
				String nstr = sb.toString();
				//System.out.format("%s->%s%n", str, nstr);
				dom.renameNode(node, null, name+"Org");
				addNode(parent, name, nstr);
			}
		}
	}

    public static void maskBankAccount (Document dom, String xpathStr) {
		Node node = getNode(dom, xpathStr);
		maskBankAccount(dom, node);
	}
	public static void maskBankAccount (Document dom, Node node) 
	{
		if (node != null) {
			Node parent = node.getParentNode();
			String name = node.getNodeName();
			String str = node.getTextContent();
			if (!Utils.isEmpty(str)) {
				int len = str.length();
				StringBuilder sb = new StringBuilder(str);
				for (int i=3; i<len-3; i++)
					sb.setCharAt(i, '*');
				String nstr = sb.toString();
				//System.out.format("%s->%s%n", str, nstr);
				dom.renameNode(node, null, name+"Org");
				addNode(parent, name, nstr);
			}
		}
	}

	public static void maskCreditCard (Document dom, String xpathStr) {
		Node node = getNode(dom, xpathStr);
		maskCreditCard(dom, node);
	}
	public static void maskCreditCard (Document dom, Node node) 
	{
		if (node != null) {
			Node parent = node.getParentNode();
			String name = node.getNodeName();
			String str = node.getTextContent();
			if (!Utils.isEmpty(str)) {
				int len = str.length();
				StringBuilder sb = new StringBuilder(str);
				for (int i=4; i<len-4; i++)
					sb.setCharAt(i, '*');
				String nstr = sb.toString();
				//System.out.format("%s->%s%n", str, nstr);
				dom.renameNode(node, null, name+"Org");
				addNode(parent, name, nstr);
			}
		}
	}
}
