
folder("Terraform-Active") {
    displayName("Terraform-Active")
    description("Folder contining Terraform-Active Jobs")
  }
freeStyleJob('Terraform-Active/Terraform-Active-Feature-Branch') {
  description("Job to run Terraform-Active feature branch jobs")
  parameters {
    choiceParam {
        name("action")
        description("TF action to take")
        choices(['plan', 'apply', 'destory'])
    }
    stringParam {
        name("provisioner")
        defaultValue("")
        description("name of the provisioner to deploy")
        trim(true)
    }
    stringParam {
        name("branch")
        defaultValue("dev")
        description("Active feature branch to run")
        trim(true)
    }
    stringParam {
        name("modules_repo_branch")
        defaultValue("dev")
        description("Modules feature branch to run")
        trim(true)   
    }
    stringParam {
        name("extra_args")
        defaultValue("")
        description("Extra arguments to add to TF call, ie -destroy when doing plan")
        trim(true)
    }
  }
  //End of paramters

  scm {
    git {
        remote {
            name ("terraform-active")
            url('git@github.com:entrup/terraform-active.git')
            credentials('jenkins-github-ssh')
        }
        branch '*/$branch'
        extensions {
            cleanAfterCheckout()
        }
    }
  }
  steps {
    shell (
        '''
        #Checkout modules repo 
        set -xe 
        git config --file=.gitmodules submodule.modules.branch ${modules_repo_branch}
        git submodule sync 
        git submodule update --init --recursive --remote 
        # End modules repo checkout

        tf_args="-input=false -no-color" 

        #do init before anything 
        ./run_terraform -p $provisioner init $tf_args
        ./run_terraform -p $provisioner plan $tf_args $fixed_args $extra_args
        
        if [ "$action" != "plan" ]; then
           echo "Action is Apply or Destory"
           fixed_args="-auto-approve"
           ./run_terraform  -p $provisioner "$action" $tf_args $fixed_args $extra_args 
        fi
        '''
    )
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
