package org.carlspring.resource;

import org.carlspring.maven.commons.util.ChecksumUtils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author mtodorov
 */
public class ResourceChangeMonitor
        extends Thread
{

    private Set<Resource> resources = new LinkedHashSet<Resource>();

    private List<ResourceChangeListener> listeners = new ArrayList<ResourceChangeListener>();

    private long interval = 500l;

    private boolean keepRunning = true;


    public ResourceChangeMonitor()
    {
    }

    @Override
    public void run()
    {
        try
        {
            while (shouldKeepRunning())
            {
                try
                {
                    checkResourceForChanges();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                catch (NoSuchAlgorithmException e)
                {
                    e.printStackTrace();
                }

                Thread.sleep(interval);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void checkResourceForChanges()
            throws IOException, NoSuchAlgorithmException
    {
        for (Resource resource : resources)
        {
            final File file = new File(resource.getFile().getCanonicalPath());

            if (!file.exists())
            {
                // When a resource has been removed, the associated listener should be removed from the monitor
                ResourceChangeEvent event = new ResourceChangeEvent(ResourceChangeEvent.EVENT_RESOURCE_DELETED, file);
                notifyListeners(event);

                continue;
            }

            if (file.length() != resource.getLength())
            {
                String hash = ChecksumUtils.getMD5Checksum(file);
                resource.setHash(hash);
                resource.setLength(file.length());

                ResourceChangeEvent event = new ResourceChangeEvent(ResourceChangeEvent.EVENT_RESOURCE_SIZE_CHANGED, file);
                notifyListeners(event);

                continue;
            }

            if (file.length() == resource.getLength())
            {
                String hash = ChecksumUtils.getMD5Checksum(file);

                if (!hash.equals(resource.getHash()))
                {
                    resource.setHash(hash);
                    resource.setLength(file.length());

                    ResourceChangeEvent event = new ResourceChangeEvent(ResourceChangeEvent.EVENT_RESOURCE_CONTENTS_CHANGED, file);
                    notifyListeners(event);

                    //noinspection UnnecessaryContinue
                    continue; // Continuing just in case other event types are added in the future.
                }
            }
        }
    }

    private void notifyListeners(ResourceChangeEvent event)
    {
        for (ResourceChangeListener listener : listeners)
        {
            listener.handleEvent(event);
        }
    }

    public void addResource(String path)
            throws IOException,
                   NoSuchAlgorithmException
    {
        final File file = new File(path).getCanonicalFile();
        String hash = ChecksumUtils.getMD5Checksum(file);

        Resource resource = new Resource();
        resource.setFile(file);
        resource.setHash(hash);
        resource.setLength(file.length());

        resources.add(resource);
    }

    public void addResource(Resource resource)
            throws IOException,
                   NoSuchAlgorithmException
    {
        resources.add(resource);
    }

    public Set<Resource> getResources()
    {
        return resources;
    }

    public void setResources(Set<Resource> resources)
    {
        this.resources = resources;
    }

    public void addListener(ResourceChangeListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(ResourceChangeListener listener)
    {
        listeners.remove(listener);
    }

    public List<ResourceChangeListener> getListeners()
    {
        return listeners;
    }

    public void setListeners(List<ResourceChangeListener> listeners)
    {
        this.listeners = listeners;
    }

    public long getInterval()
    {
        return interval;
    }

    public void setInterval(long interval)
    {
        this.interval = interval;
    }

    public boolean shouldKeepRunning()
    {
        return keepRunning;
    }

    public void setKeepRunning(boolean keepRunning)
    {
        this.keepRunning = keepRunning;
    }

}
