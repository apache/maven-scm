---
title: SCM Implementation: Git
author: 
  - Olivier Lamy
date: 2008-08-10
---

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
# SCM Implementation: Git

## General Info

Link: <https://git-scm.com/>

License: GNU General Public License v2

## SCM URL

The syntax of Git SCM URLs, including different fetch and push URLs, is described in [SCM URLs](https://maven.apache.org/pom-scm-urls.html#Git).

## Working with branches

Since version 1\.3, we assume that the name of the branch in the upstream repo is the same as the name of the current local branch. So whenever you invoke a maven-scm action which has to access the upstream repository, e.g. start a release, you should be on that very branch.

In other words: If no branch is specified manually, every git-fetch, git-pull, git-push, etc will always work on the branch in the upstream repository which has the same branch name as your current local branch

```
git push pushUrl currentBranch:currentBranch
```

## Provider Configuration

The provider configuration is defined in `${user.home}/.scm/git-settings.xml`. For more information see the [reference guide](./maven-scm-providers/maven-scm-providers-git/maven-scm-provider-git-commons/git-settings.html).

