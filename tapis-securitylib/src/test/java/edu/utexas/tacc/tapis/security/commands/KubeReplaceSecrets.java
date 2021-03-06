package edu.utexas.tacc.tapis.security.commands;

import java.io.IOException;
import java.util.HashMap;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.Config;

public class KubeReplaceSecrets 
{
    private static final String SECRET_NAME = "rich-test-secret";
    
    public static void main(String[] args) throws ApiException, IOException 
    {
        // Get the required kubernetes information from the environment.
        String kubeToken = System.getenv("KUBE_TOKEN");
        String kubeUrl   = System.getenv("KUBE_URL");
        String kubeNS    = System.getenv("KUBE_NAMESPACE"); 
        
        // Get a kube client.
        ApiClient apiClient = Config.fromToken(kubeUrl, kubeToken, false);
        // ApiClient apiClient = Config.fromConfig("/home/rcardone/.kube/config");
        CoreV1Api coreApi = new CoreV1Api(apiClient);
        
        // Assign some dummy secrets.
        var map = new HashMap<String,String>();
        map.put("key3", "val3");
        
        // Create the metadata.
        V1ObjectMeta meta = new V1ObjectMeta();
        meta.setName(SECRET_NAME);
        meta.setNamespace(kubeNS);
        
        // Create a secret.
        V1Secret secret = new V1Secret();
        secret.setApiVersion("v1");
        secret.setKind("Secret");
        secret.setStringData(map);
        secret.setMetadata(meta);
        
        // Make the call. Replace will fail if the secret doesn't already exist.
        V1Secret newSecret = null;
        try {newSecret = coreApi.replaceNamespacedSecret(SECRET_NAME, kubeNS, secret, null, null,  null);}
            catch (ApiException e) {
                e.printStackTrace();
                return;
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        
        System.out.println(newSecret);
    }

}
