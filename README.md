# Trade Upload And Processing

This project is for tutorial purposes.<br />

<h2>Description & Feature</h2>
<ul>
  <li>Implements a multi-part way to upload large csv files</li>
    <li>Used Reactive Sprign libraries and MemoryMappedFiles to use very little memory when
        processing large files and little GC.
    </li>
  <li>Single file upload. Shown implementaion using <code>Mono < FilePart ></code></li>
  <li>Two test files are included in this project - products.csv and trade.csv, both in the resources folder</li>
</ul>
<br/>

<h3>Steps</h3>
<ul>
  <li><h4>Upload a trade file</h4>
  
  <ol>
    <li>Build the project using : mvn clean install 
    </br>
     Uses Jdk 17.0.2
   </li>
      </li>  
      <br>
        c:\jdk-17.0.2
 <br>
        java -version
        java version "17.0.2" 2022-01-18 LTS
 <br>
        Java(TM) SE Runtime Environment (build 17.0.2+8-LTS-86)
 <br>
        Java HotSpot(TM) 64-Bit Server VM (build 17.0.2+8-LTS-86, mixed mode, sharing)
 <br>
 <br>
        mvn -version
 <br>
        Apache Maven 3.8.1 (05c21c65bdfed0f71a2f2ada8b84da59348c4c5d)
 <br>
        Maven home: c:\apache-maven-3.8.1\bin\..
 <br>
        Java version: 17.0.2, vendor: Oracle Corporation, runtime: c:\jdk-17.0.2
 <br>
        Default locale: en_GB, platform encoding: Cp1252
 <br>
        OS name: "windows 11", version: "10.0", arch: "amd64", family: "windows"
 <br>
 <br>
    </li>

<h3>How to upload a file to test this code</h3>
    <li>Open Postman to access the api of this project</li>
    <li>Set url at<br /><code>http://localhost:8080/api/vi/enrich</code> and use POST method</li>
    <li>From body tab select <code>form-data</code></li>
    <li>Select type <code>File</code> for a key and name the key as <code>file</code></li>
    <li>In value section choose a file from your PC by browsing to upload. IN this example , select trades.csv. See screenshot in resources folder of the code</li>
    <li>Alternatively you can use curl like this.
        Start the projects main class : MultipartFileUploadApplication
        Go to the the command prompt.
        Make sure the file trades.csv is in that folder OR copy it from the resources folder to THIS folder
        you are in right now.
        Then run: curl --request POST --form file=@trade.csv http://localhost:8080/api/v1/enrich
  </ol>

</li>
</ul>

<h3>Things that can be improved</h3>
 <li>Use Streams to process the data that comes in the file upload</li>
 <li>Spark has streaming methods that can be used to stream process the file data</li>
 <li>We can upload the whole file onto disk storage and then use the memory mapped code incouded
to process this file and lookup in the products data as we read line by line.
Look at the class ProcessTradeData included that shows how this can be done.</li>
<li>
The trade data can be split into chunks dn each can be uploaded and processed
in parallel then the results joined. Completeable Futures and threads can be 
used to do this.
</li>
<h3>Note</h3>
<li>There are a few tests included that tests the core functionality of looking up the trade product ids
and the memory mapped file processign code. I need to add tests for the RESTful api side.
</li>

<li>
When building the code with mvn clean install, ALL the tests wil run, one throws an exception by design
and this is logged by log.error so we test this too. 
The error is caught but this is expected behaviour as one of the dates in the data has been
made invalid to test this feature of processing data with errors.
</li>
<br />
Author's Profiles:
<ul>
  <li><a href="https://www.linkedin.com/in/ashish-patel-95850310">LinkedIn</a></li>
</ul>
