---
title: How to use Maven SCM in my application
author: 
  - Maven Team
date: 2007-03-26
---

<!-- Licensed to the Apache Software Foundation (ASF) under one-->
<!-- or more contributor license agreements.  See the NOTICE file-->
<!-- distributed with this work for additional information-->
<!-- regarding copyright ownership.  The ASF licenses this file-->
<!-- to you under the Apache License, Version 2.0 (the-->
<!-- "License"); you may not use this file except in compliance-->
<!-- with the License.  You may obtain a copy of the License at-->
<!---->
<!--   http://www.apache.org/licenses/LICENSE-2.0-->
<!---->
<!-- Unless required by applicable law or agreed to in writing,-->
<!-- software distributed under the License is distributed on an-->
<!-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY-->
<!-- KIND, either express or implied.  See the License for the-->
<!-- specific language governing permissions and limitations-->
<!-- under the License.-->
# How to use Maven SCM in my application?

## Create an SCM Manager

### With Plexus IOC

With [Plexus](http://codehaus-plexus.github.io/), it&apos;s very easy to use Maven SCM because it injects all dependencies in fields, so you only have to write minimal code.

```unknown
import org.apache.maven.scm.manager.ScmManager;

public class MyApp
{
    private ScmManager scmManager;

    public MyApp()
    {
        plexus = new Embedder();

        plexus.start();

        scmManager = (ScmManager) plexus.lookup( ScmManager.ROLE );
    }

    public ScmManager getScmManager()
    {
        return scmManager;
    }
```

### Without Plexus IOC

Without Plexus, you must add all your SCM providers in the manager and that will require more work. You can use the basic SCM manager or write your own:

```unknown
import org.apache.maven.scm.manager.BasicScmManager;

public class MyApp
{
    private ScmManager scmManager;

    public MyApp()
    {
        scmManager = new BasicScmManager();

        //Add all SCM providers we want to use
        scmManager.setScmProvider( "git", new GitExeScmProvider() );
        scmManager.setScmProvider( "svn", new SvnExeScmProvider() );
        ...
    }

    public ScmManager getScmManager()
    {
        return scmManager;
    }
```

## Run a SCM command

Before you call a command, the SCM manager needs an `ScmRepository`. This object contains all the information about the SCM connection.

```unknown
    public ScmRepository getScmRepository( String scmUrl )
        throw Exception
    {
        ScmRepository repository;

        try
        {
            return getScmManager().makeScmRepository( scmUrl );
        }
        catch ( NoSuchScmProviderException ex )
        {
            throw new Exception( "Could not find a provider.", ex );
        }
        catch ( ScmRepositoryException ex )
        {
            throw new Exception( "Error while connecting to the repository", ex );
        }
    }
```

### Checkout command

```unknown
    public void checkOut( ScmRepository scmRepository, File workingDirectory )
        throws ScmException
    {
        if ( workingDirectory.exists() )
        {
            System.err.println( "The working directory already exist: '" + workingDirectory.getAbsolutePath() + "'." );

            return;
        }

        if ( !workingDirectory.mkdirs() )
        {
            System.err.println(
                "Error while making the working directory: '" + workingDirectory.getAbsolutePath() + "'." );

            return;
        }

        CheckOutScmResult result = scmManager.checkOut( scmRepository, new ScmFileSet( workingDirectory ) );

        checkResult( result );

        List checkedOutFiles = result.getCheckedOutFiles();

        System.out.println( "Checked out these files: " );

        for ( Iterator it = checkedOutFiles.iterator(); it.hasNext(); )
        {
            ScmFile file = (ScmFile) it.next();

            System.out.println( " " + file.getPath() );
        }
    }
```

### Update command

```unknown
    public void update( ScmRepository scmRepository, File workingDirectory )
        throws ScmException
    {
        if ( !workingDirectory.exists() )
        {
            System.err.println( "The working directory doesn't exist: '" + workingDirectory.getAbsolutePath() + "'." );

            return;
        }

        UpdateScmResult result = scmManager.update( scmRepository, new ScmFileSet( workingDirectory ) );

        checkResult( result );

        List updatedFiles = result.getUpdatedFiles();

        System.out.println( "Updated these files: " );

        for ( Iterator it = updatedFiles.iterator(); it.hasNext(); )
        {
            ScmFile file = (ScmFile) it.next();

            System.out.println( " " + file.getPath() );
        }
    }
```

## The checkResult method

In each sample command code, we use the `checkResult` method, it isn&apos;t required but can be useful if something failed in the command execution.

```unknown
    public void checkResult( ScmResult result )
        throws Exception
    {
        if ( !result.isSuccess() )
        {
            System.err.println( "Provider message:" );

            System.err.println( result.getProviderMessage() == null ? "" : result.getProviderMessage() );

            System.err.println( "Command output:" );

            System.err.println( result.getCommandOutput() == null ? "" : result.getCommandOutput() );

            throw new Exception(
                "Command failed." + StringUtils.defaultString( result.getProviderMessage() ) );
        }
    }
```

## Sample code

The code above is available here: [Maven SCM client](http://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-client/).

