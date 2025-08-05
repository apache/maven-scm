---
title: Introduction
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
# Maven SCM Plugin

The SCM Plugin offers vendor independent access to common scm commands by offering a set of command mappings for the configured scm. Each command is implemented as a goal.

## Goals Overview

The SCM Plugin has the following goals:

- [scm:add](./add-mojo.html) - command to add file
- [scm:bootstrap](./bootstrap-mojo.html) - command to checkout and build a project
- [scm:branch](./branch-mojo.html) - branch the project
- [scm:changelog](./changelog-mojo.html) - command to show the source code revisions
- [scm:check-local-modification](./check-local-modification-mojo.html) - fail the build if there is any local modifications
- [scm:checkin](./checkin-mojo.html) - command for commiting changes
- [scm:checkout](./checkout-mojo.html) - command for getting the source code
- [scm:diff](./diff-mojo.html) - command for showing the difference of the working copy with the remote one
- [scm:edit](./edit-mojo.html) - command for starting edit on the working copy
- [scm:export](./export-mojo.html) - command to get a fresh exported copy
- [scm:list](./list-mojo.html) - command for get the list of project files
- [scm:remove](./remove-mojo.html) - command to mark a set of files for deletion
- [scm:status](./status-mojo.html) - command for showing the scm status of the working copy
- [scm:tag](./tag-mojo.html) - command for tagging a certain revision
- [scm:unedit](./unedit-mojo.html) - command to stop editing the working copy
- [scm:update](./update-mojo.html) - command for updating the working copy with the latest changes
- [scm:update-subprojects](./update-subprojects-mojo.html) - command for updating all projects in a multi project build
- [scm:validate](./validate-mojo.html) - validate the scm information in the pom
## Usage

General instructions on how to use the SCM Plugin can be found on the [usage page](./usage.html). Some more specific use cases are described in the examples given below.

In case you still have questions regarding the plugin&apos;s usage, please feel free to contact the [user mailing list](./mailing-lists.html). The posts to the mailing list are archived and could already contain the answer to your question as part of an older thread. Hence, it is also worth browsing/searching the [mail archive](./mailing-lists.html).

If you feel like the plugin is missing a feature or has a defect, you can fill a feature request or bug report in our [issue tracker](./issue-management.html). When creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. For this reason, entire debug logs, POMs or most preferably little demo projects attached to the issue are very much appreciated. Of course, patches are welcome, too. Contributors can check out the project from our [source repository](./scm.html) and will find supplementary information in the [guide to helping with Maven](http://maven.apache.org/guides/development/guide-helping.html).

## Examples

To provide you with better understanding on some usages of the Maven SCM Plugin, you can take a look into the following examples:

- [Bootstrapping using a POM file](./examples/bootstrapping-with-pom.html)
- [Other advanced scm commands](./examples/scm-advance-features.html)
