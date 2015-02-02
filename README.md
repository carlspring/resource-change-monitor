
## resource-change-monitor

This is a simple event-driven library for monitoring file changes.

## Maven

This library is available as a Maven artifact from Maven Central, so you don't need to add any additional repositories.

## Usage

In order to use the ResourceChangeMonitor, you will need to implement the ResourceChangeListener as illustrated below:

    private class Observer implements ResourceChangeListener
    {

        @Override
        public void handleEvent(ResourceChangeEvent event)
        {
            try
            {
                if (event.getEventType() == ResourceChangeEvent.EVENT_RESOURCE_CONTENTS_CHANGED)
                {
                    System.out.println(event.getResourceFile().getCanonicalPath() + ": contents have changed.");
                }

                if (event.getEventType() == ResourceChangeEvent.EVENT_RESOURCE_SIZE_CHANGED)
                {
                    System.out.println(event.getResourceFile().getCanonicalPath() + ": size has changed.");
                }

                if (event.getEventType() == ResourceChangeEvent.EVENT_RESOURCE_DELETED)
                {
                    System.out.println(event.getResourceFile().getCanonicalPath() + ": has been deleted.");
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

Then, in order to observe files for changes:

    File file1 = new File(DIR_TEST_RESOURCES, "file1"); // size changed

    Observer observer = new Observer();

    ChangeMonitor monitor = new ChangeMonitor();
    monitor.setInterval(1000); // Check for changes every second
    monitor.addResource(new Resource(file1, getHashFor(file1), 10 * 1024));
    monitor.addListener(observer);
    monitor.start();

For more details and an example implementation, check src/test/java/org/carlspring/resource/ChangeMonitorTest.java.


