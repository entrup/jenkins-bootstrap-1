folder("Bootstrap") {
    displayName("Bootstrap")
    description("Folder contining Jenkins Bootstrap Job")
  }
  freeStyleJob('Bootstrap/seed-job') {
    displayName('Initial Seed Job')
    description('Job to create Jenkins jobs, pipelines, folders etc')
    scm {
      git {
        remote {
          url('git@github.com:entrup/jenkins-bootstrap.git')
          credentials('jenkins-github-ssh')
        }
        branch('master')
        extensions {
          cleanBeforeCheckout()
        }
      }
    }
    steps {
        jobDsl {
            targets(['jobs/*.groovy'
            ].join('\n'))
            removedJobAction('DELETE')
            ignoreMissingFiles(true)
        }
    }
    publishers {
      cleanWs {
        cleanWhenAborted(true)
        cleanWhenFailure(true)
        cleanWhenNotBuilt(true)
        cleanWhenSuccess(true)
        cleanWhenUnstable(true)
      }
    }
  }
