---
title: How to write a new SCM provider
author: 
  - Maven Team
date: 2007-03-27
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
# How to write a new SCM provider?

## What are the steps to write a new Maven SCM provider?

- Define allowed scm urls for this provider
- Create a class that extends `org.apache.maven.scm.provider.ScmProviderRepository` or `org.apache.maven.scm.provider.ScmProviderRepositoryWithHost`, this class is the corresponding bean of the scm url
- Create a class that extends `org.apache.maven.scm.provider.AbstractScmProvider`. This class parse the scm url and link all scm commands methods to their implementations. Important methods are `makeProviderScmRepository()` and `validateScmUrl()`
- Implement all commands and link them in the scm provider class created in the step above
- For each command, implement junit tests that test the command line format
- For each command, implement TCK tests
- Test the release plugin with the new provider. For that, you must add the dependency to the release plugin and run it
- Add the dependency to Continuum libs and test the provider with a sample project
- Update the site

In the next section, we&apos;ll see all the steps in details to write a new Maven SCM provider.

## Create a new Maven project for the provider

Your project need to use some jars from the Maven SCM framework. Add them to your POM.

```unknown
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <artifactId>maven-scm-providers</artifactId>
    <groupId>org.apache.maven.scm</groupId>
    <version>LATEST VERSION OF MAVEN-SCM PROVIDERS MASTER POM</version>
  </parent>

  <artifactId>maven-scm-provider-YOUR_PROVIDER_NAME</artifactId>
  <version>1.0-SNAPSHOT</version>

  <name>My Maven SCM Provider</name>

  <build>
    <plugins>
      <plugin>
        <groupId>org.eclipse.sisu</groupId>
        <artifactId>sisu-maven-plugin</artifactId>
        <version>${sisuVersion}</version>
        <executions>
          <execution>
            <id>index-project</id>
            <goals>
              <goal>main-index</goal>
              <goal>test-index</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

The Sisu Maven Plugin will generate the components meta-data file used by the DI to discover them inject them to SCM manager.

## Create an SCM Provider Repository class

This class will contain all SCM information about your SCM connection \(user, password, host, port, path...\).

```unknown
package org.apache.maven.scm.provider.myprovider.repository;

import org.apache.maven.scm.provider.ScmProviderRepository;

public class MyScmProviderRepository
    extends ScmProviderRepository
{
}
```

Before you add more information to this class, you can look at the `ScmProviderRepository` sub-classes, if they are already implemented.

## Create the Provider class

This class is the central point of the provider. The Maven SCM framework will know only this class in the provider, so this class must validate the scm url, populate the `ScmProviderRepository` and provide all commands supported by your provider. We start with a basic class, then we&apos;ll add commands to it when we implement them.

Before you start to write your SCM provider, you must define the SCM URLs you want to support.

```unknown
package org.apache.maven.scm.provider.myprovider;

import org.apache.maven.scm.provider.myprovider.repository.MyScmProviderRepository;

import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * My provider.
 */
@Singleton
@Named( "provider_name" )
public class MyScmProvider
    extends AbstractScmProvider
{
    public String getScmType()
    {
        return "provider_name";
    }

    /**
     * This method parse the scm URL and return a SCM provider repository.
     * At this point, the scmSpecificUrl is the part after scm:provider_name: in your SCM URL.
     */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
         MyScmProviderRepository providerRepository = new MyScmProviderRepository();
         //Parse scmSpecificUrl and populate there your provider repository

         return providerRepository;
    }
}
```

The JSR330 annotations will be used by the Sisu Maven Plugin, declared in the POM, to generate component meta-data. Generally, we use the string just after _scm:_ in the scm URL as the _provider\_name_.

## Commands implementation

When you write a new SCM command, you must extend base classes for the Maven SCM framework. We have one base command for each command supported by Maven SCM and each command have an `execute` method that return an SCM result.

```unknown
package org.apache.maven.scm.provider.myprovider.command.checkout;

import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;

public class MyCheckoutCommand
    extends AbstractCheckOutCommand
{
    protected abstract CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        CheckOutScmResult result = new CheckOutScmResult();

        //Add the code there to run the command
        //If you need to run a native commandline like git/svn/..., look at other providers how to launch it and parse the output

        return result;
    }
}
```

## Allow the command in the SCM provider

Now that your command is implemented, you need to add it in your SCM provider \(`MyScmProvider`\). Open the provider class and override the method that relates to your command.

```unknown
public class MyScmProvider
    extends AbstractScmProvider
{
    ...

    protected CheckOutScmResult checkout( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        MyCheckoutCommand command = new MyCheckoutCommand();
        command.setLogger( getLogger() );
        return (CheckOutScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }
}
```

## Provider Tests

### Automated tests

To be sure your provider works as expected, you must implement some tests. You can implement two levels of tests:

- Simple JUnit tests that use your command directly and test that the command line you launch in your SCM command is correct
- Implementation of the TCK. The TCK provides a set of tests that validate that your implementation is compatible with the Maven SCM framework. The TCK requires access to the SCM tool.
### Other tests

You can do manual tests in the real world with the Maven SCM Plugin, the Maven Release Plugin, the Maven Changelog Plugin and Continuum.

It&apos;s important to test your SCM provider with these tools, because they are used by users that will use your provider.

## Document your provider

Now that your provider works fine, you must document it \(which scm URLs are supported, which commands are supported...\). You can use the same template that is used by the other providers.

