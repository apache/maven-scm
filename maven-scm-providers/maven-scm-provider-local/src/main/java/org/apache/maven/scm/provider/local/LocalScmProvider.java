/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.provider.local;

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.add.LocalAddCommand;
import org.apache.maven.scm.provider.local.command.changelog.LocalChangeLogCommand;
import org.apache.maven.scm.provider.local.command.checkin.LocalCheckInCommand;
import org.apache.maven.scm.provider.local.command.checkout.LocalCheckOutCommand;
import org.apache.maven.scm.provider.local.command.list.LocalListCommand;
import org.apache.maven.scm.provider.local.command.mkdir.LocalMkdirCommand;
import org.apache.maven.scm.provider.local.command.status.LocalStatusCommand;
import org.apache.maven.scm.provider.local.command.tag.LocalTagCommand;
import org.apache.maven.scm.provider.local.command.update.LocalUpdateCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Singleton
@Named("local")
public class LocalScmProvider extends AbstractScmProvider {
    /** {@inheritDoc} */
    @Override
    public String getScmType() {
        return "local";
    }

    /** {@inheritDoc} */
    @Override
    public ScmProviderRepository makeProviderScmRepository(String scmSpecificUrl, char delimiter)
            throws ScmRepositoryException {
        String[] tokens = StringUtils.split(scmSpecificUrl, Character.toString(delimiter));

        if (tokens.length != 2) {
            throw new ScmRepositoryException("The connection string didn't contain the expected number of tokens. "
                    + "Expected 2 tokens but got " + tokens.length + " tokens.");
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String root = tokens[0];

        File rootFile = new File(root);

        if (!rootFile.isAbsolute()) {
            String basedir = System.getProperty("basedir", new File("").getAbsolutePath());

            rootFile = new File(basedir, root);
        }

        if (!rootFile.exists()) {
            throw new ScmRepositoryException("The root doesn't exists (" + rootFile.getAbsolutePath() + ").");
        }

        if (!rootFile.isDirectory()) {
            throw new ScmRepositoryException("The root isn't a directory (" + rootFile.getAbsolutePath() + ").");
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String module = tokens[1];

        File moduleFile = new File(rootFile, module);

        if (!moduleFile.exists()) {
            throw new ScmRepositoryException(
                    "The module doesn't exist (root: " + rootFile.getAbsolutePath() + ", module: " + module + ").");
        }

        if (!moduleFile.isDirectory()) {
            throw new ScmRepositoryException("The module isn't a directory.");
        }

        return new LocalScmProviderRepository(rootFile.getAbsolutePath(), module);
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static String fixModuleName(String module) {
        if (module.endsWith("/")) {
            module = module.substring(0, module.length() - 1);
        }

        if (module.startsWith("/")) {
            module = module.substring(1);
        }

        return module;
    }

    /** {@inheritDoc} */
    @Override
    public StatusScmResult status(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        LocalStatusCommand command = new LocalStatusCommand();

        return (StatusScmResult) command.execute(repository, fileSet, parameters);
    }

    /** {@inheritDoc} */
    @Override
    public TagScmResult tag(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        LocalTagCommand command = new LocalTagCommand();

        return (TagScmResult) command.execute(repository, fileSet, parameters);
    }

    /** {@inheritDoc} */
    @Override
    public AddScmResult add(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        LocalAddCommand command = new LocalAddCommand();

        return (AddScmResult) command.execute(repository, fileSet, parameters);
    }

    /** {@inheritDoc} */
    @Override
    protected ChangeLogScmResult changelog(
            ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
        LocalChangeLogCommand command = new LocalChangeLogCommand();

        return (ChangeLogScmResult) command.execute(repository, fileSet, parameters);
    }

    /** {@inheritDoc} */
    @Override
    public CheckInScmResult checkin(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        LocalCheckInCommand command = new LocalCheckInCommand();

        return (CheckInScmResult) command.execute(repository, fileSet, parameters);
    }

    /** {@inheritDoc} */
    @Override
    public CheckOutScmResult checkout(
            ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
        LocalCheckOutCommand command = new LocalCheckOutCommand();

        return (CheckOutScmResult) command.execute(repository, fileSet, parameters);
    }

    /** {@inheritDoc} */
    @Override
    protected ListScmResult list(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        LocalListCommand command = new LocalListCommand();

        return (ListScmResult) command.execute(repository, fileSet, parameters);
    }

    /** {@inheritDoc} */
    @Override
    protected MkdirScmResult mkdir(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        LocalMkdirCommand command = new LocalMkdirCommand();

        return (MkdirScmResult) command.execute(repository, fileSet, parameters);
    }

    /** {@inheritDoc} */
    @Override
    public UpdateScmResult update(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        LocalUpdateCommand command = new LocalUpdateCommand();

        return (UpdateScmResult) command.execute(repository, fileSet, parameters);
    }
}
