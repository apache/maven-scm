package org.apache.maven.scm.provider.integrity;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItemIterator;
import com.mks.api.response.InterruptedException;

/**
 * Helper class to appropriately diagnose an APIException
 * @version $Id: ExceptionHandler.java 1.2 2011/08/22 13:06:45EDT Cletus D'Souza (dsouza) Exp  $
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 */
public class ExceptionHandler 
{
    // Private variables to provide diagnostics on the exception
    private String message;
    private String command;
    private int exitCode;
    
    /**
     * Constructor requires a single APIException to debug
     * @param ex APIException 
     */
    public ExceptionHandler( APIException ex )
    {
        
        // API Exceptions can be nested.  Hence we will need to recurse the 
        // exception hierarchy to dig for a conclusive message
        Response response = ex.getResponse();

        // Print the stack trace to standard out for debugging purposes
        ex.printStackTrace();
        
        // The API failed to execute the command (i.e. a real API error)
        if ( null == response )
        {            
            message = ex.getMessage();
            command = new java.lang.String();
            //exitCode = Integer.parseInt(ex.getExceptionId());
            exitCode = -1;
        }
        else
        {
            command = response.getCommandString();
            try
            {
                exitCode = response.getExitCode();
            }
            catch ( InterruptedException ie )
            {
                // Just print out the stack trace
                ie.printStackTrace();
                exitCode = -1;
            }
            WorkItemIterator wit = response.getWorkItems();
            // In the event there is a problem with one of the command's elements
            // we have to dig deeper into the exception...
            try
            {
                while ( wit.hasNext() )
                {
                    wit.next();
                }
                // If we got here then just extract the message
                if ( ex.getMessage() != null )
                {
                    message = ex.getMessage();
                }
            }
            catch ( APIException ae )
        {
                // This message will be the real reason for the exception
                String curMessage = ae.getMessage();
                if ( curMessage != null )
                {
                    message = curMessage;
                }
                ae.printStackTrace();
            }
        }        
    }
    
    /**
     * Returns the Message obtained from the APIException
     * @return message APIException String
     */
    public String getMessage()
    {
        return message;
    }
    
    /**
     * Returns the executed command that caused the exception
     * @return command Complete CLI Command String
     */
    public String getCommand()
    {
        return command;
    }
    
    /**
     * Returns the exit codes associated with executing the command
     * @return exitCode API/CLI Exit Code
     */
    public int getExitCode()
    {
        return exitCode;
    }
}
