---
title: Usage
author: 
  - Pete Marvin King
date: 2008-08-13
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
# Usage

The SCM Plugin maps a lot of commands to a variety of SCM implementations. But there are only 2 frequently used commands:

- `checkin` - commit the changes to the remote repository \( SCM server \).
- `update` - updates the local working copy with the one from the remote repository \( SCM server \).

# Configuring SCM

Each SCM has a different command line/library API invocation to commit the modified sources. Using Maven this process is simplified by providing a uniform way to do this by letting Maven handle the API/CLI translation to perform the SCM task.

To configure the SCM support for Maven you need the [SCM](http://maven.apache.org/pom.html#SCM) configuration in your `pom.xml`.

```unknown
<project>
  ...
  <packaging>jar</packaging>
  <version>1.0-SNAPSHOT</version>
  <name>SCM Sample Project</name>
  <url>http://somecompany.com</url>
  <scm>
    <connection>scm:svn:http://somerepository.com/svn_repo/trunk</connection>
    <developerConnection>scm:svn:https://somerepository.com/svn_repo/trunk</developerConnection>
    <url>http://somerepository.com/view.cgi</url>
  </scm>
  ...
</project>
```

Maven will use the information embedded in the SCM configuration to determine the used SCM provider (implementation) and the remote repository URL. The SCM configuration URL is composed of different information that define the mapping:

```unknown
   scm:svn:http://somerepository.com/svn_repo/trunk
   <service name>:<scm implementation>:<repository url>
```

Check the [Maven SCM list](http://maven.apache.org/scm/scms-overview.html) for the list of supported SCMs.

# Committing and updating changes through Maven

Assuming that SCM has been configured in the `pom.xml` and the project directory is managed by an SCM, invoking the checkin goal in the SCM will start the commit process for all configured sources in your `pom.xml`.

**The files should be added beforehand by an external scm client.**

```unknown
  mvn -Dmessage="<commit_log_here>" scm:checkin
```

for update

```unknown
  mvn scm:update
```

# Specifying the SCM connection to use

There two possible SCM connections that can be used in the `pom.xml`, connection and developerConnection.

- connection configuration

    ```unknown
    <project>
      ...
      <build>
        [...]
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-scm-plugin</artifactId>
            <version>${project.version}</version>
            <configuration>
              <connectionType>connection</connectionType>
            </configuration>
          </plugin>
          ...
        </plugins>
        ...
      </build>
      ...
    </project>
    ```

- developerConnection configuration

    ```unknown
    <project>
      ...
      <build>
        ...
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-scm-plugin</artifactId>
            <version>${project.version}</version>
            <configuration>
              <connectionType>developerConnection</connectionType>
            </configuration>
          </plugin>
          ...
        </plugins>
        ...
      </build>
      ...
    </project>
    ```

# Related Links

- [Overview of SCMs](../scms-overview.html)
