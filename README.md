# Executable Fetcher

Lets you retrieve, cache and execute arbitrary variants of executables through Gradle.

## Simple example

### List available executables
```kotlin
plugins {
    id("de.hanno.executablefetcher")
}

executableFetcher {
    registerExecutable(de.hanno.executablefetcher.executables.builtin.helm, "3.11.3")
}

[...]


./gradlew listExecutables --info

> helm - foo/executablefetcher/helm/windows/amd64/3.12.0/windows-amd64/helm.exe
> helm - foo/executablefetcher/helm/windows/amd64/3.11.3/windows-amd64/helm.exe
```

### Execute command ad-hoc
```kotlin
plugins {
    id("de.hanno.executablefetcher")
}

[...]

./gradlew execute --executable=helm --args=version

> [...]
> :execute (Thread[Execution worker for ':',5,main]) started.

> version.BuildInfo{Version:"v3.12.0", GitCommit:"c9f554d75773799f72ceef38c51210f1842a1dea", GitTreeState:"clean", GoVersion:"go1.20.3"}
```

### Execute a prepared command
```kotlin
plugins {
    id("de.hanno.executablefetcher")
}

tasks.named("executeHelm", de.hanno.executablefetcher.tasks.ExecuteTask::class.java) {
    args = "version"
}

[...]


./gradlew executeHelm --info

> [...]
> :executeHelm (Thread[Execution worker for ':',5,main]) started.

> version.BuildInfo{Version:"v3.11.3", GitCommit:"323249351482b3bbfc9f5004f65d400aa70f9ae7", GitTreeState:"clean", GoVersion:"go1.20.3"}
```

## Features

* _Builtin executables_: For some executables, there are builtin definitions. That only means that the plugin knows
  how to download those executables for some operating systems and some architectures. For all builtin executables,
  there is a default execution Task generated like *executeHelm* or *executeGit* that uses some default version. Take
  a look at the examples here to find out how to override args and versions.
* _Operating system independent automation_: Gradle has support for different operating systems. Using it as a gateway
  to access executables, you can have all your tooling operating system independent, as long as the exe has support for
  a specific os.
* _Task for ad hoc execution of a command_: Executes an arbitrary known executable with some provided args.
* _Custom executables_: The plugin extension provides a way to define custom executables. Here you can also pass in
  your own implementation of an executable. You only need to provide some names and implement 
  `fun resolveDownloadUrl(variant: Variant): URL` and benefit from some more or less cool interfaces and types, 
  automatic downloading, automatic caching etc :)
* _Minimal dependencies_: This plugin has only a single runtime library dependency - commons compression.
  That means you won't run into dependency conflicts on your gradle build classpath 
  (as long as Kotlin's std library remains backwards compatible :P) and if you do, commons compression can be expected
  to be solid and backwards compatible.

## Rationale

### The distribution problem

Distributing executables is a big burden for their creators. There are countless package managers
across all the operating systems - ranging from more stable and secured repositories like the ones from linux distributions
to less strict managers like brew, over special solutions from certain ecosystems like npm. And we have those
curl-a-shell-file-and-pipe-to-cmd variants that for example got very popular in the cloud native world.

Especially for the latter ones, it's hard to keep versions of executables aligned across the team or keep the tooling
in line with the used/needed versions.

When Gradle is present in the tool stack, it can be very convenient to just use a plugin (this very plugin here ...) to
retrieve the needed executable, write a gradle task using Java's process apis using that executable and put it into the
task graph where the rest of your automation is already defined.

### The version management problem

Managing multiple versions of an executable locally is a big burden for the developers, but often not an option but a necessity.
There is a ton of custom tooling, often one for each executable - like nvm, java-alternatives or hand rolled symlinks
on your local machine for executables that don't bring any tooling with them.

What if we use a convention based interface how executables are saved, use it through gradle tasks, and then it works for all tools
that you have to use, no matter if they support versions somehow or not? With multiple versions at the same time?
In you infrastructure automation gradle project, in your service gradle project, in whatever gradle project you want.

### The execution problem

I have never worked in a team where everyone used the same operating system. How often was tons of tooling created and
collapsed like a house of cards as soon as the typical Windows user showed up, eager to participate? And no,
subsystem for linux doesn't cut it, there's always problems with that. So it would be nice to have a small abstraction
over the way the actual command is written, so that it works on all the operating systems ootb.
Normally solved with some fancy shell magic no one wants to maintain, in your gradle 
project (or in plain Java, Kotlin, you name it), you can do that with the language you are used to, or simply use
the helper methods provided (in the future) in this project.
