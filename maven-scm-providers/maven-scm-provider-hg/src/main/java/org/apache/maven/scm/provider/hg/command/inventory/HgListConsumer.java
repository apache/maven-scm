package org.apache.maven.scm.provider.hg.command.inventory;

import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmFile;

import java.util.List;
import java.util.ArrayList;

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

/**
 * Get a list of all files in the repository
 *
 * @author <a href="mailto:ryan@darksleep.com">ryan daum</a>
 * @version $Id$
 */
public class HgListConsumer extends HgConsumer {

    private List files = new ArrayList();

    public HgListConsumer( ScmLogger logger )
    {
        super( logger );
    }

    /** {@inheritDoc} */
    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        files.add(new ScmFile(trimmedLine, status));
    }

    public List getFiles() {
        return files;
    }
}
