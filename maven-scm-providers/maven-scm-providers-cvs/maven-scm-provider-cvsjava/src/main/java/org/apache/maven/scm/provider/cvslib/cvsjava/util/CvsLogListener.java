package org.apache.maven.scm.provider.cvslib.cvsjava.util;

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

import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

/**
 * A basic implementation of a CVS listener. It merely saves up
 * into StringBuffers the stdout and stderr printstreams.
 * 
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 */
public class CvsLogListener extends CVSAdapter
{
    private final StringBuffer taggedLine = new StringBuffer();
    private StringBuffer stdout = new StringBuffer();
    private StringBuffer stderr = new StringBuffer();

    /**
	 * Called when the server wants to send a message to be displayed to the
	 * user. The message is only for information purposes and clients can
	 * choose to ignore these messages if they wish.
	 * 
	 * @param e the event
	 */
    public void messageSent(MessageEvent e)
    {
        String line = e.getMessage();
        StringBuffer stream = e.isError() ? stderr : stdout;

        if (e.isTagged())
        {
            String message =
                MessageEvent.parseTaggedMessage(taggedLine, e.getMessage());
            if (message != null)
            {
                //stream.println(message);
                stream.append( message ).append( "\n" );

            }
        }
        else
        {
            //stream.println(line);
            stream.append( line ).append( "\n" );

        }
    }

    /**
     * @return Returns the standard output from cvs as a StringBuffer..
     */
    public StringBuffer getStdout()
    {
        return stdout;
    }

    /**
     * @return Returns the standard error from cvs as a StringBuffer..
     */
    public StringBuffer getStderr()
    {
        return stderr;
    }
}