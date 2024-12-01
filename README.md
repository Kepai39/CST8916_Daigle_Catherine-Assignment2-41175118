# Assignment 2





## Scenario Description:
The Rideau Canal Skateway is a canal in Ottawa that, throughout the winter season, allows the public to skate onto the ice within the canal, making it one of Ottawa's largest skating attractions. The issue is that given global warming and the skateway being a canal, the National Capital Commission (NCC) must determine whether the canal is safe enough for the general public to skate on or close the canal for the rest of the winter. This issue can be solved by placing IoT sensors within the canal to measure the following:

Ice Thickness (in cm) - ensures the public does not fall into thin ice. Surface Temperature (in Celsius) - To determine if the ice is melting Snow Accumulation (in cm) - Cannot skate in heavy snow and weakens the ice structure. External Temperature (in Celsius) - Determines in general if it is safe to skate in the canal outside.

The analysis and processing of this data and visualizing the data in an understandable way will notify the NCC to determine the appropriate measures taken to open the canal without risking public safety. With IoT sensors, information about the canal's condition can be determined in real-time, allowing the NCC to make specific conditions when opening the canal. For example, the NCC may open the canal on a day they deem safe and close the canal the next day, or they may close certain sections of the canal they deem too unsafe for the public. With real-time monitoring, the NCC can issue warnings or determine appropriate actions with sudden weather changes.



## System Architecture Diagram:






## Implementation Details:


### IoT Sensor Simulation:

![Sensor Simulation Output Screencap 1](./screenshots/sensor-simulation1.png)

The main flow of the above app is in the `main` method. There is a `while` loop set to true, so that it continues to run indefinitely. Inside that loop is an array of locations. For each location in one iteration of the `while` loop, numbers are generated for our json variables. The json and an index number is sent to the `sendToIotHub()` method, which handles sending of the data to the endpoints specified in the below:

![Sensor Simulation Endpoints](./screenshots/sensor-simulation2.png)

This .env file is part of a virtual environment where we define our environment variables. See the below screenshot for where we define our array with connection strings, and how we use the passed `data` and index (`conn`) to send the message.

![Sensor Simulation Code 1](./screenshots/sensor-simulation3.png)

This code was referenced/adapted from Avirup Basu, who made a detailed explanation via [YouTube](https://www.youtube.com/watch?v=JEffAb_3DlE) and [GitHub](https://github.com/avirup171/python-iot-hub-sender). What was added was reading frrom environment variables, our json output, and extra loops to handle the different endpoints.

### Azure IoT Hub Configuration:

#### Screenshots:

Creating the IoT hub and resource group, configuring name, region and Free tier (for cost friendly) with 8,000 daily message limit.
![Creating IoT Hub](https://github.com/Kepai39/CST8916_Daigle_Catherine-Assignment2-41175118/blob/main/screenshots/IoTHubScreenshot1.png)
Configuring Network connectivity to be Public accesss.
![Creating IoT Hub](https://github.com/Kepai39/CST8916_Daigle_Catherine-Assignment2-41175118/blob/main/screenshots/IoTHubScreenshot2.png)
Default settings, No device update and no windows Defender for IoT
![Creating IoT Hub](https://github.com/Kepai39/CST8916_Daigle_Catherine-Assignment2-41175118/blob/main/screenshots/IoTHubScreenshot3.png)
Review of the creation of IoT Hub.
![Creating IoT Hub](https://github.com/Kepai39/CST8916_Daigle_Catherine-Assignment2-41175118/blob/main/screenshots/IoTHubScreenshot4.png)






### Azure Stream Analytics Job:


Querying Azure Stream Analytics

A basic query can be:

```SQL
SELECT * INTO [jsonstorage1] FROM [IoThubs1]
```

Which shows us rows full of our sensor data, unfiltered.

![Analytics Stream Query](./screenshots/analytics-stream1.png)




### Azure Blob Storage:


## Usage Instructions:


## Results:

## Reflection:

