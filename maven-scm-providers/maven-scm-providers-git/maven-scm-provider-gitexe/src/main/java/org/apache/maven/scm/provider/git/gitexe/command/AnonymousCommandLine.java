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
package org.apache.maven.scm.provider.git.gitexe.command;

import org.apache.maven.scm.provider.git.util.GitUtil;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * CommandLine extension to mask password
 * @since 1.9.3
 */
public class AnonymousCommandLine extends Commandline {

    /**
     * Provides an anonymous output to mask password. Considering URL of type :
     * &lt;&lt;protocol&gt;&gt;://&lt;&lt;user&gt;&gt;:&lt;&lt;password&gt;&gt;@
     * &lt;&lt;host_definition&gt;&gt;
     */
    @Override
    public String toString() {
        String output = GitUtil.maskPasswordInUrl(super.toString());
        return output;
    }
}
