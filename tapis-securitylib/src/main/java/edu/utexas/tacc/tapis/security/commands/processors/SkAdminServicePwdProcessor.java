package edu.utexas.tacc.tapis.security.commands.processors;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecret;
import edu.utexas.tacc.tapis.security.client.gen.model.SkSecretMetadata;
import edu.utexas.tacc.tapis.security.client.model.SKSecretReadParms;
import edu.utexas.tacc.tapis.security.client.model.SKSecretWriteParms;
import edu.utexas.tacc.tapis.security.client.model.SecretType;
import edu.utexas.tacc.tapis.security.commands.SkAdminParameters;
import edu.utexas.tacc.tapis.security.commands.model.ISkAdminDeployRecorder;
import edu.utexas.tacc.tapis.security.commands.model.SkAdminServicePwd;
import edu.utexas.tacc.tapis.shared.exceptions.TapisException;
import edu.utexas.tacc.tapis.shared.i18n.MsgUtils;

public class SkAdminServicePwdProcessor
 extends SkAdminAbstractProcessor<SkAdminServicePwd>
{
    /* ********************************************************************** */
    /*                               Constants                                */
    /* ********************************************************************** */
    // Tracing.
    private static final Logger _log = LoggerFactory.getLogger(SkAdminServicePwdProcessor.class);
    
    /* ********************************************************************** */
    /*                              Constructors                              */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* constructor:                                                           */
    /* ---------------------------------------------------------------------- */
    public SkAdminServicePwdProcessor(List<SkAdminServicePwd> secrets, 
                                      SkAdminParameters parms)
    {
        super(secrets, parms);
    }
    
    /* ********************************************************************** */
    /*                            Protected Methods                           */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* create:                                                                */
    /* ---------------------------------------------------------------------- */
    @Override
    protected void create(SkAdminServicePwd secret)
    {
        // See if the secret already exists.
        SkSecret skSecret = null;
        try {skSecret = readSecret(secret);} 
        catch (TapisClientException e) {
            // Not found is ok.
            if (e.getCode() != 404) {
                // Save the error condition for this secret.
                _results.recordFailure(Op.create, SecretType.ServicePwd, 
                                       makeFailureMessage(Op.create, secret, e.getMessage()));
                return;
            }
        }
        catch (Exception e) {
            // Save the error condition for this secret.
            _results.recordFailure(Op.create, SecretType.ServicePwd, 
                                   makeFailureMessage(Op.create, secret, e.getMessage()));
            return;
        }
        
        // Don't overwrite the secret.  Note that even if "password" isn't
        // present in the existing secret, that fact that the secret exists
        // is enough to cause us to skip the update.  We may in the future
        // want to make this a cumulative secret update operation.
        if (skSecret != null) {
            _results.recordSkipped(Op.create, SecretType.ServicePwd, 
                                   makeSkippedMessage(Op.create, secret));
            return;
        }
            
        // Create the secret.
        update(secret, Op.create);
    }
    
    /* ---------------------------------------------------------------------- */
    /* update:                                                                */
    /* ---------------------------------------------------------------------- */
    /** The op parameter is indicates the logical or original operation 
     * and is used only to create error messages meaningful to the end user.
     */
    @Override
    protected void update(SkAdminServicePwd secret, Op msgOp)
    {
        SkSecretMetadata metadata = null;
        try {
            // Initialize
            var parms = new SKSecretWriteParms(SecretType.ServicePwd);
            parms.setTenant(secret.tenant);
            parms.setUser(secret.user);  // user maps to service here
            parms.setSecretName(secret.secretName);
            
            // Add the password into the map field. We hardcode
            // the key as "password" in a single element map that
            // gets saved as the actual secret map in vault. 
            var map = new HashMap<String,String>();
            map.put(DEFAULT_KEY_NAME, secret.password);
            parms.setData(map);
            
            // Make the write call.
            metadata = _skClient.writeSecret(parms.getTenant(), parms.getUser(), parms);
        }
        catch (Exception e) {
            // Save the error condition for this secret.
            _results.recordFailure(msgOp, SecretType.ServicePwd, 
                                   makeFailureMessage(msgOp, secret, e.getMessage()));
            return;
        }
        
        // Success.
        _results.recordSuccess(msgOp, SecretType.ServicePwd, 
                               makeSuccessMessage(msgOp, secret));        
    }
    
    /* ---------------------------------------------------------------------- */
    /* deploy:                                                                */
    /* ---------------------------------------------------------------------- */
    @Override
    protected void deploy(SkAdminServicePwd secret, ISkAdminDeployRecorder recorder)
    {
        // Is this secret slated for deployment?
        if (StringUtils.isBlank(secret.kubeSecretName)) {
            _results.recordDeploySkipped(makeSkippedDeployMessage(secret));
            return;
        }    
        
        // See if the secret already exists.
        SkSecret skSecret = null;
        try {skSecret = readSecret(secret);} 
        catch (Exception e) {
            // Save the error condition for this secret.
            _results.recordFailure(Op.deploy, SecretType.ServicePwd, 
                                   makeFailureMessage(Op.deploy, secret, e.getMessage()));
            return;
        }
        
        // This shouldn't happen.
        if (skSecret == null || skSecret.getSecretMap().isEmpty()) {
            String msg = MsgUtils.getMsg("SK_ADMIN_NO_SECRET_FOUND");
            _results.recordFailure(Op.deploy, SecretType.ServicePwd, 
                                   makeFailureMessage(Op.deploy, secret, msg));
            return;
        }
        
        // Validate the specified secret key's value.
        String value = skSecret.getSecretMap().get(DEFAULT_KEY_NAME);
        if (StringUtils.isBlank(value)) {
            String msg = MsgUtils.getMsg("SK_ADMIN_NO_SECRET_FOUND");
            _results.recordFailure(Op.deploy, SecretType.ServicePwd, 
                                   makeFailureMessage(Op.deploy, secret, msg));
            return;
        }
        
        // Record the value as is (no need to base64 encode here).
        recorder.addDeployRecord(secret.kubeSecretName, secret.kubeSecretKey, value);
    }    
    
    /* ---------------------------------------------------------------------- */
    /* makeFailureMessage:                                                    */
    /* ---------------------------------------------------------------------- */
    @Override
    protected String makeFailureMessage(Op op, SkAdminServicePwd secret, String errorMsg)
    {
        // Set the failed flag to alert any subsequent processing.
        secret.failed = true;
        return " FAILED to " + op.name() + " secret \"" + secret.secretName +
               "\" for service \"" + secret.user + "\" in tenant \"" + secret.tenant + 
               "\": " + errorMsg;
    }
    
    /* ---------------------------------------------------------------------- */
    /* makeSkippedMessage:                                                    */
    /* ---------------------------------------------------------------------- */
    @Override
    protected String makeSkippedMessage(Op op, SkAdminServicePwd secret)
    {
        return " SKIPPED " + op.name() + " for secret \"" + secret.secretName +
               "\" for service \"" + secret.user + "\" in tenant \"" + secret.tenant + 
               "\": Already exists.";
    }
    
    /* ---------------------------------------------------------------------- */
    /* makeSuccessMessage:                                                    */
    /* ---------------------------------------------------------------------- */
    @Override
    protected String makeSuccessMessage(Op op, SkAdminServicePwd secret)
    {
        return " SUCCESSFUL " + op.name() + " of secret \"" + secret.secretName +
               "\" for service \"" + secret.user + "\" in tenant " + secret.tenant + "\".";
    }
    
    /* ---------------------------------------------------------------------- */
    /* makeSkippedDeployMessage:                                              */
    /* ---------------------------------------------------------------------- */
    @Override
    protected String makeSkippedDeployMessage(SkAdminServicePwd secret)
    {
        return " SKIPPED deployment of secret \"" + secret.secretName +
               "\" for service \"" + secret.user + "\" in tenant \"" + secret.tenant + 
               "\": No target Kubernetes secret specified.";
    }

    /* ********************************************************************** */
    /*                            Private Methods                             */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* readSecret:                                                            */
    /* ---------------------------------------------------------------------- */
    private SkSecret readSecret(SkAdminServicePwd secret) 
      throws TapisException, TapisClientException
    {
        // Try to read a secret.  HTTP 404 is returned if not found.
        var parms = new SKSecretReadParms(SecretType.ServicePwd);
        parms.setTenant(secret.tenant);
        parms.setUser(secret.user);
        parms.setSecretName(secret.secretName);
        return _skClient.readSecret(parms);
    }
}
