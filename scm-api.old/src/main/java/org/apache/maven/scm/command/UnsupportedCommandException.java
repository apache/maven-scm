package org.apache.maven.scm.command;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

import org.apache.maven.scm.ScmException;

/**
 * 
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a> 
 * @version $Id$
 */
public class UnsupportedCommandException extends ScmException
{
    /**
     * @param message
     */
    public UnsupportedCommandException(String message)
    {
        super(message);
    }

     /**
     * @param message
     * @param cause
     */
    public UnsupportedCommandException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
