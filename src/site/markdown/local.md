---
title: SCM Implementation: File System
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
# SCM Implementation: File System

## General Info

Link: -

License: -

## SCM URL

For all URLs below, we use a colon \(:\) as separator. If you use a colon for one of the variables \(e.g. a windows path\), then use a pipe \(|\) as separator.

```
scm:local<delimiter>path_to_repository<delimiter>module_name
```

_path\_to\_repository_: The absolute or relative path to the parent directory of your pom.xml

_module\_name_: The name of the directory that contains your pom.xml

## Examples

```
scm:local:/usr/modules:my_module
scm:local|C:/javaprojects|my_module
```

