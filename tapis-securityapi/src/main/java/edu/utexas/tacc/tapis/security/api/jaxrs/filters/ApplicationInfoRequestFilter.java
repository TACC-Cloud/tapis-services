package edu.utexas.tacc.tapis.security.api.jaxrs.filters;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utexas.tacc.tapis.security.config.RuntimeParameters;
import edu.utexas.tacc.tapis.shared.TapisConstants;

/** This filter logs the web application's start up information the first time 
 * it's called, i.e., on the first request.  Most of the information logged is 
 * collected by the RuntimeParameters class, which initializes as a side effect
 * of this filter's execution.
 * 
 * @author rcardone
 */
@Provider
@Priority(TapisConstants.JAXRS_FILTER_PRIORITY_AFTER_AUTHENTICATION_3)
public class ApplicationInfoRequestFilter 
 implements ContainerRequestFilter
{
    /* ********************************************************************** */
    /*                               Constants                                */
    /* ********************************************************************** */
    // Tracing.
    private static final Logger _log = LoggerFactory.getLogger(ApplicationInfoRequestFilter.class);
    
    /* ********************************************************************** */
    /*                                Fields                                  */
    /* ********************************************************************** */
    // Only print application level information on the first request.
    private static boolean _firstRequest = true;
    
    /* ********************************************************************** */
    /*                            Public Methods                              */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* filter:                                                                */
    /* ---------------------------------------------------------------------- */
    @Override
    public void filter(ContainerRequestContext requestContext) 
    {
        // We only write our information on the first request to the application.
        if (!_firstRequest) return;
        
        // Stop others from printing application information. Even though it's
        // not critical, we avoid race conditions by using a synchronized block.
        synchronized (ApplicationInfoRequestFilter.class) {
            if (!_firstRequest) return;
            _firstRequest = false;
        }
        
        // Should we even try...
        if (!_log.isInfoEnabled()) return;
        
        // Get the initialized runtime configuration.
        RuntimeParameters runParms = RuntimeParameters.getInstance();
          
        // Dump the parms.
        StringBuilder buf = new StringBuilder(2500); // capacity to avoid resizing
        buf.append("\n------- Starting Security Kernel Service ");
        buf.append(" -------");
        buf.append("\nBase URL: ");
        buf.append(requestContext.getUriInfo().getBaseUri().toString());
        
        // Dump the runtime configuration.
        runParms.getRuntimeInfo(buf);
        buf.append("\n---------------------------------------------------\n");
        
        // Write the output information.
        _log.info(buf.toString());
    }
}
