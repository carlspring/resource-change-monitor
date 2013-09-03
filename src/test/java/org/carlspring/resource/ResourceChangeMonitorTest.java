package org.carlspring.resource;

import org.carlspring.maven.commons.util.ChecksumUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

/**
 * @author mtodorov
 */
public class ResourceChangeMonitorTest
{

    public static final String DIR_TEST_RESOURCES = "target/test-resources";

    private ResourceChangeMonitor monitor;


    @Before
    public void setUp()
            throws Exception
    {
        final File dir = new File(DIR_TEST_RESOURCES);
        if (!dir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        File file1 = new File(DIR_TEST_RESOURCES, "file1"); // size changed
        File file2 = new File(DIR_TEST_RESOURCES, "file2"); // contents changed
        File file3 = new File(DIR_TEST_RESOURCES, "file3"); // deleted
        File file4 = new File(DIR_TEST_RESOURCES, "file4"); // unchanged

        createFileWithRandomContents(file1, 10 * 1024);
        createFileWithRandomContents(file2, 9 * 1024);
        createFileWithRandomContents(file3, 8 * 1024);
        createFileWithRandomContents(file4, 7 * 1024);

        monitor = new ResourceChangeMonitor();
        monitor.addResource(new Resource(file1, getHashFor(file1), 10 * 1024));
        monitor.addResource(new Resource(file2, getHashFor(file2), 9 * 1024));
        monitor.addResource(new Resource(file3, getHashFor(file3), 8 * 1024));
        monitor.addResource(new Resource(file4, getHashFor(file4), 7 * 1024));
    }

    @Test
    public void testObserving()
            throws InterruptedException
    {
        Observer observer = new Observer();

        monitor.addListener(observer);
        monitor.start();

        ResourceChanger resourceChanger = new ResourceChanger();
        resourceChanger.start();

        while (!observer.file3Deleted)
        {
            Thread.sleep(500);
        }

        assertEquals("Event-handling failed for file1!", true, observer.file1SizeChanged);
        assertEquals("Event-handling failed for file2!", true, observer.file2ContentsChanged);
        assertEquals("Event-handling failed for file3!", true, observer.file3Deleted);
        assertEquals("Event-handling failed for file4!", false, observer.file4Changed);
    }

    private String getHashFor(File file)
            throws IOException, NoSuchAlgorithmException
    {
        return ChecksumUtils.getMD5Checksum(file);
    }

    private void createFileWithRandomContents(File file1, int length)
            throws IOException
    {
        InputStream is = null;
        FileOutputStream fos = null;

        try
        {
            is = new RandomInputStream(length);
            fos = new FileOutputStream(file1);

            byte[] buffer = new byte[1024];
            while ((length = is.read(buffer)) != -1)
            {
                fos.write(buffer, 0, length);
                fos.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }

            if (fos != null)
            {
                fos.close();
            }
        }
    }

    private class ResourceChanger extends Thread
    {

        @Override
        public void run()
        {
            try
            {
                Thread.sleep(1000);

                File file1 = new File(DIR_TEST_RESOURCES, "file1");
                File file2 = new File(DIR_TEST_RESOURCES, "file2");
                File file3 = new File(DIR_TEST_RESOURCES, "file3");

                Thread.sleep(1000);

                createFileWithRandomContents(file1, 11 * 1024);

                Thread.sleep(1000);

                createFileWithRandomContents(file2, 9 * 1024);

                Thread.sleep(1000);

                createFileWithRandomContents(file1, 15 * 1024);

                //noinspection ResultOfMethodCallIgnored
                file3.delete();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
    }

    private class Observer implements ResourceChangeListener
    {
        boolean file1SizeChanged = false;
        boolean file2ContentsChanged = false;
        boolean file3Deleted = false;
        boolean file4Changed = false; // Should not have changed


        @Override
        public void handleEvent(ResourceChangeEvent event)
        {
            try
            {
                if (event.getEventType() == ResourceChangeEvent.EVENT_RESOURCE_CONTENTS_CHANGED)
                {
                    System.out.println(event.getResourceFile().getCanonicalPath() + ": contents have changed.");

                    if (event.getResourceFile().getName().equals("file2"))
                    {
                        file2ContentsChanged = true;
                        return;
                    }
                }

                if (event.getEventType() == ResourceChangeEvent.EVENT_RESOURCE_SIZE_CHANGED)
                {
                    System.out.println(event.getResourceFile().getCanonicalPath() + ": size has changed.");

                    if (event.getResourceFile().getName().equals("file1"))
                    {
                        file1SizeChanged = true;
                        return;
                    }
                }

                if (event.getEventType() == ResourceChangeEvent.EVENT_RESOURCE_DELETED && !file3Deleted)
                {
                    System.out.println(event.getResourceFile().getCanonicalPath() + ": has been deleted.");

                    // When a resource has been removed, the associated listener should be removed from the monitor.
                    // Since this test has just one listener, we'll just flip a boolean instead.

                    if (event.getResourceFile().getName().equals("file3"))
                    {
                        file3Deleted = true;
                        return;
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class RandomInputStream
            extends InputStream
    {

        private long count;

        private long length;

        private Random random = new Random(System.currentTimeMillis());


        public RandomInputStream(long length)
        {
            super();
            this.length = length;
        }

        @Override
        public int read()
                throws IOException
        {
            if (count >= length)
            {
                return -1;
            }

            count++;

            return random.nextInt();
        }

        public long getCount()
        {
            return count;
        }

        public void setCount(long count)
        {
            this.count = count;
        }

        public long getLength()
        {
            return length;
        }

        public void setLength(long length)
        {
            this.length = length;
        }
    }

}
