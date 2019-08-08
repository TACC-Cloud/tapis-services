package edu.utexas.tacc.tapis.files.api.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import edu.utexas.tacc.tapis.files.lib.clients.RemoteFileInfo;
import edu.utexas.tacc.tapis.files.lib.clients.S3DataClient;

@Path("listings")
@Produces(MediaType.APPLICATION_JSON)
public class ListingsResource {

    @GET
    @Path("{systemId}/{filePath}/")
    public List<RemoteFileInfo> listFiles(@Context SecurityContext sc,
                                          @PathParam("systemId") long systemId,
                                          @PathParam("filePath") String filePath) throws WebApplicationException {
//
//        // TODO: Permissions checks here
////        AuthenticatedUser user = sc.getUserPrincipal();
//
//        try {
//            StorageSystemsDAO dao = new StorageSystemsDAO();
//            StorageSystem system = dao.getStorageSystem("test", "test", 1);
//            S3DataClient client = new S3DataClient(system);
//            return client.ls("/");
//        } catch (IOException e) {
//            throw new WebApplicationException("Could not list files");
//        }
        return new ArrayList();
    }



}
