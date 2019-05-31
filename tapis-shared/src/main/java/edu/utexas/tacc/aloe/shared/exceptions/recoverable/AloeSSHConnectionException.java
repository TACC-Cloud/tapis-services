package edu.utexas.tacc.aloe.shared.exceptions.recoverable;

import java.util.TreeMap;

public class AloeSSHConnectionException 
 extends AloeRecoverableException 
{
    private static final long serialVersionUID = 6143550896805006147L;
    
	public AloeSSHConnectionException(String message, Throwable cause, TreeMap<String,String> state) 
	{
	    super(message, cause, state);
	}
}
