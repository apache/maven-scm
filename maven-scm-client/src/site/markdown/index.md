---
title: Introduction
author: 
  - Vincent Siveton
date: 2008-08-14
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
# Maven SCM Client

The Maven SCM client is a simple SCM command line tool.

## Usage

```unknown
# java -jar target\maven-scm-client-1.1-jar-with-dependencies.jar
Usage: maven-scm-client <command> <working directory> <scm url> [<scmVersion> [<scmVersionType>]]
scmVersion is a branch name/tag name/revision number.
scmVersionType can be 'branch', 'tag', 'revision'. The default value is 'revision'.
```

## Example

```unknown
# java -jar target\maven-scm-client-1.1-jar-with-dependencies.jar checkout c:\temp\maven-scm-client scm:svn:http://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-client
[INFO] Executing: cmd.exe /X /C "svn --non-interactive checkout http://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-client maven-scm-client"
[INFO] Working directory: c:\temp
Checked out these files:
 maven-scm-client\src\main\java\org\apache\maven\scm\client\cli\MavenScmCli.java
 maven-scm-client\src\main\resources\META-INF\plexus\components.xml
 maven-scm-client\src\main\bash\maven-scm-update
 maven-scm-client\src\main\bash\maven-scm-checkin
 maven-scm-client\src\main\bash\maven-scm-checkout
 maven-scm-client\src\site\site.xml
 maven-scm-client\pom.xml
```

