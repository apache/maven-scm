package org.apache.maven.scm.provider.accurev.util;

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

import java.io.File;
import java.io.IOException;

public class WorkspaceUtils
{
    public static boolean isSameFile( File file1, String filename2)
    {
        return isSameFile( file1, filename2 == null ? null : new File( filename2 ) );

    }

    //We need to canonicalise the files (if we can) before we compare them..
    public static boolean isSameFile( File file1, File file2 )
    {
        
        if ( file1 == file2 || ( file1 == null && file2 == null ) )
        {
            return true;
        }

        if ( file1 == null || file2 == null )
        {
            return false;
        }

        try
        {
            file1 = file1.getCanonicalFile();
        }
        catch ( IOException ioEx )
        {
            //Oh well, we'll compare the non-canonicalised file then.
        }

        try
        {
            file2 = file2.getCanonicalFile();
        }
        catch ( IOException ioEx )
        {
            //Oh well, we'll compare the non-canonicalised file then.
        }
        return file1.equals( file2 );
    }

    private WorkspaceUtils()
    {
        // no op only to prevents class creation
    }
}
