package edu.utexas.tacc.tapis.security.api.resources;

import java.io.InputStream;
import java.util.List;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utexas.tacc.tapis.security.api.requestBody.ReqAddChildRole;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqAddRolePermission;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqCreateRole;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqPreviewPathPrefix;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqRemoveChildRole;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqRemovePermissionFromAllRoles;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqRemoveRolePermission;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqReplacePathPrefix;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqUpdateRoleDescription;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqUpdateRoleName;
import edu.utexas.tacc.tapis.security.api.requestBody.ReqUpdateRoleOwner;
import edu.utexas.tacc.tapis.security.api.responses.RespPathPrefixes;
import edu.utexas.tacc.tapis.security.api.responses.RespRole;
import edu.utexas.tacc.tapis.security.api.utils.SKApiUtils;
import edu.utexas.tacc.tapis.security.api.utils.SKCheckAuthz;
import edu.utexas.tacc.tapis.security.authz.impl.RoleImpl;
import edu.utexas.tacc.tapis.security.authz.model.SkRole;
import edu.utexas.tacc.tapis.security.authz.permissions.PermissionTransformer.Transformation;
import edu.utexas.tacc.tapis.shared.i18n.MsgUtils;
import edu.utexas.tacc.tapis.shared.threadlocal.TapisThreadLocal;
import edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount;
import edu.utexas.tacc.tapis.sharedapi.responses.RespName;
import edu.utexas.tacc.tapis.sharedapi.responses.RespNameArray;
import edu.utexas.tacc.tapis.sharedapi.responses.RespResourceUrl;
import edu.utexas.tacc.tapis.sharedapi.responses.results.ResultChangeCount;
import edu.utexas.tacc.tapis.sharedapi.responses.results.ResultName;
import edu.utexas.tacc.tapis.sharedapi.responses.results.ResultNameArray;
import edu.utexas.tacc.tapis.sharedapi.responses.results.ResultResourceUrl;
import edu.utexas.tacc.tapis.sharedapi.utils.TapisRestUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Path("/role")
public final class RoleResource 
 extends AbstractResource
{
    /* **************************************************************************** */
    /*                                   Constants                                  */
    /* **************************************************************************** */
    // Local logger.
    private static final Logger _log = LoggerFactory.getLogger(RoleResource.class);
    
    // Json schema resource files.
    private static final String FILE_SK_CREATE_ROLE_REQUEST = 
        "/edu/utexas/tacc/tapis/security/api/jsonschema/CreateRoleRequest.json";
    private static final String FILE_SK_UPDATE_ROLE_NAME_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/UpdateRoleNameRequest.json";
    private static final String FILE_SK_UPDATE_ROLE_OWNER_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/UpdateRoleOwnerRequest.json";
    private static final String FILE_SK_UPDATE_ROLE_DESCRIPTION_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/UpdateRoleDescriptionRequest.json";
    private static final String FILE_SK_ADD_ROLE_PERM_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/AddRolePermissionRequest.json";
    private static final String FILE_SK_ADD_CHILD_ROLE_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/AddChildRoleRequest.json";
    private static final String FILE_SK_REMOVE_ROLE_PERM_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/RemoveRolePermissionRequest.json";
    private static final String FILE_SK_REMOVE_PERM_FROM_ALL_ROLES_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/RemovePermissionFromAllRolesRequest.json";
    private static final String FILE_SK_REMOVE_CHILD_ROLE_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/RemoveChildRoleRequest.json";
    private static final String FILE_SK_PREVIEW_PATH_PREFIX_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/PreviewPathPrefixRequest.json";
    private static final String FILE_SK_REPLACE_PATH_PREFIX_REQUEST = 
            "/edu/utexas/tacc/tapis/security/api/jsonschema/ReplacePathPrefixRequest.json";

    /* **************************************************************************** */
    /*                                    Fields                                    */
    /* **************************************************************************** */
    /* Jax-RS context dependency injection allows implementations of these abstract
     * types to be injected (ch 9, jax-rs 2.0):
     * 
     *      javax.ws.rs.container.ResourceContext
     *      javax.ws.rs.core.Application
     *      javax.ws.rs.core.HttpHeaders
     *      javax.ws.rs.core.Request
     *      javax.ws.rs.core.SecurityContext
     *      javax.ws.rs.core.UriInfo
     *      javax.ws.rs.core.Configuration
     *      javax.ws.rs.ext.Providers
     * 
     * In a servlet environment, Jersey context dependency injection can also 
     * initialize these concrete types (ch 3.6, jersey spec):
     * 
     *      javax.servlet.HttpServletRequest
     *      javax.servlet.HttpServletResponse
     *      javax.servlet.ServletConfig
     *      javax.servlet.ServletContext
     *
     * Inject takes place after constructor invocation, so fields initialized in this
     * way can not be accessed in constructors.
     */ 
     @Context
     private HttpHeaders        _httpHeaders;
  
     @Context
     private Application        _application;
  
     @Context
     private UriInfo            _uriInfo;
  
     @Context
     private SecurityContext    _securityContext;
  
     @Context
     private ServletContext     _servletContext;
  
     @Context
     private HttpServletRequest _request;
    
     /* **************************************************************************** */
     /*                                Public Methods                                */
     /* **************************************************************************** */
     /* ---------------------------------------------------------------------------- */
     /* getRoleNames:                                                                */
     /* ---------------------------------------------------------------------------- */
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Get the names of all roles in the tenant in alphabetic order.  "
                     + "Future enhancements will include search filtering.\n\n"
                     + ""
                     + "A valid tenant must be specified as a query parameter.  "
                     + "This request is authorized if the requestor is a user that has "
                     + "access to the specified tenant or if the requestor is a service."
                     + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             responses = 
                 {@ApiResponse(responseCode = "200", description = "List of role names returned.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespNameArray.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response getRoleNames(@QueryParam("tenant") String tenant,
                                  @DefaultValue("false") @QueryParam("pretty") boolean prettyPrint)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "getRoleNames", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         if (StringUtils.isBlank(tenant)) {
             String msg = MsgUtils.getMsg("SK_MISSING_PARAMETER", "tenant");
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
                     entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }

         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(tenant, null).check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Create the role.
         List<String> list = null;
         try {
             list = getRoleImpl().getRoleNames(tenant);
         } catch (Exception e) {
             String msg = MsgUtils.getMsg("SK_ROLE_GET_NAMES_ERROR", tenant, 
                                          TapisThreadLocal.tapisThreadContext.get().getJwtUser());
             return getExceptionResponse(e, msg, prettyPrint);
         }
         
         // Assign result.
         ResultNameArray names = new ResultNameArray();
         names.names = list.toArray(new String[list.size()]);
         RespNameArray r = new RespNameArray(names);

         // ---------------------------- Success ------------------------------- 
         // Success means we found the tenant's role names.
         int cnt = names.names.length;
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_FOUND", "Roles", cnt + " items"), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* getRoleByName:                                                               */
     /* ---------------------------------------------------------------------------- */
     @GET
     @Path("/{roleName}")
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
         description = "Get the named role's definition.  A valid tenant must be "
                       + "specified as a query parameter.  This request is authorized "
                       + "if the requestor is a user that has access to the specified "
                       + "tenant or if the requestor is a service."
                       + "",
         tags = "role",
         security = {@SecurityRequirement(name = "TapisJWT")},
         responses = 
             {@ApiResponse(responseCode = "200", description = "Named role returned.",
               content = @Content(schema = @Schema(
                   implementation = edu.utexas.tacc.tapis.security.api.responses.RespRole.class))),
              @ApiResponse(responseCode = "400", description = "Input error.",
               content = @Content(schema = @Schema(
                  implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
              @ApiResponse(responseCode = "401", description = "Not authorized.",
               content = @Content(schema = @Schema(
                  implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
              @ApiResponse(responseCode = "404", description = "Named role not found.",
                content = @Content(schema = @Schema(
                   implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
              @ApiResponse(responseCode = "500", description = "Server error.",
                content = @Content(schema = @Schema(
                   implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
     )
     public Response getRoleByName(@PathParam("roleName") String roleName,
                                   @QueryParam("tenant") String tenant,
                                   @DefaultValue("false") @QueryParam("pretty") boolean prettyPrint)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "getRoleByName", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         if (StringUtils.isBlank(tenant)) {
             String msg = MsgUtils.getMsg("SK_MISSING_PARAMETER", "tenant");
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
                     entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }

         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(tenant, null).check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Get the role.
         SkRole role = null;
         try {
             role = getRoleImpl().getRoleByName(tenant, roleName);
         } catch (Exception e) {
             String msg = MsgUtils.getMsg("SK_ROLE_GET_ERROR", tenant,
                                          TapisThreadLocal.tapisThreadContext.get().getJwtUser(), 
                                          roleName);
             return getExceptionResponse(e, msg, prettyPrint);
         }

         // Adjust status based on whether we found the role.
         if (role == null) {
             ResultName missingName = new ResultName();
             missingName.name = roleName;
             RespName r = new RespName(missingName);
             return Response.status(Status.NOT_FOUND).entity(TapisRestUtils.createSuccessResponse(
                 MsgUtils.getMsg("TAPIS_NOT_FOUND", "Role", roleName), prettyPrint, r)).build();
         }
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         RespRole r = new RespRole(role);
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_FOUND", "Role", roleName), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* createRole:                                                                  */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Create a role using a request body.  "
                           + "Role names are case sensitive, alpha-numeric "
                           + "strings that can also contain underscores.  Role names must "
                           + "start with an alphbetic character and can be no more than 58 "
                           + "characters in length.  The desciption can be no more than "
                           + "2048 characters long.  If the role already exists, this "
                           + "request has no effect.\n\n"
                           + ""
                           + "For the request to be authorized, the requestor must be "
                           + "either an administrator or a service allowed to perform "
                           + "updates in the new role's tenant."
                           + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqCreateRole.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Role existed.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespResourceUrl.class))),
                  @ApiResponse(responseCode = "201", description = "Role created.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespResourceUrl.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response createRole(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "createRole", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqCreateRole payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_CREATE_ROLE_REQUEST, 
                                   ReqCreateRole.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "createRole", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String roleTenant  = payload.roleTenant;
         String roleName    = payload.roleName;
         String description = payload.description;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(roleTenant, null)
                             .setCheckIsService()
                             .setCheckIsAdmin()
                             .setPreventForeignTenantUpdate()
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // The threadlocal object has been validated by now.
         String owner = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
         String ownerTenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();

         // Create the role.
         int rows = 0;
         try {rows = getRoleImpl().createRole(roleName, roleTenant, description, owner, ownerTenant);}
         catch (Exception e) {
             String msg = MsgUtils.getMsg("SK_ROLE_CREATE_ERROR", roleName, roleTenant, owner, ownerTenant);
             return getExceptionResponse(e, msg, prettyPrint);
         }
         
         // NOTE: We need to assign a location header as well.
         //       See https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5.
         ResultResourceUrl respUrl = new ResultResourceUrl();
         respUrl.url = SKApiUtils.constructTenantURL(roleTenant, _request.getRequestURI(), roleName);
         RespResourceUrl r = new RespResourceUrl(respUrl);
         
         // ---------------------------- Success ------------------------------- 
         // No new rows means the role exists. 
         if (rows == 0)
             return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
                 MsgUtils.getMsg("TAPIS_EXISTED", "Role", roleName+"@"+roleTenant), prettyPrint, r)).build();
         else 
             return Response.status(Status.CREATED).entity(TapisRestUtils.createSuccessResponse(
                 MsgUtils.getMsg("TAPIS_CREATED", "Role", roleName+"@"+roleTenant), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* deleteRoleByName:                                                            */
     /* ---------------------------------------------------------------------------- */
     @DELETE
     @Path("/{roleName}")
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
         description = "Delete the named role. A valid tenant and user must be "
                       + "specified as query parameters.\n\n"
                       + ""
                       + "This request is authorized only if the authenticated user is either the "
                       + "role owner or an administrator."
                       + "",
         tags = "role",
         security = {@SecurityRequirement(name = "TapisJWT")},
         responses = 
             {@ApiResponse(responseCode = "200", description = "Role deleted.",
                 content = @Content(schema = @Schema(
                     implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount.class))),
              @ApiResponse(responseCode = "400", description = "Input error.",
                 content = @Content(schema = @Schema(
                     implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
              @ApiResponse(responseCode = "401", description = "Not authorized.",
                 content = @Content(schema = @Schema(
                     implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
              @ApiResponse(responseCode = "500", description = "Server error.",
                 content = @Content(schema = @Schema(
                     implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
     )
     public Response deleteRoleByName(@PathParam("roleName") String roleName,
                                      @QueryParam("tenant") String tenant,
                                      @DefaultValue("false") @QueryParam("pretty") boolean prettyPrint)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "deleteRoleByName", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         if (StringUtils.isBlank(tenant)) {
             String msg = MsgUtils.getMsg("SK_MISSING_PARAMETER", "tenant");
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
                     entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(tenant, null)
                             .setCheckIsAdmin()
                             .addOwnedRole(roleName)
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Delete the role.
         int rows = 0;
         try {rows =  getRoleImpl().deleteRoleByName(tenant, roleName);}
         catch (Exception e) {
        	 // The threadlocal value has been validated.
             String msg = MsgUtils.getMsg("SK_ROLE_DELETE_ERROR", tenant, 
            		                      TapisThreadLocal.tapisThreadContext.get().getJwtUser(), 
            		                      roleName);
             return getExceptionResponse(e, msg, prettyPrint);
         }
         
         // Return the number of row affected.
         ResultChangeCount count = new ResultChangeCount();
         count.changes = rows;
         RespChangeCount r = new RespChangeCount(count);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we deleted the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_DELETED", "Role", roleName), prettyPrint, r)).build();
     }
     
     /* ---------------------------------------------------------------------------- */
     /* getRolePermissions:                                                          */
     /* ---------------------------------------------------------------------------- */
     @GET
     @Path("/{roleName}/perms")
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
         description = "Get the named role's permissions.  By default, all permissions "
                 + "assigned to the role, whether directly and transitively through "
                 + "child roles, are returned.  Set the immediate query parameter to "
                 + "only retrieve permissions directly assigned to the role.  A valid "
                 + "tenant must be specified.\n\n"
                 + ""
                 + "This request is authorized if the requestor is a user that has "
                 + "access to the specified tenant or if the requestor is a service."
                 + "",
         tags = "role",
         security = {@SecurityRequirement(name = "TapisJWT")},
         responses = 
             {@ApiResponse(responseCode = "200", description = "Named role returned.",
               content = @Content(schema = @Schema(
                   implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespNameArray.class))),
              @ApiResponse(responseCode = "400", description = "Input error.",
               content = @Content(schema = @Schema(
                  implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
              @ApiResponse(responseCode = "401", description = "Not authorized.",
               content = @Content(schema = @Schema(
                  implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
              @ApiResponse(responseCode = "404", description = "Named role not found.",
                content = @Content(schema = @Schema(
                   implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
              @ApiResponse(responseCode = "500", description = "Server error.",
                content = @Content(schema = @Schema(
                   implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
     )
     public Response getRolePermissions(@PathParam("roleName") String roleName,
                                        @QueryParam("tenant") String tenant,
                                        @DefaultValue("false") @QueryParam("immediate") boolean immediate,
                                        @DefaultValue("false") @QueryParam("pretty") boolean prettyPrint)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "getRolePermissions", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         if (StringUtils.isBlank(tenant)) {
             String msg = MsgUtils.getMsg("SK_MISSING_PARAMETER", "tenant");
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
                     entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(tenant, null).check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Create the role.
         List<String> list = null;
         try {
             list = getRoleImpl().getRolePermissions(tenant, roleName, immediate);
         } catch (Exception e) {
             String msg = MsgUtils.getMsg("SK_ROLE_GET_PERMISSIONS_ERROR",tenant, 
                                          TapisThreadLocal.tapisThreadContext.get().getJwtUser(), 
                                          roleName);
             return getExceptionResponse(e, msg, prettyPrint);
         }

         // Assign result.
         ResultNameArray names = new ResultNameArray();
         names.names = list.toArray(new String[list.size()]);
         RespNameArray r = new RespNameArray(names);

         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         int cnt = names.names.length;
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_FOUND", "Permissions", cnt + " permissions"), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* updateRoleName:                                                              */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/updateName/{roleName}")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Update an existing role's name using a request body.  "
                           + "Role names are case sensitive, alphanumeric strings "
                           + "that can contain underscores but must begin with an alphabetic "
                           + "character.  The limit on role name is 58 characters.\n\n"
                           + ""
                           + "This request is authorized if the requestor is the role owner "
                           + "or an administrator."
                           + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqUpdateRoleName.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Role name updated.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response updateRoleName(@PathParam("roleName") String roleName,
                                    @DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                    InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "updateRoleName", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Make sure the existing role name is not reserved.
         if (!SKApiUtils.isValidName(roleName)) {
             String msg = MsgUtils.getMsg("TAPIS_INVALID_PARAMETER", "updateRoleName", "roleName",
                                          roleName);
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
         
         // Parse and validate the json in the request payload, which must exist.
         ReqUpdateRoleName payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_UPDATE_ROLE_NAME_REQUEST, 
                                   ReqUpdateRoleName.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "updateRoleName", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String roleTenant  = payload.roleTenant;
         String newRoleName = payload.newRoleName;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(roleTenant, null)
                             .setCheckIsAdmin()
                             .addOwnedRole(roleName)
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // The threadlocal object has been validated by now.
         String requestor = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
         String requestorTenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
         
         // Create the role.
         int rows = 0;
         try {
             rows = getRoleImpl().updateRoleName(roleTenant, roleName, newRoleName, 
            		                             requestor, requestorTenant);
         } catch (Exception e) {
             String msg = MsgUtils.getMsg("SK_ROLE_UPDATE_ERROR", roleTenant, roleName, 
            		                      requestor, requestorTenant);
             return getExceptionResponse(e, msg, prettyPrint, "Role");
         }

         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Role", roleName), prettyPrint)).build();
     }
     
     /* ---------------------------------------------------------------------------- */
     /* updateRoleOwner:                                                             */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/updateOwner/{roleName}")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Update an existing role's owner using a request body. "
             			   + "Required parameters in the payload are the *roleTenant*, "
             			   + "which is the tenant of named role, and *newOwner*, which "
             			   + "is the user to which role ownership is being transferred. "
             			   + "The *newTenant* payload parameter is optional and only "
             			   + "needed when the new owner resides in a different tenant "
             			   + "than that of the current owner.\n\n"
                           + ""
                           + "This request is authorized if the requestor is the role owner "
                           + "or an administrator. If a new tenant is specified, then the "
                           + "requestor must also be allowed to act in the new tenant."
                           + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqUpdateRoleOwner.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Role owner updated.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response updateRoleOwner(@PathParam("roleName") String roleName,
                                     @DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                     InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "updateRoleOwner", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Make sure the existing role name is not reserved.
         if (!SKApiUtils.isValidName(roleName)) {
             String msg = MsgUtils.getMsg("TAPIS_INVALID_PARAMETER", "updateRoleName", "roleName",
                                          roleName);
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
         
         // Parse and validate the json in the request payload, which must exist.
         ReqUpdateRoleOwner payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_UPDATE_ROLE_OWNER_REQUEST, 
                                   ReqUpdateRoleOwner.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "updateRoleOwner", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String roleTenant = payload.roleTenant;
         String newOwner   = payload.newOwner;
         String newTenant  = payload.newTenant; // optional, can be null or empty
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(roleTenant, null)
                             .setCheckIsAdmin()
                             .addOwnedRole(roleName)
                             .setPreventInvalidOwnerAssignment(newTenant)
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // The threadlocal object has been validated by now.
         String requestor = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
         String requestorTenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
         
         // Create the role.
         int rows = 0;
         try {
        	 // The new tenant can be null.
             rows = getRoleImpl().updateRoleOwner(roleTenant, roleName, newOwner, newTenant,
            		                              requestor, requestorTenant);
         } catch (Exception e) {
             String msg = MsgUtils.getMsg("SK_ROLE_UPDATE_ERROR", roleTenant, roleName, 
                                          requestor, requestorTenant);
             return getExceptionResponse(e, msg, prettyPrint, "Role");
         }
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Role", roleName), prettyPrint)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* updateRoleDescription:                                                       */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/updateDesc/{roleName}")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Update an existing role's decription using a request body.  "
                           + "The size limit on a description is 2048 characters.\n\n"
                           + ""
                           + "This request is authorized if the requestor is the role owner "
                           + "or an administrator."
                           + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqUpdateRoleDescription.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Role description updated.",
                      content = @Content(schema = @Schema(
                          implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                      content = @Content(schema = @Schema(
                          implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                      content = @Content(schema = @Schema(
                          implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                      content = @Content(schema = @Schema(
                          implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                      content = @Content(schema = @Schema(
                          implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response updateRoleDescription(
                                @PathParam("roleName") String roleName,
                                @DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "updateRoleDescription", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Make sure the existing role name is not reserved.
         if (!SKApiUtils.isValidName(roleName)) {
             String msg = MsgUtils.getMsg("TAPIS_INVALID_PARAMETER", "updateRoleName", "roleName",
                                          roleName);
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
         
         // Parse and validate the json in the request payload, which must exist.
         ReqUpdateRoleDescription payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_UPDATE_ROLE_DESCRIPTION_REQUEST, 
                                   ReqUpdateRoleDescription.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "updateRoleName", e.getMessage());
              _log.error(msg, e);
              return Response.status(Status.BAD_REQUEST).
                entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String roleTenant     = payload.roleTenant;
         String newDescription = payload.newDescription;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(roleTenant, null)
                             .setCheckIsAdmin()
                             .addOwnedRole(roleName)
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // The threadlocal object has been validated by now.
         String requestor = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
         String requestorTenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
         
         // Create the role.
         int rows = 0;
         try {
             rows = getRoleImpl().updateRoleDescription(roleTenant, roleName, newDescription,
            		                                    requestor, requestorTenant);
         } catch (Exception e) {
             String msg = MsgUtils.getMsg("SK_ROLE_UPDATE_ERROR", roleTenant, roleName, 
                                          requestor, requestorTenant);
             return getExceptionResponse(e, msg, prettyPrint, "Role");
         }
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Role", roleName), prettyPrint)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* addRolePermission:                                                           */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/addPerm")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Add a permission to an existing role using a request body.  "
                         + "If the permission already exists, "
                         + "then the request has no effect and the change count returned is "
                         + "zero. Otherwise, the permission is added and the change count is one.\n\n"
                         + ""
                         + "Permissions are case-sensitive strings that follow the format "
                         + "defined by Apache Shiro (https://shiro.apache.org/permissions.html).  "
                         + "This format defines any number of colon-separated (:) parts, with the "
                         + "possible use of asterisks (*) as wildcards and commas (,) as "
                         + "aggregators.  Here are two example permission strings:\n\n"
                         + ""
                         + "    system:MyTenant:read,write:system1\n"
                         + "    system:MyTenant:create,read,write,delete:*\n\n"
                         + ""
                         + "See the Shiro documentation for further details.  Note that the three "
                         + "reserved characters, [: * ,], cannot appear in the text of any part.  "
                         + "It's the application's responsibility to escape those characters in "
                         + "a manner that is safe in the application's domain.\n\n"
                         + ""
                         + "### Extended Permissions\n"
                         + ""
                         + "Tapis extends Shiro permission checking with *path semantics*.  Path "
                         + "semantics allows the last part of pre-configured permissions to be "
                         + "treated as hierarchical path names, such as the paths used in POSIX file "
                         + "systems.  Currently, only permissions that start with *files:* have their "
                         + "last (5th) component configured with path semantics.\n\n"
                         + ""
                         + "Path semantics treat the extended permission part "
                         + "as the root of the subtree to which the permission is applied "
                         + "recursively.  Grantees assigned the permission will "
                         + "have the permission on the path itself and on all its children.\n\n"
                         + ""
                         + "As an example, consider a role that's assigned the following permission:\n\n"
                         + ""
                         + "    files:iplantc.org:read:stampede2:/home/bud\n\n"
                         + ""
                         + "Users granted the role have read permission on the following file "
                         + "system resources on stampede2:\n\n"
                         + ""
                         + "    /home/bud\n"
                         + "    /home/bud/\n"
                         + "    /home/bud/myfile\n"
                         + "    /home/bud/mydir/myfile\n\n"
                         + ""
                         + "Those users, however, will not have access to /home.\n\n"
                         + ""
                         + "When an extended permission part ends with a slash, such as /home/bud/, "
                         + "then that part is interpreted as a directory or, more generally, some type of "
                         + "container.  In such cases, the permission applies to the children of the path "
                         + "and to the path as written with a slash.  For instance, for the file permission "
                         + "path /home/bud/, the permission allows access to /home/bud/ and /home/bud/myfile, "
                         + "but not to /home/bud.\n\n"
                         + ""
                         + "When an extended permission part does not end with a slash, such as /home/bud, "
                         + "then the permission applies to the children of the path and to the path written "
                         + "with or without a trailing slash.  For instance, for the file permission path "
                         + "/home/bud, the permission allows access to /home/bud, /home/bud/ and "
                         + "/home/bud/myfile.\n\n"
                         + ""
                         + "In the previous examples, we assumed /home/bud was a directory.  If /home/bud is a "
                         + "file (or more generally a leaf), then specifying the permission path /home/bud/ "
                         + "will not work as intended.  Permissions with paths that have trailing slashes "
                         + "should only be used for directories, and they require a trailing slash "
                         + "whenever refering to the root directory.  Permissions that don't have a trailing "
                         + "slash can represent directories or files, and thus are more general.\n\n"
                         + ""
                         + "Extended permission checking avoids *false capture*.  Whether a path has a "
                         + "trailing slash or not, "
                         + "permission checking will not capture similarly named sibling paths. For example, "
                         + "using the file permission path /home/bud, grantees are allowed access to "
                         + "/home/bud and all its children (if it's a directory), but not to the file "
                         + "/home/buddy.txt nor the directory /home/bud2.\n\n"
                         + ""
                         + "This request is authorized only if the authenticated user is either the "
                         + "role owner or an administrator."
                         + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqAddRolePermission.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Permission assigned to role.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response addRolePermission(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                       InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "addRolePermission", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqAddRolePermission payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_ADD_ROLE_PERM_REQUEST, 
                                   ReqAddRolePermission.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "addRolePermission", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
                entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String roleTenant = payload.roleTenant;
         String roleName   = payload.roleName;
         String permSpec   = payload.permSpec;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(roleTenant, null)
                             .setCheckIsAdmin()
                             .addOwnedRole(roleName)
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // The threadlocal object has been validated by now.
         String requestor = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
         String requestorTenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
         
         // Add permission to role.
         int rows = 0;
         try {
             rows = getRoleImpl().addRolePermission(roleTenant, roleName, permSpec, requestor, requestorTenant);
         } catch (Exception e) {
             // This only occurs when the role name is not found.
             String msg = MsgUtils.getMsg("SK_ADD_PERMISSION_ERROR", requestor, requestorTenant, permSpec, 
            		                      roleName, roleTenant);
             return getExceptionResponse(e, msg, prettyPrint, "Role", roleName);
         }

         // Report the number of rows changed.
         ResultChangeCount count = new ResultChangeCount();
         count.changes = rows;
         RespChangeCount r = new RespChangeCount(count);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Role", roleName), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* removeRolePermission:                                                        */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/removePerm")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Remove a permission from a role using a request body.  "
                     + "A valid role, roleTenant and permission must be specified in "
                     + "the request body.\n\n"
                     + ""
                     + "Only the role owner or administrators are authorized to make this call."
                     + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqRemoveRolePermission.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Permission removed from role.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response removeRolePermission(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                          InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "removeRolePermission", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqRemoveRolePermission payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_REMOVE_ROLE_PERM_REQUEST, 
                                   ReqRemoveRolePermission.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "removeRolePermission", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String roleTenant = payload.roleTenant;
         String roleName   = payload.roleName;
         String permSpec   = payload.permSpec;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(roleTenant, null)
                             .setCheckIsAdmin()
                             .addOwnedRole(roleName)
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Remove the permission from the role.
         int rows = 0;
         try {rows = getRoleImpl().removeRolePermission(roleTenant, roleName, permSpec);} 
         catch (Exception e) {
             // Role not found is an error in this case.
             String requestor = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
             String requestorTenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
             String msg = MsgUtils.getMsg("SK_REMOVE_PERMISSION_ERROR", requestor,
            		                      requestorTenant, permSpec, roleName, roleTenant);
             return getExceptionResponse(e, msg, prettyPrint, "Role", roleName);
         }

         // Report the number of rows changed.
         ResultChangeCount count = new ResultChangeCount();
         count.changes = rows;
         RespChangeCount r = new RespChangeCount(count);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Role", roleName), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* addChildRole:                                                                */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/addChild")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Add a child role to another role using a request body.  "
                         + "If the child already exists, "
                         + "then the request has no effect and the change count returned is "
                         + "zero. Otherwise, the child is added and the change count is one.\n\n"
                         + ""
                         + "The user@tenant identity specified in JWT is authorized to make "
                         + "this request only if that user is an administrator or if the user "
                         + "owns both the parent and child roles."
                         + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqAddChildRole.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Child assigned to parent role.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response addChildRole(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                  InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "addChildRole", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqAddChildRole payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_ADD_CHILD_ROLE_REQUEST, 
                                   ReqAddChildRole.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "addChildRole", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String roleTenant     = payload.roleTenant; 
         String parentRoleName = payload.parentRoleName;
         String childRoleName  = payload.childRoleName;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(roleTenant, null)
                             .setCheckIsAdmin()
                             .addOwnedRole(parentRoleName)
                             .addOwnedRole(childRoleName)
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // The requestor will always be non-null after the above check. 
         String user = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
         String tenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
         
         // Add the child role to the parent.
         int rows = 0;
         try {
             rows = getRoleImpl().addChildRole(tenant, user, roleTenant, parentRoleName, childRoleName);
         } catch (Exception e) {
             String msg = MsgUtils.getMsg("SK_ADD_CHILD_ROLE_ERROR", tenant, user, 
            		                      childRoleName, parentRoleName, roleTenant);
             return getExceptionResponse(e, msg, prettyPrint, "Role");
         }

         // Report the number of rows changed.
         ResultChangeCount count = new ResultChangeCount();
         count.changes = rows;
         RespChangeCount r = new RespChangeCount(count);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Role", parentRoleName), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* removeChildRole:                                                             */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/removeChild")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Remove a child role from a parent role using a request body.  "
                     + "A valid tenant and user must be specified in the request body.\n\n"
                     + ""
                     + "The user@tenant identity specified in JWT is authorized to make "
                     + "this request only if that user is an administrator or if they own "
                     + "the parent role."
                     + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqRemoveChildRole.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Child removed from parent role.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response removeChildRole(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                     InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "removeChildRole", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqRemoveChildRole payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_REMOVE_CHILD_ROLE_REQUEST, 
                                   ReqRemoveChildRole.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "removeChildRole", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String roleTenant     = payload.roleTenant;
         String parentRoleName = payload.parentRoleName;
         String childRoleName  = payload.childRoleName;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(roleTenant, null)
                             .setCheckIsAdmin()
                             .addOwnedRole(parentRoleName)
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Create the role.
         int rows = 0;
         try {
             rows = getRoleImpl().removeChildRole(roleTenant, parentRoleName, childRoleName);
         } catch (Exception e) {
             String user = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
             String tenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
             String msg = MsgUtils.getMsg("SK_DELETE_CHILD_ROLE_ERROR", 
                                          tenant, user, childRoleName, parentRoleName);
             return getExceptionResponse(e, msg, prettyPrint, "Role");
         }

         // Report the number of rows changed.
         ResultChangeCount count = new ResultChangeCount();
         count.changes = rows;
         RespChangeCount r = new RespChangeCount(count);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Role", parentRoleName), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* previewPathPrefix:                                                           */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/previewPathPrefix")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "This read-only endpoint previews the transformations that would take "
                         + "place if the same input was used on a replacePathPrefix POST call. "
                         + "This call is also implemented as a POST so that the same input "
                         + "as used on replacePathPrefix can be used here, but this call changes "
                         + "nothing.\n\n"
                         + ""
                         + "This endpoint can be used to get an accounting of existing "
                         + "system/path combinations that match the input specification. "
                         + "Such information is useful when trying to duplicate a set of "
                         + "permissions. For example, one may want to copy a file subtree to "
                         + "another location and assign the same permissions to the new subtree "
                         + "as currently exist on the original subtree. One could use  "
                         + "this call to calculate the users that should be granted "
                         + "permission on the new subtree.\n\n"
                         + ""
                         + "The optional parameters are roleName, oldPrefix and newPrefix. "
                         + "No wildcards are defined for the path prefix parameters.  When "
                         + "roleName is specified then only permissions assigned to that role are "
                         + "considered.\n\n"
                         + ""
                         + "When the oldPrefix parameter is provided, it's used to filter out "
                         + "permissions whose paths do not begin with the specified string; when not "
                         + "provided, no path prefix filtering occurs.\n\n"
                         + ""
                         + "When the newPrefix parameter is not provided no new characters are "
                         + "prepended to the new path, effectively just removing the oldPrefix "
                         + "from the new path. "
                         + "When neither oldPrefix nor newPrefix are provided, no path transformation "
                         + "occurs, though system IDs can still be transformed.\n\n"
                         + ""
                         + "The result object contains an array of transformation objects, each of "
                         + "which contains the unique permission sequence number, the existing "
                         + "permission that matched the search criteria and the new permission if "
                         + "the specified transformations were applied.\n\n"
                         + ""
                         + "A valid tenant and user must be specified in the request body.  "
                         + "This request is authorized if the requestor is a user that has "
                         + "access to the specified tenant or if the requestor is a service."
                         + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqPreviewPathPrefix.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Path prefixes previewed.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.responses.RespPathPrefixes.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response previewPathPrefix(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                       InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "previewPathPrefix", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqPreviewPathPrefix payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_PREVIEW_PATH_PREFIX_REQUEST, 
                                   ReqPreviewPathPrefix.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "previewPathPrefix", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
                entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String tenant = payload.tenant;
         String schema = payload.schema;
         String roleName = payload.roleName;
         String oldSystemId = payload.oldSystemId;
         String newSystemId = payload.newSystemId;
         String oldPrefix = payload.oldPrefix;
         String newPrefix = payload.newPrefix;
         
         // Canonicalize blank prefix values.
         if (StringUtils.isBlank(oldPrefix)) oldPrefix = "";
         if (StringUtils.isBlank(newPrefix)) newPrefix = "";
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(tenant, null).check(prettyPrint);
         if (resp != null) return resp;
         
        // ------------------------ Request Processing ------------------------
         // Get the list of transformations that would be appled by replacePathPrefix.
         List<Transformation> transList = null;
         try {
                 transList = getRoleImpl().previewPathPrefix(schema, roleName, 
                                                             oldSystemId, newSystemId, 
                                                             oldPrefix, newPrefix, 
                                                             tenant);
             }
             catch (Exception e) {
                 String msg = MsgUtils.getMsg("SK_PERM_TRANSFORM_FAILED", schema, roleName,
                                              oldSystemId, oldPrefix, newSystemId, newPrefix,
                                              tenant);
                 _log.error(msg);
                 return Response.status(Status.BAD_REQUEST).
                         entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
             }
         
         // Create the result object with a properly sized transformation array.
         var transArray = new Transformation[transList.size()];
         transArray = transList.toArray(transArray);
         RespPathPrefixes pathPrefixes = new RespPathPrefixes(transArray);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we calculated zero or more transformations. 
         String s = oldSystemId + ":" + oldPrefix;
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_READ", "Permission", s), prettyPrint, pathPrefixes)).build();
     }
     
     /* ---------------------------------------------------------------------------- */
     /* replacePathPrefix:                                                           */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/replacePathPrefix")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Replace the text in a permission specification when its last component "
                         + "defines an *extended path attribute*.  Extended path attributes "
                         + "enhance the standard Shiro matching algorithm with one that treats "
                         + "designated components in a permission specification as a path name, "
                         + "such as a posix file or directory path name.  This request is useful "
                         + "when files or directories have been renamed or moved and their "
                         + "authorizations need to be adjusted.  Consider, for example, "
                         + "permissions that conform to the following specification:\n\n"
                         + ""
                         + "      files:tenantId:op:systemId:path\n\n"
                         + ""
                         + "By definition, the last component is an extended path attribute whose "
                         + "content can be changed by replacePathPrefix requests.  Specifically, paths "
                         + "that begin with the oldPrefix will have that prefix replaced with "
                         + "the newPrefix value.  Replacement only occurs on permissions "
                         + "that also match the schema and oldSystemId parameter values.  The systemId "
                         + "attribute is required to immediately precede the path attribute, which "
                         + "must be the last attribute.\n\n"
                         + ""
                         + "Additionally, the oldSystemId is replaced with the newSystemId "
                         + "when a match is found.  If a roleName is provided, then replacement is "
                         + "limited to permissions defined only in that role.  Otherwise, permissions "
                         + "in all roles that meet the other matching criteria will be considered.\n\n"
                         + ""
                         + "The optional parameters are roleName, oldPrefix and newPrefix. "
                         + "No wildcards are defined for the path prefix parameters.  When "
                         + "roleName is specified then only permissions assigned to that role are "
                         + "considered.\n\n"
                         + ""
                         + "When the oldPrefix parameter is provided, it's used to filter out "
                         + "permissions whose paths do not begin with the specified string; when not "
                         + "provided, no path prefix filtering occurs.\n\n"
                         + ""
                         + "When the newPrefix parameter is not provided no new characters are "
                         + "prepended to the new path, effectively just removing the oldPrefix "
                         + "from the new path. "
                         + "When neither oldPrefix nor newPrefix are provided, no path transformation "
                         + "occurs, though system IDs can still be transformed.\n\n"
                         + ""
                         + "The previewPathPrefix request provides a way to do a dry run using the "
                         + "same input as this request. The preview call calculates the permissions "
                         + "that would change and what their new values would be, but it does not "
                         + "actually change those permissions as replacePathPrefix does.\n\n"
                         + ""
                         + "The input parameters are passed in the payload of this request.  "
                         + "The response indicates the number of changed permission "
                         + "specifications.\n\n"
                         + ""
                         + "The path prefix replacement operation is authorized if "
                         + "the user@tenant in the JWT represents a tenant administrator or "
                         + "the Files service."
                         + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqReplacePathPrefix.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Path prefixes replaced.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "404", description = "Named role not found.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response replacePathPrefix(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                       InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "replacePathPrefix", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqReplacePathPrefix payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_REPLACE_PATH_PREFIX_REQUEST, 
                                   ReqReplacePathPrefix.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "replacePathPrefix", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
                entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String tenant = payload.tenant;
         String schema = payload.schema;
         String roleName = payload.roleName;
         String oldSystemId = payload.oldSystemId;
         String newSystemId = payload.newSystemId;
         String oldPrefix = payload.oldPrefix;
         String newPrefix = payload.newPrefix;
         
         // Canonicalize blank prefix values.
         if (StringUtils.isBlank(oldPrefix)) oldPrefix = "";
         if (StringUtils.isBlank(newPrefix)) newPrefix = "";
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(tenant, null)
                             .setCheckIsAdmin()
                             .setCheckIsFilesService()
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Calculate the permissions that need to change and apply changes.
         int rows = 0;
         try {
                 rows = getRoleImpl().replacePathPrefix(schema, roleName, 
                                                        oldSystemId, newSystemId, 
                                                        oldPrefix, newPrefix, 
                                                        tenant);
             }
             catch (Exception e) {
                 String msg = MsgUtils.getMsg("SK_PERM_UPDATE_FAILED", schema, roleName,
                                              oldSystemId, oldPrefix, newSystemId, newPrefix,
                                              tenant, e.getMessage());
                 _log.error(msg);
                 return Response.status(Status.BAD_REQUEST).
                         entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
             }
         
         // ---------------------------- Success ------------------------------- 
         // Success means we updated zero or more permissions. 
         ResultChangeCount count = new ResultChangeCount();
         count.changes = rows;
         RespChangeCount r = new RespChangeCount(count);
         String s = oldSystemId + ":" + oldPrefix;
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Permission", s), prettyPrint, r)).build();
     }
     
     /* ---------------------------------------------------------------------------- */
     /* getDefaultUserRole:                                                          */
     /* ---------------------------------------------------------------------------- */
     @GET
     @Path("/defaultRole/{user}")
     @Produces(MediaType.APPLICATION_JSON)
     @PermitAll
     @Operation(
             description = 
               "Get a user's default role. The default role is implicitly created by the system "
               + "when needed if it doesn't already exist. No authorization required.\n\n"
               + ""
               + "A user's default role is constructed by prepending '$$' to the "
               + "user's name.  This implies the maximum length of a user name is 58 since "
               + "role names are limited to 60 characters.\n\n"
               + "",
             tags = "role",
             responses = 
                 {@ApiResponse(responseCode = "200", description = "The user's default role name.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespName.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response getDefaultUserRole(@PathParam("user") String user,
                                        @DefaultValue("false") @QueryParam("pretty") boolean prettyPrint)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "getDefaultUserRole", _request.getRequestURL());
             _log.trace(msg);
         }

         // ------------------------- Input Processing -------------------------
         // Check input.
         if (StringUtils.isBlank(user)) {
             String msg = MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "getDefaultUserRole", "user");
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
                     entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
         if (user.length() > RoleImpl.MAX_USER_NAME_LEN) {
             String msg = MsgUtils.getMsg("SK_USER_NAME_LEN", "anyTenant", 
                                          user, RoleImpl.MAX_USER_NAME_LEN);
             _log.error(msg);
             return Response.status(Status.BAD_REQUEST).
                     entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
         
         // ------------------------ Request Processing ------------------------
         // Construct the role name.
         String name = null;
         try {name = getUserImpl().getUserDefaultRolename(user);}
         catch (Exception e) {
             return getExceptionResponse(e, null, prettyPrint);
         }
         
         // Fill in the response.
         ResultName dftName = new ResultName();
         dftName.name = name;
         RespName r = new RespName(dftName);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the tenant's role names.
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_FOUND", "Role", name), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* removePermissionFromAllRoles:                                                */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/removePermFromAllRoles")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Remove a permission from all roles in a tenant using a request body.  "
                     + "The tenant and permission must be specified in the request body.\n\n"
                     + ""
                     + "Each role in the tenant is searched for the *exact* permission string and, "
                     + "where found, that permission is removed.  The matching algorithm is simple, "
                     + "character by character, string comparison.\n\n"
                     + "Permissions are not interpreted.  For example, a "
                     + "permission that contains a wildcard (*) will only match a role's permission "
                     + "when the same wildcard is found in the exact same position.  The same rule "
                     + "applies to permission segments with multiple, comma separated components: "
                     + "a match requires the exact same ordering and spacing of components.\n\n"
                     + ""
                     + "Only services are authorized to make this call."
                     + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqRemovePermissionFromAllRoles.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Permission removed from roles.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response removePermissionFromAllRoles(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                                  InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "removePermissionFromAllRoles", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqRemovePermissionFromAllRoles payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_REMOVE_PERM_FROM_ALL_ROLES_REQUEST, 
                                   ReqRemovePermissionFromAllRoles.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "removeRolePermission", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String reqTenant = payload.tenant;
         String permSpec  = payload.permSpec;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(reqTenant, null)
                             .setCheckIsService()
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Remove the permission from the role.
         int rows = 0;
         try {rows = getRoleImpl().removePermissionFromRoles(reqTenant, permSpec);} 
         catch (Exception e) {
             // Role not found is an error in this case.
             String requestor = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
             String requestorTenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
             String msg = MsgUtils.getMsg("SK_REMOVE_PERMISSION_FROM_ROLES_ERROR", requestor,
                                          requestorTenant, permSpec, reqTenant, e.getMessage());
             return getExceptionResponse(e, msg, prettyPrint, "Permission", permSpec);
         }

         // Report the number of rows changed.
         ResultChangeCount count = new ResultChangeCount();
         count.changes = rows;
         RespChangeCount r = new RespChangeCount(count);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Permission", permSpec), prettyPrint, r)).build();
     }

     /* ---------------------------------------------------------------------------- */
     /* removePathPermissionFromAllRoles:                                             */
     /* ---------------------------------------------------------------------------- */
     @POST
     @Path("/removePathPermFromAllRoles")
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     @Operation(
             description = "Remove an extended permission from all roles in a tenant using a request body.  "
                     + "The tenant and permission must be specified in the request body.\n\n"
                     + ""
                     + "Each role in the tenant is searched for the extended permission string and, "
                     + "where found, that permission is removed.  The matching algorithm is string "
                     + "comparison with wildcard semantics on the path component.  This is the same "
                     + "as an exact string match for all parts of the permission specification up to "
                     + "the path part.  A match on the path part, however, occurs "
                     + "when its path is a prefix of a role permission's path.  Consider the following "
                     + "permission specification:\n\n"
                     + ""
                     + "    files:mytenant:read:mysystem:/my/dir\n\n"
                     + ""
                     + "which will match both of the following role permissions:\n\n"
                     + ""
                     + "    files:mytenant:read:mysystem:/my/dir/subdir/myfile\n"
                     + "    files:mytenant:read:mysystem:/my/dir33/yourfile\n\n"
                     + ""
                     + "Note that a match to the second role permission might be a *false capture* "
                     + "if the intension was to remove all permissions to resources in the /my/dir "
                     + "subtree, but not those in other directories.  To avoid this potential problem, "
                     + "callers can make two calls, one to this endpoint with a permSpec that ends "
                     + "with a slash (\"/\") and one to the removePermissionFromeAllRoles endpoint "
                     + "with no trailing slash.  The former removes all children from the directory "
                     + "subtree, the latter removes the directory itself.\n\n"
                     + ""
                     + "Only the Files service is authorized to make this call."
                     + "",
             tags = "role",
             security = {@SecurityRequirement(name = "TapisJWT")},
             requestBody = 
                 @RequestBody(
                     required = true,
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.security.api.requestBody.ReqRemovePermissionFromAllRoles.class))),
             responses = 
                 {@ApiResponse(responseCode = "200", description = "Path permission removed from roles.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespChangeCount.class))),
                  @ApiResponse(responseCode = "400", description = "Input error.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "401", description = "Not authorized.",
                     content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class))),
                  @ApiResponse(responseCode = "500", description = "Server error.",
                      content = @Content(schema = @Schema(
                         implementation = edu.utexas.tacc.tapis.sharedapi.responses.RespBasic.class)))}
         )
     public Response removePathPermissionFromAllRoles(@DefaultValue("false") @QueryParam("pretty") boolean prettyPrint,
                                                      InputStream payloadStream)
     {
         // Trace this request.
         if (_log.isTraceEnabled()) {
             String msg = MsgUtils.getMsg("TAPIS_TRACE_REQUEST", getClass().getSimpleName(), 
                                          "removePathPermissionFromAllRoles", _request.getRequestURL());
             _log.trace(msg);
         }
         
         // ------------------------- Input Processing -------------------------
         // Parse and validate the json in the request payload, which must exist.
         ReqRemovePermissionFromAllRoles payload = null;
         try {payload = getPayload(payloadStream, FILE_SK_REMOVE_PERM_FROM_ALL_ROLES_REQUEST, 
                                   ReqRemovePermissionFromAllRoles.class);
         } 
         catch (Exception e) {
             String msg = MsgUtils.getMsg("NET_REQUEST_PAYLOAD_ERROR", 
                                          "removeRolePermission", e.getMessage());
             _log.error(msg, e);
             return Response.status(Status.BAD_REQUEST).
               entity(TapisRestUtils.createErrorResponse(msg, prettyPrint)).build();
         }
             
         // Fill in the parameter fields.
         String reqTenant = payload.tenant;
         String permSpec  = payload.permSpec;
         
         // ------------------------- Check Authz ------------------------------
         // Authorization passed if a null response is returned.
         Response resp = SKCheckAuthz.configure(reqTenant, null)
                             .setCheckIsFilesService()
                             .check(prettyPrint);
         if (resp != null) return resp;
         
         // ------------------------ Request Processing ------------------------
         // Remove the permission from the role.
         int rows = 0;
         try {rows = getRoleImpl().removePathPermissionFromRoles(reqTenant, permSpec);} 
         catch (Exception e) {
             // Role not found is an error in this case.
             String requestor = TapisThreadLocal.tapisThreadContext.get().getJwtUser();
             String requestorTenant = TapisThreadLocal.tapisThreadContext.get().getJwtTenantId();
             String msg = MsgUtils.getMsg("SK_REMOVE_PERMISSION_FROM_ROLES_ERROR", requestor,
                                          requestorTenant, permSpec, reqTenant, e.getMessage());
             return getExceptionResponse(e, msg, prettyPrint, "Permission", permSpec);
         }

         // Report the number of rows changed.
         ResultChangeCount count = new ResultChangeCount();
         count.changes = rows;
         RespChangeCount r = new RespChangeCount(count);
         
         // ---------------------------- Success ------------------------------- 
         // Success means we found the role. 
         return Response.status(Status.OK).entity(TapisRestUtils.createSuccessResponse(
             MsgUtils.getMsg("TAPIS_UPDATED", "Permission", permSpec), prettyPrint, r)).build();
     }
}
