#!/usr/bin/groovy
import java.util.zip.*
/*String scanRepoTechnology() {
    String pDir = pwd()
    if (isFileExists(pDir + "/pom.xml")) {
        return "java"
    }
    if (isFileExists(pDir + "/build.sbt")){
        return "scala"
    }
    if(isFileExists(pDir + "/environment.yml")){
        return "python"
    }
}*/


boolean isFileExists(String fileName) {
    def file = new File(fileName)
    return file.exists()
}

/*
def isGivenDirExists(String dir){
    println(dir)
    sh """
      pwd
      if [ -d $dir];
      then
        echo "$dir exists"
        echo "true" >> commandResult
      else
        echo "$dir doesn't exists"
        echo "false" >> commandResult
      fi
    """
    String strDir = readFile('commandResult').trim()
    return strDir.toBoolean()
}

def isGivenFileExists(String file){
    println(file)
    sh """
      pwd
      if [ -e $file];
      then
        echo "$file exists"
        echo "true" >> commandResult
      else
        echo "$file doesn't exists"
        echo "false" >> commandResult
      fi
    """
    String strFile = readFile('commandResult').trim()
    return strFile.toBoolean()
}
*/

void generateSonarPropertiesFile(args, String lang){
    try{
        println("Generating sonar properties file for sonar" +lang)
        def langMap = [python:  'py', scala: 'scala', java: 'java', impala: 'plsql', hive: 'plsql', oozie: 'xml']
        def langCode = langMap[lang]
        sh"""
           touch sonar-project.properties
           echo "sonar.sources=." >> sonar-project.properties
           echo "sonar.sourceEncoding=UTF-8" >> sonar-project.properties
           echo "sonar.login=177100e9e43dc53ac58f03aca6917ca76262f9f4" >> sonar-project.properties          
           echo "sonar.language=$langCode" >> sonar-project.properties
           echo "sonar.projectKey=${args.repoName}_$lang" >> sonar-project.properties
           #echo "sonar.projectName=${args.repoName}-$lang" >> sonar-project.properties
           
           if [ "${args.lang}" = "java" ];
           then 
              if [ -d "target" ];
              then 
                 echo "sonar.java.binaries=target/classes/**" >> sonar-project.properties
              else
                echo "No Target folder found"   
              fi
           fi                 
        """
        println("Generating sonar properties for " +lang+ "is completed")
    }catch(e){
        println("Error generating sonar properties file")
    }
}

void runSonarAnalysis(String lang){
    def scanner = tool 'SonarScanner'
    withSonarQubeEnv('sonar'){
        println(scanner)
        println("Running sonar analysis for" +lang)
        sh"""
         $scanner/bin/sonar-scanner
         rm sonar-project.properties
        """
    }
    println("Running sonar analysis for "+lang+ "is completed")
}

def init(loadLibVars = null){
    if(loadLibVars != null){
        loadLibraryVars(loadLibVars)
    }
    return this
}

Map loadProperties(loadLibVars){
    def readFile = "{$pwd()}/${loadLibVars}"
    try{
        return readYml(file: "${readFile}")
    }catch(e){
        println("Error loading properties file")
    }
}

def loadLibraryVars(loadLibVars = 'props.yml'){
    props = [:]
    println ("loading props $loadLibVars")
    propsFileName = loadLibVars
    vars('props.yml', loadLibVars)
    loadProperties(loadLibVars)
}
def vars(String name, value){
    addToVars(name,value)
}

def addToVars(String name, value){
    props[name] = value
}

def prepPackage(args){
    def folderName = ''
    def lang = "${args.lang}"
    print(args)
    print(lang)
    if(lang == 'java'){
        print("inside if")
        folderName = 'java_artifact'
    }else if(lang == 'python'){
        print("inside else")
        folderName = 'python'
    }
    print(folderName)
    sh "pwd"
    //sh "ls -ltaR"
    if(isFileExists("${args.repoName}_installer.zip")){
        sh "rm ${args.repoName}_installer.zip"
        String pyargs = "'folderName'='${folderName}' 'artifactName'='${args.repoName}_installer'"
        runScripts('packageUtils.py', pyargs)
        uploadSpec(args)
    }else{
        String pyargs = "'folderName'='${folderName}' 'artifactName'='${args.repoName}_installer'"
        runScripts('packageUtils.py', pyargs)
        uploadSpec(args)
        sh "rm ${args.repoName}_installer.zip"
    }
}

/*
def snapshot(args){
    String zipFileName = "${args.repoName}_installer"
    String inputDir = "${args.lang}"
 //   def outputDir = "zip"

    //Zip files

    ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(zipFileName))  
    new File(inputDir).eachFile() { file -> 
        //check if file
        if (file.isFile()){
            zipFile.putNextEntry(new ZipEntry(file.name))
            def buffer = new byte[file.size()]  
            file.withInputStream { 
                zipFile.write(buffer, 0, it.read(buffer))  
            }  
            zipFile.closeEntry()
        }
    }  
    zipFile.close()
    uploadSpec(args)
}    
*/

def uploadSpec(args){
    def server = Artifactory.newServer url: "http://54.200.162.118:8081/artifactory", credentialsId: "artifactory"
    sh "pwd"
    def uploadSpec =
     """{
      "files": [
           {
             "pattern": "*.zip",
             "target": "example-repo-local/"
           }  
         ]
        }"""

        //server.content-type "text/plain"
        def buildInfo = server.upload spec: uploadSpec
        server.publishBuildInfo buildInfo
}

def notifyAll(String status) {
    String msg = "Status: ${status}\n JOB URL: ${JOB_URL}${BUILD_NUMBER}"
    //mail to: 'srikanth.gunuputi@gmail.com,hema021335@gmail.com', subject: "Build result", body: msg
    office365ConnectorSend message: msg, status: status, webhookUrl: 'https://outlook.office.com/webhook/ab5a92ea-ae03-42dd-9f87-2d1d86048d29@76a2ae5a-9f00-4f6b-95ed-5d33d77c4d61/JenkinsCI/1e829e7444d64c8b9a8f3d7dd9d89497/da11b87b-2856-4a25-afc9-4d2bcc12feba'
    //slackSend baseUrl:'https://hackdevworkspace.slack.com/services/hooks/jenkins-ci/', channel: 'devops', token: '7QUnAnnhglOyF7n0qoH9yiPX', message: msg

}

def runScripts(String fileName, String args=''){
    writeFile(file: "../${fileName}", text: libraryResource("com/library/scripts/${fileName}"))
    sh "chmod +x ../${fileName}"
    String command = "python ../${fileName} ${args}"
    print(command)
    sh command
}