package edu.utexas.tacc.tapis.security.api.requestBody;

import org.apache.commons.lang3.StringUtils;

import edu.utexas.tacc.tapis.security.api.utils.SKApiUtils;
import edu.utexas.tacc.tapis.shared.i18n.MsgUtils;

public final class ReqUpdateRoleName 
 implements IReqBody
{
    public String roleTenant;
    public String newRoleName;

    /** Return a user-appropriate error message on failed validation
     *  and return null if validation succeeds.
     */ 
    @Override
    public String validate() 
    {
        // Final checks.
        if (StringUtils.isBlank(roleTenant)) 
            return MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "updateRoleName", "roleTenant");
        if (StringUtils.isBlank(newRoleName)) 
            return MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "updateRoleName", "newRoleName");
        if (!SKApiUtils.isValidName(newRoleName)) 
            return MsgUtils.getMsg("TAPIS_INVALID_PARAMETER", "updateRoleName", "newRoleName",
                                   newRoleName);

        // Success.
        return null;
    }
}

