#!/usr/bin/groovy
package com.library

def run(args) {
    try{
        String lang = 'java'
        println("Executing java build....")
        sh """
           cd java
           mvn clean install
        """
        libraryUtils.generateSonarPropertiesFile(args,lang)
        libraryUtils.runSonarAnalysis(lang)

    }catch(e){
        print(e.getMessage())
        return false
    }
    return true
}