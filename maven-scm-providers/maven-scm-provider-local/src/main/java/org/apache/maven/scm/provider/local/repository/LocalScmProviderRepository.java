package org.apache.maven.scm.provider.local.repository;

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

import org.apache.maven.scm.provider.ScmProviderRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalScmProviderRepository
    extends ScmProviderRepository
{
    private String root;

    private String module;

    private Set<String> addedFiles = new HashSet<String>();

    /**
     * @param root
     * @param module
     */
    public LocalScmProviderRepository( String root, String module )
    {
        this.root = root;

        this.module = module;
    }

    /**
     * @return Returns the root.
     */
    public String getRoot()
    {
        return root;
    }

    /**
     * @return Returns the module.
     */
    public String getModule()
    {
        return module;
    }

    public void addFile( String path )
    {
        addedFiles.add( path );
    }

    public boolean isFileAdded( String path )
    {
        return addedFiles.contains( path );
    }
}
