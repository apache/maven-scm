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
package org.apache.maven.scm.provider.svn.svnexe.command.remoteinfo;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.remoteinfo.AbstractRemoteInfoCommand;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Olivier Lamy
 * @since 1.6
 */
public class SvnRemoteInfoCommand extends AbstractRemoteInfoCommand implements SvnCommand {
    @Override
    public RemoteInfoScmResult executeRemoteInfoCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {

        String url = ((SvnScmProviderRepository) repository).getUrl();
        // use a default svn layout, url is here http://svn.apache.org/repos/asf/maven/maven-3/trunk
        // so as we presume we have good users using standard svn layout, we calculate tags and branches url
        url = StringUtils.stripEnd(url, "/");
        int idx = url.lastIndexOf("/");
        String baseUrl = url.substring(0, idx);

        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine(
                fileSet == null ? null : fileSet.getBasedir(), (SvnScmProviderRepository) repository);

        cl.createArg().setValue("ls");

        cl.createArg().setValue(baseUrl + "/tags" + "@");

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        LsConsumer consumer = new LsConsumer(baseUrl + "/tags");

        int exitCode = 0;

        Map<String, String> tagsInfos = null;

        try {
            exitCode = SvnCommandLineUtils.execute(cl, consumer, stderr);
            tagsInfos = consumer.infos;

        } catch (CommandLineException ex) {
            throw new ScmException("Error while executing svn command.", ex);
        }

        if (exitCode != 0) {
            return new RemoteInfoScmResult(cl.toString(), "The svn command failed.", stderr.getOutput(), false);
        }

        cl = SvnCommandLineUtils.getBaseSvnCommandLine(
                fileSet == null ? null : fileSet.getBasedir(), (SvnScmProviderRepository) repository);

        cl.createArg().setValue("ls");

        cl.createArg().setValue(baseUrl + "/branches" + "@");

        stderr = new CommandLineUtils.StringStreamConsumer();

        consumer = new LsConsumer(baseUrl + "/branches");

        Map<String, String> branchesInfos = null;

        try {
            exitCode = SvnCommandLineUtils.execute(cl, consumer, stderr);
            branchesInfos = consumer.infos;

        } catch (CommandLineException ex) {
            throw new ScmException("Error while executing svn command.", ex);
        }

        if (exitCode != 0) {
            return new RemoteInfoScmResult(cl.toString(), "The svn command failed.", stderr.getOutput(), false);
        }

        return new RemoteInfoScmResult(cl.toString(), branchesInfos, tagsInfos);
    }

    public boolean remoteUrlExist(ScmProviderRepository repository, CommandParameters parameters) throws ScmException {
        String url = ((SvnScmProviderRepository) repository).getUrl();

        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine(null, (SvnScmProviderRepository) repository);

        cl.createArg().setValue("ls");

        cl.createArg().setValue(url + "@");

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        LsConsumer consumer = new LsConsumer(url);

        int exitCode = 0;

        try {
            exitCode = SvnCommandLineUtils.execute(cl, consumer, stderr);
        } catch (CommandLineException ex) {
            throw new ScmException("Error while executing svn command.", ex);
        }

        if (exitCode != 0) {
            String output = stderr.getOutput();
            // olamy: a bit ugly but....
            // trying to parse error from svn cli which indicate no remote path
            if (output.contains("W160013") || output.contains("svn: URL")) {
                return false;
            }
            throw new ScmException(cl + ".The svn command failed:" + stderr.getOutput());
        }

        return true;
    }

    private static class LsConsumer extends AbstractConsumer {
        Map<String, String> infos = new HashMap<>();

        String url;

        LsConsumer(String url) {
            this.url = url;
        }

        public void consumeLine(String s) {
            infos.put(StringUtils.stripEnd(s, "/"), url + "/" + s);
        }

        Map<String, String> getInfos() {
            return infos;
        }
    }
}
