package edu.utexas.tacc.tapis.jobs.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.utexas.tacc.tapis.jobs.dao.JobsDao;
import edu.utexas.tacc.tapis.jobs.exceptions.JobException;
import edu.utexas.tacc.tapis.jobs.model.Job;
import edu.utexas.tacc.tapis.shared.exceptions.TapisException;
import edu.utexas.tacc.tapis.shared.exceptions.TapisImplException;
import edu.utexas.tacc.tapis.shared.exceptions.TapisImplException.Condition;
import edu.utexas.tacc.tapis.shared.i18n.MsgUtils;

public final class JobsImpl 
 extends BaseImpl
{
    /* ********************************************************************** */
    /*                               Constants                                */
    /* ********************************************************************** */
    // Tracing.
    private static final Logger _log = LoggerFactory.getLogger(JobsImpl.class);
    
    /* ********************************************************************** */
    /*                                Fields                                  */
    /* ********************************************************************** */
    // Singleton instance of this class.
    private static JobsImpl _instance;
    
    /* ********************************************************************** */
    /*                             Constructors                               */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* constructor:                                                           */
    /* ---------------------------------------------------------------------- */
    private JobsImpl() {}
    
    /* ********************************************************************** */
    /*                             Public Methods                             */
    /* ********************************************************************** */
    /* ---------------------------------------------------------------------- */
    /* getInstance:                                                           */
    /* ---------------------------------------------------------------------- */
    public static JobsImpl getInstance()
    {
        // Create the singleton instance if necessary.
        if (_instance == null) {
            synchronized (JobsImpl.class) {
                if (_instance == null) _instance = new JobsImpl();
            }
        }
        return _instance;
    }
    
    /* ---------------------------------------------------------------------- */
    /* getJobByUuid:                                                          */
    /* ---------------------------------------------------------------------- */
    public Job getJobByUuid(String jobUuid, String user, String tenant) 
     throws TapisImplException
    {
        // ----- Check input.
        if (StringUtils.isBlank(jobUuid)) {
            String msg = MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "getJobByUuid", "jobUuid");
            throw new TapisImplException(msg, Condition.BAD_REQUEST);
        }
        if (StringUtils.isBlank(user)) {
            String msg = MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "getJobByUuid", "user");
            throw new TapisImplException(msg, Condition.BAD_REQUEST);
        }
        if (StringUtils.isBlank(tenant)) {
            String msg = MsgUtils.getMsg("TAPIS_NULL_PARAMETER", "getJobByUuid", "tenant");
            throw new TapisImplException(msg, Condition.BAD_REQUEST);
        }
        
        // ----- Get the job.
        Job job = null;
        try {job = getJobsDao().getJobByUUID(jobUuid);}
        catch (Exception e) {
            String msg = MsgUtils.getMsg("JOBS_JOB_SELECT_UUID_ERROR", jobUuid, user, tenant);
            throw new TapisImplException(msg, e, Condition.INTERNAL_SERVER_ERROR);
        }
        
        // ----- Authorization checks.
        // Make sure the user and tenant are authorized.
        if (!tenant.equals(job.getTenant())) {
            String msg = MsgUtils.getMsg("JOBS_MISMATCHED_TENANT", tenant, job.getTenant());
            throw new TapisImplException(msg, Condition.UNAUTHORIZED);
        }
        if (!user.equals(job.getOwner()) && 
            !user.equals(job.getCreatedby()) && 
            !isAdminSafe(user, tenant)) 
        {
            String msg = MsgUtils.getMsg("JOBS_MISMATCHED_OWNER", user, job.getOwner());
            throw new TapisImplException(msg, Condition.UNAUTHORIZED);
        }
        
        // Could be null if not found.
        return job;
    }
    
    /* ---------------------------------------------------------------------- */
    /* queryDB:                                                               */
    /* ---------------------------------------------------------------------- */
    /** This monitoring method does very little logging to avoid log thrashing.
     * 
     * @param tableName the table to be queried
     * @return 0 or 1 on success
     * @throws TapisImplException on error
     */
    public int queryDB(String tableName) throws TapisImplException
    {
        // Get the dao.
        JobsDao dao = null;
        try {dao = getJobsDao();}
            catch (Exception e) {
                String msg = MsgUtils.getMsg("DB_DAO_ERROR", "roles");
               _log.error(msg, e);
                throw new TapisImplException(msg, e, Condition.INTERNAL_SERVER_ERROR);         
             }
        
        // Access the table.
        int rows = 0;
        try {rows = dao.queryDB(tableName);}
        catch (Exception e) {
            String msg = MsgUtils.getMsg("DB_QUERY_DB_ERROR", tableName);
            throw new TapisImplException(msg, e, Condition.INTERNAL_SERVER_ERROR);         
         }
	        return rows;
    }
}
