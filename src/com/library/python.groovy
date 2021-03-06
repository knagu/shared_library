#!/usr/bin/groovy
package com.library

def run(args) {
    try{
        String lang = 'python'
        stage('UnitTest'){
            print("Executing unit tests for python")
            //sh "cd python"
            sh "python -m unittest discover -s python/test/"
            print("Completed executing unit tests")
        }

        stage('SonarAnalysis') {
            libraryUtils.generateSonarPropertiesFile(args, lang)
            libraryUtils.runSonarAnalysis(lang)
        }

    }catch(e){
        print(e.getMessage())
        return false
    }
    return true
}
