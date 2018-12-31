node {
	def branch = "${env.BRANCH_NAME}".toLowerCase()

	stage('git') {
		/* Changed due to a bug: "${scmBranch} is returning UNKNOW"
		 * (https://github.com/mojohaus/buildnumber-maven-plugin/issues/53#issuecomment-373110568) */
		//checkout scm
		//def jobName = "${env.JOB_NAME}"
		//def repoPath = jobName.substring(0, jobName.lastIndexOf('/'))
		git url: "https://macpersia@bitbucket.org/planty-assistant-devs/planty-prototyping-agent.git", branch: branch
	}

	stage('build') {
		withMaven(jdk: 'jdk-10.0.2', maven: 'maven-3.6.0', mavenSettingsConfig: 'my-maven-settings'/*, tempBinDir: ''*/) {
			sh "mvn deploy -DskipTests -DaltDeploymentRepository=local-snapshots::default::http://repo-nexus-service:8081/repository/maven-snapshots"
		}
	}
}

