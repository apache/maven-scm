package org.apache.maven.scm.command.fileinfo;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractFileInfoCommand implements FileInfoCommand
{
    private List files = new ArrayList();
    private List fileInfos = new ArrayList();
    
    public void setFiles(List files)
    {
        this.files = files;
    }
    
    public List getFiles()
    {
        return files;
    }
    
    public void addFile(File file)
    {
        files.add(file);
    }
    
    public void setInformations(List fileInfos)
    {
        this.fileInfos = fileInfos;
    }
    
    public List getInformations()
    {
        return fileInfos;
    }
}