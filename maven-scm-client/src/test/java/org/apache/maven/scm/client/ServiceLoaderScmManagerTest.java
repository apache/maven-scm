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
package org.apache.maven.scm.client;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.scm.manager.BasicScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.apache.maven.scm.provider.git.jgit.JGitScmProvider;
import org.apache.maven.scm.provider.hg.HgScmProvider;
import org.apache.maven.scm.provider.local.LocalScmProvider;
import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Builds a {@link ScmManager} with no Sisu or Plexus container, from the {@code META-INF/services} entries.
 */
class ServiceLoaderScmManagerTest {

    @Test
    void providersAreResolvedByTheirNamedKey() throws Exception {
        ScmManager manager = BasicScmManager.fromServiceLoader(getClass().getClassLoader());

        assertInstanceOf(GitExeScmProvider.class, manager.getProviderByType("git"));
        assertInstanceOf(JGitScmProvider.class, manager.getProviderByType("jgit"));
        assertInstanceOf(SvnExeScmProvider.class, manager.getProviderByType("svn"));
        assertInstanceOf(HgScmProvider.class, manager.getProviderByType("hg"));
        assertInstanceOf(LocalScmProvider.class, manager.getProviderByType("local"));
    }

    @Test
    void gitAndJgitAreDistinct() throws Exception {
        ScmManager manager = BasicScmManager.fromServiceLoader(getClass().getClassLoader());

        ScmProvider git = manager.getProviderByType("git");
        assertNotSame(git, manager.getProviderByType("jgit"));
    }

    @Test
    void providerThatCannotBeLoadedIsSkipped(@TempDir Path dir) throws Exception {
        Path services = dir.resolve("META-INF/services/" + ScmProvider.class.getName());
        Files.createDirectories(services.getParent());
        Files.write(services, "org.example.MissingScmProvider\n".getBytes(StandardCharsets.UTF_8));

        try (URLClassLoader loader =
                new URLClassLoader(new URL[] {dir.toUri().toURL()}, getClass().getClassLoader())) {
            ScmManager manager = BasicScmManager.fromServiceLoader(loader);

            assertInstanceOf(GitExeScmProvider.class, manager.getProviderByType("git"));
            assertInstanceOf(SvnExeScmProvider.class, manager.getProviderByType("svn"));
            assertThrows(NoSuchScmProviderException.class, () -> manager.getProviderByType("missing"));
        }
    }
}
