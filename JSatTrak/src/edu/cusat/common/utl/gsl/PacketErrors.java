package edu.cusat.common.utl.gsl;

import java.util.ArrayList;
import java.util.Arrays;

public class PacketErrors extends Exception
{
    private static final long serialVersionUID = 4330117092198841564L;

    private String cPktName;

    private String cArrayName;

    private ArrayList<PacketError> cPktErrors = new ArrayList<PacketError>();

    private PacketErrors(String message)
    {
        super(message);
    }

    public PacketErrors(String mPktName, String mArrayName)
    {
        cPktName = mPktName;
        cArrayName = mArrayName;
    }

    public PacketErrors throwable()
    {
        return new PacketErrors(this.toString());
    }

    public void add(int mIndex, byte mExpected, byte mFound)
    {
        cPktErrors.add(new PacketError(mIndex, new byte[] { mExpected },
                new byte[] { mFound }));
    }

    public void add(int mIndex, byte mExpected[], byte mFound[])
    {
        cPktErrors.add(new PacketError(mIndex, (byte[]) mExpected.clone(),
                (byte[]) mFound.clone()));
    }

    public String toString()
    {
        String ret;
        if (hasErrors())
        {
            ret = "The following errors were found creating " + cPktName
                    + " from " + cArrayName + "\n";
            for (int i = 0; i < cPktErrors.size(); i++)
            {
                ret += cPktErrors.get(i) + "\n";
            }
        } else
        {
            ret = "There were no errors found creating " + cPktName + " from "
                    + cArrayName;
        }
        return ret;
    }

    public boolean hasErrors()
    {
        return !cPktErrors.isEmpty();
    }

    private static class PacketError
    {
        private int cIndex;

        private String cExpected;

        private String cFound;

        private PacketError(int mIndex, byte[] mExpected, byte[] mFound)
        {
        	cExpected = Arrays.toString(mExpected);
        	cFound = Arrays.toString(mFound);
            cIndex = mIndex;
        }

        public String toString()
        {
            return "\tIndex: " + cIndex + "\tExpected: " + cExpected
                    + "\tFound: " + cFound;
        }
    }

}
