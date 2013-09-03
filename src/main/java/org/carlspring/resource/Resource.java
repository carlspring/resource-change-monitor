package org.carlspring.resource;

import java.io.File;

/**
 * @author mtodorov
 */
public class Resource
{

    private File file;

    private long length;

    private String hash;


    public Resource()
    {
    }

    public Resource(File file,
                    String hash,
                    long length)
    {
        this.file = file;
        this.length = length;
        this.hash = hash;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public long getLength()
    {
        return length;
    }

    public void setLength(long length)
    {
        this.length = length;
    }

    public String getHash()
    {
        return hash;
    }

    public void setHash(String hash)
    {
        this.hash = hash;
    }

}
