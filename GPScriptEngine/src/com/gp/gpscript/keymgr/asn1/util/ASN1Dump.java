package com.gp.gpscript.keymgr.asn1.util;

import java.util.*;

import com.gp.gpscript.keymgr.asn1.*;
import com.gp.gpscript.keymgr.util.encoders.Hex;


public class ASN1Dump
{
    private static String  TAB = "    ";

    /**
     * dump a DER object as a formatted string with indentation
     *
     * @param obj the DERObject to be dumped out.
     */
    public static String _dumpAsString(
        String      indent,
        DERObject   obj)
    {
        if (obj instanceof DERConstructedSequence)
        {
            StringBuffer    buf = new StringBuffer();
            Enumeration     e = ((DERConstructedSequence)obj).getObjects();
            String          tab = indent + TAB;

            buf.append(indent);
			if (obj instanceof BERConstructedSequence)
			{
	            buf.append("BER ConstructedSequence");
			}
			else
			{
	            buf.append("ConstructedSequence");
			}
            buf.append(System.getProperty("line.separator"));

            while (e.hasMoreElements())
            {
                Object  o = e.nextElement();

                if (o == null)
                {
                    buf.append(tab);
                    buf.append("NULL");
                    buf.append(System.getProperty("line.separator"));
                }
                else if (o instanceof DERObject)
                {
                    buf.append(_dumpAsString(tab, (DERObject)o));
                }
                else
                {
                    buf.append(_dumpAsString(tab, ((DEREncodable)o).getDERObject()));
                }
            }
            return buf.toString();
        }
        else if (obj instanceof DERTaggedObject)
        {
            StringBuffer    buf = new StringBuffer();
            String          tab = indent + TAB;

            buf.append(indent);
			if (obj instanceof BERTaggedObject)
			{
            	buf.append("BER Tagged [");
			}
			else
			{
            	buf.append("Tagged [");
			}

            DERTaggedObject o = (DERTaggedObject)obj;

            buf.append(Integer.toString(o.getTagNo()));
            buf.append("]");

            if (!o.isExplicit())
            {
                buf.append(" IMPLICIT ");
            }

            buf.append(System.getProperty("line.separator"));

            if (o.isEmpty())
            {
                buf.append(tab);
                buf.append("EMPTY");
                buf.append(System.getProperty("line.separator"));
            }
            else
            {
                buf.append(_dumpAsString(tab, o.getObject()));
            }

            return buf.toString();
        }
        else if (obj instanceof DERConstructedSet)
        {
            StringBuffer    buf = new StringBuffer();
            Enumeration     e = ((DERConstructedSet)obj).getObjects();
            String          tab = indent + TAB;

            buf.append(indent);
            buf.append("ConstructedSet");
            buf.append(System.getProperty("line.separator"));

            while (e.hasMoreElements())
            {
                Object  o = e.nextElement();

                if (o == null)
                {
                    buf.append(tab);
                    buf.append("NULL");
                    buf.append(System.getProperty("line.separator"));
                }
                else if (o instanceof DERObject)
                {
                    buf.append(_dumpAsString(tab, (DERObject)o));
                }
                else
                {
                    buf.append(_dumpAsString(tab, ((DEREncodable)o).getDERObject()));
                }
            }
            return buf.toString();
        }
        else if (obj instanceof DERSet)
        {
            StringBuffer    buf = new StringBuffer();
            String          tab = indent + TAB;

            buf.append(indent);
            buf.append("Set");
            buf.append(System.getProperty("line.separator"));

            buf.append(_dumpAsString(tab, ((DERSet)obj).getSequence()));

            return buf.toString();
        }
        else if (obj instanceof DERObjectIdentifier)
        {
            return indent + "ObjectIdentifier(" + ((DERObjectIdentifier)obj).getId() + ")" + System.getProperty("line.separator");
        }
        else if (obj instanceof DERBoolean)
        {
            return indent + "Boolean(" + ((DERBoolean)obj).isTrue() + ")" + System.getProperty("line.separator");
        }
        else if (obj instanceof DERInteger)
        {
            return indent + "Integer(" + ((DERInteger)obj).getValue() + ")" + System.getProperty("line.separator");
        }
        else if (obj instanceof DEROctetString)
        {
            return indent + obj.toString() + "[" + ((DEROctetString)obj).getOctets().length + "] " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERIA5String)
        {
            return indent + "IA5String(" + ((DERIA5String)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERPrintableString)
        {
            return indent + "PrintableString(" + ((DERPrintableString)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERVisibleString)
        {
            return indent + "VisibleString(" + ((DERVisibleString)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERBMPString)
        {
            return indent + "BMPString(" + ((DERBMPString)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERT61String)
        {
            return indent + "T61String(" + ((DERT61String)obj).getString() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERUTCTime)
        {
            return indent + "UTCTime(" + ((DERUTCTime)obj).getTime() + ") " + System.getProperty("line.separator");
        }
        else if (obj instanceof DERUnknownTag)
        {
            return indent + "Unknown " + Integer.toString(((DERUnknownTag)obj).getTag(), 16) + " " + new String(Hex.encode(((DERUnknownTag)obj).getData())) + System.getProperty("line.separator");
        }
        else
        {
            return indent + obj.toString() + System.getProperty("line.separator");
        }
    }

    /**
     * dump out a DER object as a formatted string
     *
     * @param obj the DERObject to be dumped out.
     */
    public static String dumpAsString(
        Object   obj)
    {
		if (obj instanceof DERObject)
		{
        	return _dumpAsString("", (DERObject)obj);
		}
		else if (obj instanceof DEREncodable)
		{
        	return _dumpAsString("", ((DEREncodable)obj).getDERObject());
		}

		return "unknown object type " + obj.toString();
    }
}
