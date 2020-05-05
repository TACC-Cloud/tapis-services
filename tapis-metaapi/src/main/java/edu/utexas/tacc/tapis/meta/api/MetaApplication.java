package edu.utexas.tacc.tapis.meta.api;

import edu.utexas.tacc.tapis.meta.config.RuntimeParameters;
import edu.utexas.tacc.tapis.shared.exceptions.TapisException;
import edu.utexas.tacc.tapis.shared.parameters.TapisEnv;
import edu.utexas.tacc.tapis.sharedapi.security.ServiceJWT;
import edu.utexas.tacc.tapis.sharedapi.security.ServiceJWTParms;
import edu.utexas.tacc.tapis.sharedapi.security.TenantManager;
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;


@ApplicationPath("/meta")
public class MetaApplication extends ResourceConfig {
  
  public MetaApplication() throws TapisException {
    // Log our existence.
    System.out.println("**** Starting tapis-metaapi ****");
    
    // Register the swagger resources that allow the
    // documentation endpoints to be automatically generated.
    // TODO expand to all endpoints for auto generation of openapi definition
    register(OpenApiResource.class);
    register(AcceptHeaderOpenApiResource.class);
    
    // We specify what packages JAX-RS should recursively scan
    packages("edu.utexas.tacc.tapis");
    setApplicationName("meta");
  
    // Force runtime initialization of the tenant manager.  This creates the
    // singleton instance of the TenantManager that can then be accessed by
    // all subsequent application code--including filters--without reference
    // to the tenant service base url parameter.
    try {
      // The base url of the tenants service is a required input parameter.
      // We actually retrieve the tenant list from the tenant service now
      // to fail fast if we can't access the list.
      RuntimeParameters runTime = RuntimeParameters.getInstance();
      String url = runTime.getTenantBaseUrl();
      TenantManager.getInstance(url).getTenants();
      
      // Do we also fail if we can't get a service token?
      // todo set runtime set Tokens
      ServiceJWTParms serviceJWTParms = new ServiceJWTParms();
      serviceJWTParms.setServiceName("meta");
      serviceJWTParms.setTenant("master");
      serviceJWTParms.setTokensBaseUrl(runTime.getTenantBaseUrl());
      ServiceJWT serviceJWT = new ServiceJWT(serviceJWTParms, TapisEnv.get(TapisEnv.EnvVar.TAPIS_SERVICE_PASSWORD));
      runTime.setMetaToken(serviceJWT.getAccessJWT());
    } catch (Exception e) {
      // We don't depend on the logging subsystem.
      System.out.println("**** FAILURE TO INITIALIZE: tapis-metaapi ****");
      e.printStackTrace();
      throw e;
    }
  
  
  }

}
