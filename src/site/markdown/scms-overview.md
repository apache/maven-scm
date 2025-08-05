---
title: Overview of SCMs
author: 
  - Wim Deblauwe
date: 2005-12-01
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
# Overview of SCMs

## Standard SCM Providers

These SCMs are supported with their providers shipping with maven-scm

|   |   |   |
|:---:|:---:|:---:|
|**SCM**|**Provider ID**|**Provider Module**|**Native Java**
|[Git](./git.html)|`git`|[Git Executable Provider](./maven-scm-providers/maven-scm-providers-git/maven-scm-provider-gitexe/index.html)|no
|[Git](./git.html)|`jgit`|[JGit Provider](./maven-scm-providers/maven-scm-providers-git/maven-scm-provider-jgit/index.html)|yes
|[Subversion](./subversion.html)|`svn`|[SVN Executable Provider](./maven-scm-providers/maven-scm-providers-svn/maven-scm-provider-svnexe/index.html)|no
|[Mercurial](./mercurial.html)|`hg`|[Mercurial \(Hg\) Provider](./maven-scm-providers/maven-scm-provider-hg/index.html)|no
|[Local](./local.html)|`local`|[Local Provider](./maven-scm-providers/maven-scm-provider-local/index.html)|yes

## 3rd Party SCM Providers

- [maven-scm-provider-svnjava](https://github.com/olamy/maven-scm-provider-svnjava): Native Java SVN provider based on SVNKit

## Related Links

- [Comparison of revision control software](https://en.wikipedia.org/wiki/Comparison_of_revision_control_software)
- [SCM Common vocabulary](https://en.wikipedia.org/wiki/Source_Code_Management#Common_vocabulary)
- [Outdated Wiki page of Maven SCM Support Matrix](https://cwiki.apache.org/confluence/display/MAVENSCM/SCM+Matrix)