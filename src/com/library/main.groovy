#!/usr/bin/groovy
package com.library

def start(Map jobArgs = null) {
    print "success"
    node () {
        stage("checkout") {
            checkout scm
            print pwd()
            sh "ls -l"
        }
        stage("build") {
            def tech = scanRepoTechnology()
            executeBuild(tech)
        }
    }
}

def executeBuild(String technology) {
    switch (technology) {
        case "java":
            print "Gotcha!!! Its Java. Performing the Java build"
    }
}

String scanRepoTechnology() {
    if(isFileExists("pom.xml")) {
        return "java"
    }
}

boolean isFileExists(String fileName) {
    def file = new File(fileName)
    return file.exists()
}



