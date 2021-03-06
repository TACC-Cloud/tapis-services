package edu.utexas.tacc.tapis.jobs.api.driver;

import java.io.IOException;

import edu.utexas.tacc.tapis.client.shared.exceptions.TapisClientException;
import edu.utexas.tacc.tapis.files.client.FilesClient;

public class GetPathPerms 
{
    // ***************** Configuration Parameters *****************
    private static final String BASE_URL = "https://dev.develop.tapis.io";
    private static final String userJWT = 
        "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIxOTVhNzU2YS1hZWRhLTQ2NjQtODFhMS1hODg1ZDZhZmJiNDMiLCJpc3MiOiJodHRwczovL2Rldi5kZXZlbG9wLnRhcGlzLmlvL3YzL3Rva2VucyIsInN1YiI6InRlc3R1c2VyMkBkZXYiLCJ0YXBpcy90ZW5hbnRfaWQiOiJkZXYiLCJ0YXBpcy90b2tlbl90eXBlIjoiYWNjZXNzIiwidGFwaXMvZGVsZWdhdGlvbiI6ZmFsc2UsInRhcGlzL2RlbGVnYXRpb25fc3ViIjpudWxsLCJ0YXBpcy91c2VybmFtZSI6InRlc3R1c2VyMiIsInRhcGlzL2FjY291bnRfdHlwZSI6InVzZXIiLCJleHAiOjE5MjU4MzgyMzF9.wbyeWa6PQpROtnPWpykKc9ln2TQj04cD_uwjS40UeF5PMDJ7jd5u8GJ0JPyaH-qj9R3H9-J4H9vQGPnKQg7Wqj9_QIja9t5g5WM7Vz70TaXmu91EO3_rbJkmguXZMRFdBS0YFDYGLccO2i50NVyt3i-nVRAp3nFCn5-eB6UEoU_KEe5MiFnMmuzF6kUIGDi6Cw_26DxI_SsY-zcpjCmX0jx5cM0xqLv8XNv1RIVr8o9fKGuvGupdT0ZdTCp_MiMBPi11OE7OCYo7iwp-yglcpOMlQF8LOCJe9txzJGqcCZSGbQBi4mNLasyYn0cstVak9ToGQpEpiSdz4FvQtSKT9A";
    
    // System id.
    private static final String EXEC_SYSTEM = "tapisv3-exec";
    private static final String PATH = "/jobs/input"
;    // ***************** End Configuration Parameters *************
    
    /** Driver
     * 
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception
    {
        // Try to submit a job.
        var pathPerms = new GetPathPerms();
        pathPerms.get(args);
    }
    
    /** Get a list of files on the system.
     * 
     * @param args contains the name of a request file
     * @throws IOException 
     * @throws TapisClientException 
     */
    public void get(String[] args) throws IOException, TapisClientException
    {
        // Check permissions.
        var filesClient = new FilesClient(BASE_URL, userJWT);
        var filePerm = filesClient.getFilePermissions(EXEC_SYSTEM, PATH, "testuser2");
        if (filePerm == null) {
            System.out.println("Null list returned!");
        } else {
            System.out.println("\nsystem: " + filePerm.getSystemId());
            System.out.println("  path  : " + filePerm.getPath());
            System.out.println("  perm  : " + filePerm.getPermission());
            System.out.println("  tenant: " + filePerm.getTenantId());
            System.out.println("  use   : " + filePerm.getUsername());
        }
    }
}
