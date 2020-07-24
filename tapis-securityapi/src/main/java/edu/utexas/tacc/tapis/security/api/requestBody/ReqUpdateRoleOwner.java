package edu.utexas.tacc.tapis.security.api.requestBody;

import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.shared.i18n.MsgUtils;

public final class ReqUpdateRoleOwner 
 implements IReqBody
{
    public String tenant;
    public String user;
    public String newOwner;

    /** Return a user-appropriate error message on failed validation
     *  and return null if validation succeeds.
     */ 
    @Override
    public String validate() 
    {
        // Final checks.
        if (StringUtils.isBlank(tenant)) 
            return MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "updateRoleDescription", "tenant");
        if (StringUtils.isBlank(user)) 
            return MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "updateRoleDescription", "user");
        if (StringUtils.isBlank(newOwner)) 
            return MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "updateRoleDescription", "newOwner");
        
        // Success.
        return null;
    }
}
