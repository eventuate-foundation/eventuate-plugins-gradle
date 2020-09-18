
class GitBranchUtil {

  static def executeCommand(command) {
      def lastLine = ""
      def proc = command.execute()
      proc.in.eachLine { line -> lastLine = line }
      proc.err.eachLine { line -> println line }
      proc.waitFor()
      lastLine
  }

  static def gitBranch() {
      executeCommand("git rev-parse --abbrev-ref HEAD")
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
  

}
