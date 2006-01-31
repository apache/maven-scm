package org.apache.maven.scm.provider.bazaar.command.remove;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
 */

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarRemoveConsumer
    extends BazaarConsumer
{
    private final List removedFiles = new ArrayList();

    public BazaarRemoveConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        //TODO
    }

    public List getRemovedFiles()
    {
        return removedFiles;
    }
}
