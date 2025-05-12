Skip to content
[Jenkins]Jenkins
1
Luis Santos
log out

    Dashboard
    java-app-staging-deploy
    #25

Status
Changes
Console Output
Edit Build Information
Delete build ‘#25’
Timings
Git Build Data
Open Blue Ocean
Pipeline Overview
Restart from Stage
Replay
Pipeline Steps
Workspaces
Previous Build
FailedFailed
Console Output
Download
View as plain text

Started by user Luis Santos
Obtained Jenkinsfile from git https://github.com/Lmsantos89/exam-ready-be.git
[Pipeline] Start of Pipeline
[Pipeline] node
Running on Jenkins in /var/lib/jenkins/.jenkins/workspace/java-app-staging-deploy
[Pipeline] {
[Pipeline] stage
[Pipeline] { (Declarative: Checkout SCM)
[Pipeline] checkout
Selected Git installation does not exist. Using Default
The recommended git tool is: NONE
No credentials specified
> git rev-parse --resolve-git-dir /var/lib/jenkins/.jenkins/workspace/java-app-staging-deploy/.git # timeout=10
Fetching changes from the remote Git repository
> git config remote.origin.url https://github.com/Lmsantos89/exam-ready-be.git # timeout=10
Fetching upstream changes from https://github.com/Lmsantos89/exam-ready-be.git
> git --version # timeout=10
> git --version # 'git version 2.47.1'
> git fetch --tags --force --progress -- https://github.com/Lmsantos89/exam-ready-be.git +refs/heads/*:refs/remotes/origin/* # timeout=10
> git rev-parse refs/remotes/origin/master^{commit} # timeout=10
Checking out Revision f33f3149a2cb6796c6cae5620768247490703bf9 (refs/remotes/origin/master)
> git config core.sparsecheckout # timeout=10
> git checkout -f f33f3149a2cb6796c6cae5620768247490703bf9 # timeout=10
Commit message: "New Jenkins file"
> git rev-list --no-walk 7c34e86cd7c88912547940c399ccf98178fa7660 # timeout=10
[Pipeline] }
[Pipeline] // stage
[Pipeline] withEnv
[Pipeline] {
[Pipeline] stage
[Pipeline] { (Declarative: Tool Install)
[Pipeline] tool
Checking OpenJDK installation...
[Pipeline] }
[Pipeline] // stage
[Pipeline] }
[Pipeline] // withEnv
[Pipeline] }
[Pipeline] // node
[Pipeline] stage
[Pipeline] { (Declarative: Post Actions)
[Pipeline] cleanWs
Error when executing always post condition:
org.jenkinsci.plugins.workflow.steps.MissingContextVariableException: Required context class hudson.FilePath is missing
Perhaps you forgot to surround the step with a step that provides this, such as: node
at PluginClassLoader for workflow-basic-steps//org.jenkinsci.plugins.workflow.steps.CoreStep$Execution.run(CoreStep.java:90)
at PluginClassLoader for workflow-basic-steps//org.jenkinsci.plugins.workflow.steps.CoreStep$Execution.run(CoreStep.java:71)
at PluginClassLoader for workflow-step-api//org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution.lambda$start$0(SynchronousNonBlockingStepExecution.java:49)
at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572)
at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
at java.base/java.lang.Thread.run(Thread.java:1583)

[Pipeline] echo
Build failed!
[Pipeline] }
[Pipeline] // stage
[Pipeline] End of Pipeline
Also:   org.jenkinsci.plugins.workflow.actions.ErrorAction$ErrorId: 0f4d75b8-23c4-4934-b483-2950cec7ff23
java.lang.IllegalArgumentException: Node Jenkins doesn't seem to be running on RedHat-like distro
at PluginClassLoader for openJDK-native-plugin//org.jenkinsci.plugins.openjdk_native.OpenJDKInstaller.isInstalled(OpenJDKInstaller.java:96)
at PluginClassLoader for openJDK-native-plugin//org.jenkinsci.plugins.openjdk_native.OpenJDKInstaller.performInstallation(OpenJDKInstaller.java:56)
at hudson.tools.InstallerTranslator.getToolHome(InstallerTranslator.java:67)
at hudson.tools.ToolLocationNodeProperty.getToolHome(ToolLocationNodeProperty.java:109)
at hudson.tools.ToolInstallation.translateFor(ToolInstallation.java:221)
at hudson.model.JDK.forNode(JDK.java:150)
at hudson.model.JDK.forNode(JDK.java:60)
at PluginClassLoader for workflow-basic-steps//org.jenkinsci.plugins.workflow.steps.ToolStep$Execution.run(ToolStep.java:157)
at PluginClassLoader for workflow-basic-steps//org.jenkinsci.plugins.workflow.steps.ToolStep$Execution.run(ToolStep.java:138)
at PluginClassLoader for workflow-step-api//org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution.lambda$start$0(SynchronousNonBlockingStepExecution.java:49)
at java.base/java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:572)
at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:317)
at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
at java.base/java.lang.Thread.run(Thread.java:1583)
Finished: FAILURE

REST API
