#!/usr/bin/groovy
package com.library

/*def execute(userPropFile = 'props.yml'){
  args = [:]
  args.userPropFile = userPropFile 

start(jobArgs)
}*/

def start(Map args) {
    node () {
        stage("Checkout") {
            checkout scm
        }
        stage("MergeConflict"){
            print("Merge conflict stage is in progress")
        }
        stage("Compile") {
            //def lang = libraryUtils.scanRepoTechnology()
            executeBuild(args)
        }
        stage("Package"){
            libraryUtils.prepPackage(args)
        }
        stage("Tagging"){
            print("Tagging stage is in progress")
        }
        stage("Notifications"){
            if(currentBuild.result == null || currentBuild.result.isEmpty()) {
                libraryUtils.notifyAll('SUCCESS')
            }
            else{
                libraryUtils.notifyAll(currentBuild.result)
            }
        }
    }
}

def executeBuild(args) {
    switch (args.lang) {
        case "java":
            print "Gotcha!!! Its Java. Performing the Java build"
            new java().run(args)
            break
        case "scala":
            print "Gotcha!!! Its scala. Performing the scala build"
            new scala().run(args)
            break
        case "python":
            print "Gotcha!!! Its python. Performing the python build"
            new python().run(args)
            break
        case "hive":
            print "Gotcha!!! Its hive. Performing the hive build"
            new hive().run(args)
            break
        case "impala":
            print "Gotcha!!! Its impala. Performing the impala build"
            new impala().run(args)
            break
        case "oozie":
            print "Gotcha!!! Its oozie. Performing the oozie build"
            new oozie().run(args)
            break
        default:
            print "No tech found"
            break
    }
}




