package org.carlspring.resource;

import java.io.File;

/**
 * @author mtodorov
 */
public class ResourceChangeEvent
{

    public static final int EVENT_RESOURCE_SIZE_CHANGED = 1;

    public static final int EVENT_RESOURCE_CONTENTS_CHANGED = 2;

    public static final int EVENT_RESOURCE_DELETED = 3;

    private File resourceFile;

    private int eventType;


    public ResourceChangeEvent()
    {
    }

    public ResourceChangeEvent(int eventType, File file)
    {
        this.eventType = eventType;
        this.resourceFile = file;
    }

    public File getResourceFile()
    {
        return resourceFile;
    }

    public void setResourceFile(File resourceFile)
    {
        this.resourceFile = resourceFile;
    }

    public int getEventType()
    {
        return eventType;
    }

    public void setEventType(int eventType)
    {
        this.eventType = eventType;
    }

}
