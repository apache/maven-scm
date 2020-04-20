package org.apache.maven.scm.provider.dimensionscm.command.login;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.login.AbstractLoginCommand;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * Dimensions CM implementation for Maven's AbstractLoginCommand.
 */
public class DimensionsLoginCmd extends AbstractLoginCommand
{

    @Override
    public LoginScmResult executeLoginCommand( ScmProviderRepository repository,
        ScmFileSet fileSet, CommandParameters commandParameters )
    {
        return new LoginScmResult( StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, true );
    }
}
