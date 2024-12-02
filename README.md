# CST8916 - Assignment 2

| Name        | Student # |
| ---------------- | --------- |
| Catherine Daigle | 41175118  |
| Rae Ehret        | 040907812 |

## Scenario Description

The Rideau Canal Skateway is a canal in Ottawa that allows the public to skate on its surface throughout the winter season, making it one of Ottawa's largest skating attractions. The issue is that given global warming and the skateway being a canal, the National Capital Commission (NCC) must determine whether the canal is safe enough for the general public to skate on or to close the canal for the rest of the winter. This issue can be solved by placing IoT sensors within the canal to measure the following:

* Ice Thickness (in cm) - Ensures the public does not fall into thin ice.
* Surface Temperature (in Celsius) - To determine if the ice is melting
* Snow Accumulation (in cm) - Cannot skate in heavy snow and weakens the ice structure.
* External Temperature (in Celsius) - Determines in general if it is safe to skate in the canal outside.

The analysis and processing of this data as well as visualizing it in an understandable way will notify the NCC and help determine the appropriate measures to be taken, the goal being to open the canal without risking public safety. With IoT sensors, information about the canal's condition can be determined in real-time, allowing the NCC to determine specific conditions on when to open the canal. For example, the NCC may open the canal on a day they deem safe and close the canal the next day due to poor conditions, or they may close certain sections of the canal they deem too unsafe for the public. With real-time monitoring, the NCC can issue warnings or determine appropriate actions with sudden weather changes.

---

## System Architecture Diagram

![System architecture Diagram](RTAssignment2ArchitectureDiagram.drawio.png)

The architecture diagram above is fairly simple and shows the relationship of the IoT devices along with Azure Cloud services. The IoT devices send their data via a connection string to IoT hub which is then sent to Azure Stream Analytics for processing. When the data is finished processing, it is then sent to Azure Blob storage where it is stored in containers.

---

## Implementation Details

### IoT Sensor Simulation

![Sensor Simulation Output Screencap Output](./screenshots/sensor-simulation1.png)

The main flow of the above app is in the `main` method. There is a `while` loop set to true, so that it continues to run indefinitely. Inside that loop is an array of locations. For each location in one iteration of the `while` loop, numbers are generated for our json variables. The json and an index number is sent to the `sendToIotHub()` method, which handles sending of the data to the endpoints specified in the below:

![Sensor Simulation Endpoints](./screenshots/sensor-simulation2.png)

"The most important parameter of *[the Device on Iot Hub]* here, is the primary connection string." (Basu, 2023). A device can be 'named' and connection strings/keys generated through the IoT Hub in the Azure Portal. The generated primary connection string should be then added to an environment variable, or in the `.env` file as shown in the previous screenshot.

This .env file is part of a virtual environment where we define our environment variables. See the below screenshot for where we define our array with the fetched environment variables (connection strings), and how we use the passed `data` and index (`conn`) to send the message.

![Sensor Simulation Code - send to IoT Hub code](./screenshots/sensor-simulation3.png)

Each iteration of the inner loop pauses for 10 seconds after sending a message to IoT Hub. Further development of this program might consider externalizing the sleep/wait time to an environment variable for further configuration.

For the sensors, the JSON payload is separated into ice thickness, surface temperature, snow accumulation, and external temperature. We use `data=json.dumps(msgData)` which turns our `msgData` object full of generated variables into a json string and passes it to the method `sendToIotHub()`. That method sends the json payload to IoT Hub. This data is sent every 10 seconds with new generated data to alternating locations for as long as the program is running. Each location has a dedicated connection string set in the environment variables. Despite being one application, it treats the messages for each location as individual messages using different connections, as if it were different devices.

![Sensor Simulation Code JSON](./screenshots/JsonPayload.png)

### Azure IoT Hub Configuration

The Configuration Steps for Iot Hub are demonstrated in the screenshots that are below [Usage instructions](#usage-instructions) and within [Creating IoT hub](#creating-azure-iot-hub). For the configurations, we chose to pick the most cost-effective options such as the free tier option. For Networking, we picked a public network as using a private network, although secure, can complicate the process. 

IoT Hub Overview:
![Creating IoT Hub - overview](./screenshots/IoToverview.png)

Creating an IoT hub Device. This process is repeated two more times to create a total of three devices, one for each region:
![Creating IoT Hub Device - creating the device](./screenshots/CreatingIoTdevice.png)

IoT connection Strings (below). These endpoints are used within the `.env` file of the IoT simulation code to connect simulated devices to Azure IoT hub. The important connection used is the Primary Connection String. Using the Python code as before, after the connection is made, the messages are then routed to the IoT hub.

![Creating IoT Hub Device - connection strings and keys](./screenshots/IOTConnectionString.png)

### Azure Stream Analytics Job

Review of Stream Analytics job settings:
![Creating Stream Analytics Job - review](./screenshots/StreamAnalyticsJobReview.png)

Here (below), the input is created. We picked Messaging as the endpoint and JSON as our input format since the simulation IoT devices output JSON. The source of the input is the IoT hub, therefore Stream Analytics retrieves and processes the data in IoT Hub.

![Creating Stream Analytics Job input](./screenshots/CreatingStreamInput.png)

The **Output** is created. Here it is linked to the storage container 'jsonstorage1'. We picked this name originally since it stores the data in JSON file format, but a more accurate name would have been 'IceWarningLogs' as later that container was used to store logs that showed the ice was not safe to skate on. The organization format is in array form as it is much easier to parse the information.

![Creating Stream Analytics Job Output](./screenshots/CreatingStreamOutput.png)

#### Querying Azure Stream Analytics

A basic query can be:

```SQL
SELECT * INTO [jsonstorage1] FROM [IoThubs1]
```

This shows us rows full of our sensor data, unfiltered.

![Analytics Stream Query - default query](./screenshots/analytics-stream1.png)

Changing the Analyzation screen query. Using this query specifies that only 'Unsafe' ice conditions are to be logged and stored into the container. Unsafe conditions are as follows: Ice Thickness is < 5, Surface Temperature > 0, Snow Accumulation > 3, or External Temperature > 0. We chose '*temp* > 0' degrees celsius as an unsafe condition for both external and surface temperatures since ice melts above the freezing point.

![Creating Stream Analytics Job Query](./screenshots/AnalyzingStreamSQLQueryWarn.png)

### Azure Blob Storage

Creating A Storage Account and picking standard Azure blob Storage Account and locally redundant storage:
![Creating Storage Account - basics](./screenshots/CreatingStorageBasic.png)

Kept the advanced settings as default:
![Creating Storage Account - advanced](./screenshots/CreatingStorageAdvanced.png)

Enabled public access to networking, for ease of use and not to encounter networking issues:
![Creating Storage Account - networking](./screenshots/CreatingStorageNetworking.png)

Disabled soft delete, as this is a test resource and makes deleting resources afterwards easier:
![Creating Storage Account - data protection](./screenshots/CreatingStorageDataProt.png)

Default Encryption settings:
![Creating Storage Account - encryption](./screenshots/CreatingStorageEncryption.png)

Created Blob Storage container, default settings:
![Creating Storage Account - overview of the created container](./screenshots/container-storage1.png)

The message payload is processed into container storage as 'JSON in an array'. At the time, our naming convention of the container that contained the processed data was 'jsonstorage1'. We chose this naming convention due to the fact that the container would be used to store JSON files only. However, a more appropriate name for the container would be "iceWarningLogs" since we opted to have the container store log file data of times where the Canal Skateway is too dangerous for the public to skate on.

---

## Usage Instructions

### Running the IoT Sensor Simulation

Follow the following steps to set up and run the simulation for the three IoT devices:

1. In the project root folder, create a `.env` file with the following fields:
    * `IOTHUB_CONN_STR1`
    * `IOTHUB_CONN_STR2`
    * `IOTHUB_CONN_STR3`
    * Each field should have the IoT Hub connection string from one of three Created Devices in IoT Hub, for example:
        ```
        IOTHUB_CONN_STR1 = "HostName=IoThubs1.azure-devices.net;DeviceId=iotsensor1;SharedAccessKey=<string>"
        ```
2. Open a terminal and navigate to the project directory. Run: `python -m venv .venv`
3. `.venv\scripts\activate` to activate the virtual environment (if not already activated), which will have your 3 environment variables from step 1.
4. `pip install -r requirements.txt` to install the requirements (2).
5. `py sensor-simulation/sensor.py` to run the program.

### Configuring Azure Services

#### Creating Azure IoT Hub

The basic tab of IoT hub creation, picking free tier:
![Creating IoT Hub - basics](./screenshots/IoTHubScreenshot1.png)

Picking public access networking:
![Creating IoT Hub - networking](./screenshots/IoTHubScreenshot2.png)

Shared Access policy + RBAC:
![Creating IoT Hub - management](./screenshots/IoTHubScreenshot5.png)

No defender or updates:
![Creating IoT Hub - addons](./screenshots/IoTHubScreenshot3.png)

Review of IoT hub:
![Creating IoT Hub - eview and create](./screenshots/IoTHubScreenshot4.png)

Creating an IoT hub Device. This process is repeated two more times to create a total of three devices, one for each region:
![Creating IoT Hub Device - creation screen](./screenshots/CreatingIoTdevice.png)

Result of three IoT devices:
![Creating IoT Hub Device - list of 3 devices](./screenshots/ListOfIoTDevices.png)

#### Creating Azure Stream Analytics Job

Creating a New Stream Analytics Job. We picked '1/3' in streaming units for cost effectiveness:
![Creating Stream Analytics Job](./screenshots/AnalyticsJobBasic.png)

Connecting Stream Analytics Job to the appropriate storage account 'blobstoragecan'. The creation of the storage account is shown in the [Azure Blob Storage Section](#azure-blob-storage):
![Creating Stream Analytics Job](./screenshots/CreatingStreamAnalyticsJob.png)

Here, the input is created. We picked 'Messaging' as the endpoint and JSON as our input format since the IoT device simulation outputs JSON:
![Creating Stream Analytics Job input](./screenshots/CreatingStreamInput.png)

Here, the output is created. Here it is linked to the storage container 'jsonstorage1'. We picked this name originally because its contents are of JSON format, however a more accurate name would have been 'IceWarningLogs' as later that container was used to store logs that showed the ice was not safe to skate on. The organization format is in array form as it is much easier to parse the information:
![Creating Stream Analytics Job Output](./screenshots/CreatingStreamOutput.png)

Result of the Stream output/input creation:
![Creating Stream Analytics Job Output](./screenshots/StreamOutputResult.png)
![Creating Stream Analytics Job input](./screenshots/StreamInputResult.png)

### Accessing Stored Data

Before retrieving the results, I had to create diagnostic settings to **Run** the Stream analytics job:
![Creating Storage Account](./screenshots/AnalyzingStreammOverview.png)

After 10 seconds, the Stream Job outputted a JSON file to the 'jsonstorage1' container:
![Creating Storage Account](./screenshots/StorageJSONFileOutput.png)

---

## Results

Stream Analytics. Receiving simulated messages:
![Simulated messages](./screenshots/StreamAnalyzeOverviewMessaging.png)

Confirming that the data has been processed into a JSON array format:
![Creating Storage Account](./screenshots/TheArrayFormatofJsonFileOutput.png)

After the testing of the IoT devices and the assignment, we deleted our resources:
![Deletion of resources](./screenshots/DeletionOfResources.png)

---

## Reflection

In this assignment we learned a lot about the development process and the specifics about navigating Azure's environment to set up a pipeline from IoT Device (Thing) to Cloud.

There were challenges from the beginning. Our initial approach posed challenges in availability of libraries (Java) and restrictions because of our choice in build automation tools. We were forced to rethink our approach, and ended up going with a language that in retrospect is a lot more suited to development of cloud-interacting applications like the sensor simulation. The resulting simulation code (Python) is so much more lightweight and faster to set up than our original approach with Java. With Java, there was difficulty in navigating the Azure IoT Java SDK as the samples did not come with detailed enough explanations as to how to use its various mechanisms, such as making a connection. Furthermore, our choice in building tool gave us issues that compounded our frustration. Switching to Python, our application code shrunk to 1/4th the size, and importing dependencies was made simple with a `requirements.txt` and `pip` command. Furthermore, there is much more documentation with Python, highlighting the lesson that research into appropriate developing languages is a worthwhile step to cloud development.

More challenges were brought forth when navigating the Azure Portal. The Azure portal is highly structured, but with the abundance of menus and the particular requirements of certain services like Analytics Stream requiring a storage container and diagnostic settings set first, it was clear that we needed guidance as to the order of creation along with the settings.

There are some noteworthy resources available for those who need a step-by-step walkthrough of doing exactly what we did in this assignment. Please see the References section if curious.

When we finally started the batch job to process the "data that indicates sub-par skating conditions" and everything moved along smoothly, we reflected on the process and what we would do differently if we were to do this again.

1. **More research into programming languages** and available documentation within the communities/projects.
2. **Naming conventions are important.** We kept our naming conventions to "function + id number" such as `jsonstorage1`, because its function was to host json data. But this isn't an appropriate naming convention for bigger projects, as it says nothing of its purpose to store specifically *processed* data and warnings about sub-par skating conditions.
3. **Create the step-by-step setup plan**, including order of creation for services. This may require gathering documentation or video tutorials, but it saves on time when the target service is something never used before.

In the end, it was a valuable experience with practical lessons to be learned.

---

## References

* Basu, A. (2023, August 31). Sending simulated data to IoT hub using Python. YouTube. https://www.youtube.com/watch?v=JEffAb_3DlE 
* Vidiv Academy. (2023, December 26). Azure IoT Hub Project with Stream Analytics | Streaming Data Azure Data Engineering Project. https://www.youtube.com/watch?v=oxD6nEAUXuA
