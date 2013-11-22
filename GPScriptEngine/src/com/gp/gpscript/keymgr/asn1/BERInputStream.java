package com.gp.gpscript.keymgr.asn1;

import java.math.BigInteger;
import java.io.*;
import java.util.*;

public class BERInputStream
    extends DERInputStream
{
	private DERObject END_OF_STREAM = new DERObject() {
										void encode(
											DEROutputStream out)
										throws IOException
										{
											throw new IOException("Eeek!");
										}

									};
    public BERInputStream(
        InputStream is)
    {
        super(is);
    }

    /**
     * read a string of bytes representing an indefinite length object.
     */
    private byte[] readIndefiniteLengthFully()
        throws IOException
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        int                     b, b1;

        b1 = read();

        while ((b = read()) >= 0)
        {
			if (b1 == 0 && b == 0)
			{
				break;
			}

            bOut.write(b1);
            b1 = b;
        }

        return bOut.toByteArray();
    }

	private BERConstructedOctetString buildConstructedOctetString()
		throws IOException
	{
        Vector                  octs = new Vector();

		for (;;)
		{
			DERObject		o = readObject();

			if (o == END_OF_STREAM)
			{
				break;
			}

            octs.addElement(o);
		}

		return new BERConstructedOctetString(octs);
	}

    public DERObject readObject()
        throws IOException
    {
        int tag = read();
        if (tag == -1)
        {
            throw new EOFException();
        }

        int     length = readLength();

        if (length < 0)    // indefinite length method
        {
            switch (tag)
            {
            case NULL:
                return null;
            case SEQUENCE | CONSTRUCTED:
                BERConstructedSequence  seq = new BERConstructedSequence();

				for (;;)
				{
					DERObject   obj = readObject();

					if (obj == END_OF_STREAM)
					{
						break;
					}

					seq.addObject(obj);
				}
				return seq;
            case OCTET_STRING | CONSTRUCTED:
				return buildConstructedOctetString();
            default:
                //
                // with tagged object tag number is bottom 5 bits
                //
                if ((tag & (TAGGED | CONSTRUCTED)) != 0)
                {
                    if ((tag & 0x1f) == 0x1f)
                    {
                        throw new IOException("unsupported high tag encountered");
                    }

                    //
                    // simple type - implicit... return an octet string
                    //
                    if ((tag & CONSTRUCTED) == 0)
                    {
                        byte[]  bytes = readIndefiniteLengthFully();

                        if (bytes.length == 0)
                        {
                            return new BERTaggedObject(false, tag & 0x1f);
                        }

                        return new BERTaggedObject(false, tag & 0x1f, new DEROctetString(bytes));
                    }

                    //
                    // either constructed or explicitly tagged
                    //
					DERObject		dObj = readObject();

					if (dObj == END_OF_STREAM)     // empty tag!
                    {
                        return new DERTaggedObject(tag & 0x1f);
                    }

                    DERObject       next = readObject();

                    //
                    // explicitly tagged (probably!) - if it isn't we'd have to
                    // tell from the context
                    //
                    if (next == END_OF_STREAM)
                    {
                        return new BERTaggedObject(tag & 0x1f, dObj);
                    }

                    //
                    // another implicit object, we'll create a sequence...
                    //
                    seq = new BERConstructedSequence();

                    seq.addObject(dObj);

                    do
                    {
                        seq.addObject(next);
                        next = readObject();
                    }
                    while (next != END_OF_STREAM);

                    return new BERTaggedObject(false, tag & 0x1f, seq);
                }

                throw new IOException("unknown BER object encountered");
            }
        }
        else
        {
            if (tag == 0 && length == 0)    // end of contents marker.
            {
                return END_OF_STREAM;
            }

            byte[]  bytes = new byte[length];

            readFully(bytes);

			return buildObject(tag, bytes);
        }
    }
}