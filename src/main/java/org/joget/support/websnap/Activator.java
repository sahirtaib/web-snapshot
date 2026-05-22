package org.joget.support.websnap;

import org.joget.support.websnap.gotenberg.GotenbergService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import java.util.ArrayList;
import java.util.Collection;

public class Activator implements BundleActivator {

    private Collection<ServiceRegistration> registrationList;
    protected static GotenbergService gotenbergService;
    
    @Override
    public void start(BundleContext context) {
        registrationList = new ArrayList<>();
        
        registrationList.add(
            context.registerService(
                DownloadWebSnap.class.getName(),
                new DownloadWebSnap(),
                null
            )
        );

        /**
         * TODO: Store snapshot files in `wflow` folder
         */
        // registrationList.add(
        //     context.registerService(
        //         StoreWebSnap.class.getName(),
        //         new StoreWebSnap(),
        //         null
        //     )
        // );
    }
    
    @Override
    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }        
    }

    public static GotenbergService getGotenbergService() {
        if (gotenbergService == null) {
            gotenbergService = new GotenbergService();
        }
        return gotenbergService;
    }
}