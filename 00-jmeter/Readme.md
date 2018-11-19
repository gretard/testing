# Performance testing with JMeter #
## Pre-requisites ##
You need to:

1. Download and install Java JDK from [http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html "http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html")
2. Download Wiremock from http://wiremock.org/docs/running-standalone/
3. Download and extract JMeter binary zip from https://jmeter.apache.org/download_jmeter.cgi
4. Download JMeter plugins from [https://jmeter-plugins.org/downloads/file/JMeterPlugins-Standard-1.4.0.zip](https://jmeter-plugins.org/downloads/file/JMeterPlugins-Standard-1.4.0.zip "https://jmeter-plugins.org/downloads/file/JMeterPlugins-Standard-1.4.0.zip")
## Exercises ##
### Pre-requisites ##
0. Extract JMeter, extract plugins zip
1. Copy wiremock jar to 00-jmeter folder
2. Open Command prompt and navigate to 00-jmeter folder
3. Start Wiremock using the following command ( or you can use *run.cmd* file):
	> java -jar wiremock-standalone-2.19.0.jar --root-dir wiremockSetup --no-request-journal
4. In the browser you can check if it is working [http://localhost:8080/Services/Test/1](http://localhost:8080/Services/Test/1 "http://localhost:8080/Services/Test/1")
5. Start JMeter (bin/jmeter.bat)
 
### 1. Creating a basic test plan ###
In this task you will create a basic test plan simulating 1 user hitting your site for 120s. Steps:

1. Right click on the test plan, select **Add -> Config Element->HTTP Request Defaults**, enter:
   - **Port**: 8080
   - **Server Name or IP**: localhost
   - **Path**: /Services/Test/1
2. Right click on the test plan, select **Add -> Listener -> Summary Report**
3. Right click on the test plan, select **Add -> Listener -> View Results Tree**
3. Right click on the test plan, select **Add -> Threads(Users)->Thread Group**, enter:
	- **Duration**: 120 s
	- **Ramp-up-period**: 10s
	- Select **Forever** check-box
4. Right click on the Thread Group, select **Add -> Sampler -> HTTP Request**
5. Save your test plan as **ex1.jmx**
6. Click play
7. Click on **Summary Report** and/or **View Results Tree** 


### 2. Running through the command line ###
You will be running test plan saved in the previous task using the command line. Steps:

1. Open command prompt and run with updated path to your JMeter:
	> "apache-jmeter-3.3\bin\jmeter.bat" -Jjmeter.save.saveservice.output_format=csv -n -t "ex1.jmx" -l "ex1_results.jtl" -e -o "ex1_htmlreport"
2. After test finished, you can go and see results at **ex1_htmlreport** folder

### 3. Generating more load using different thread groups and timers

Your task is to check if application will be able to handle 10800 requests per hour. You will need to simulate 3 requests/s rate (10800/3600).


#### Ex. 3.1 Using simple thread group ####

If your request takes around 1.5s then you need each second around 5 working threads (3*1.5). Steps:

1. Open **ex1.jmx** test plan
2. Disable  **View Results Tree** by right-clicking on it and selecting **Disable**
2. Save it as **ex3_1.jmx**
3. Update thread count to 5.
4. Save
2. Run test
3. Check in the **Summary Report** if you managed to reach 3 requests/s throughput 

#### Ex. 3.2 Using Constant Throughput Timer ####
In this task you will use more threads, but you will limit throughput so that required number of requests rate is reached. Steps:

1. Open  **ex3_1.jmx**
2. Save it as **ex3_2.jmx**
3. Update number of threads to 10 
4. Right click on the HTTP Request, select **Add -> Timer -> Constant Throughput Timer** , set:
   - Target throughput to 180 as you want to send 3 requests per second, making 180 requests per minute
   - Calculate based on: all active threads
5. Save
6. Run test
7. Check in the **Summary Report** if you managed to reach 3 requests/s throughput 


#### Ex. 3.3 Adding random timer ####
In this task, you will add random pauses between requests so that load would be more distributed. If your request takes around 1.5s, and you add around 1s random time between your requests then you need each second around 8 working threads (3*(1.5+1)). Steps:

1. Open  **ex3_1.jmx**
2. Save it as **ex3_3.jmx**
3. Update number of threads to 8 
4. Right click on the HTTP Request, select **Add -> Timer -> Gaussian Random Timer** , set:
   - **Constant delay**: 1000
   - **Deviation**: 500
5. Save
6. Run test
7. Check in the **Summary Report** if you managed to reach 3 requests/s throughput 
