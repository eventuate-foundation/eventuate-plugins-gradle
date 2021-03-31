
class GitBranchUtil {

  static def executeCommand(command) {
      def lastLine = ""
      def proc = command.execute()
      proc.in.eachLine { line -> lastLine = line }
      proc.err.eachLine { line -> println line }
      proc.waitFor()
      lastLine
  }

  static def isPlatform(project) {
      project.name == "eventuate-platform-dependencies" || isPlatformSubmodule(project)
  }

  static def isPlatformSubmodule(project) {
      project.name.endsWith("-bom")
  }

  static def gitBranch() {
      executeCommand("git rev-parse --abbrev-ref HEAD")
  }

  static def getWipPublishingVersion(project) {
      def suffix = gitBranch().substring("wip-".length()).replace("-", "_").toUpperCase()
      return project.version.replace("-SNAPSHOT", "." + suffix + ".BUILD-SNAPSHOT")
  }

  static String determineRepoType(String branch) {
      if (branch ==~ /.*RELEASE$/)
          return "release"
      if (branch ==~ /.*M[0-9]+$/)
          return "milestone"
      if (branch ==~ /.*RC[0-9]+$/)
          return "rc"
      return null;
  }

  static boolean isRelease() {
    return "release" == determineRepoType(gitBranch())
  }

  static String getRemote() {
    return executeCommand("git remote get-url origin")
  }

  static String getenv(name) {
    def v = System.getenv(name)
    if (v == null)
      println("Not defined: ${name}")
    return v
  }

}
