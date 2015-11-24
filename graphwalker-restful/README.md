# The RESTFUL module 

The module provides a restful api, tha facilitates the usage of GraphWalker for any other
tool or programming language that kan do HTTP.

| Name          | Type | Description |Input | Output |
|:------------- |:-----|:------------|:-----|:----|
| load          | POST  | Upload model(s) to the service |The format is json, using the GW3 notation |A json string with **result: ok**, or an error mesage|
| hasNext       | GET  | Query the service if the execution of the model(s) is done. | No indata | A json string with hasNext resturning true or false, or an error message |
| getNext       | GET  | Retrieve the next element to be executed. | No indata | A json string with the next element to execute, or an error message |
| getData       | GET  | Get the value of a specific attribute | The name of the attribute (key) | A json string with the value of the attribute |
| setData       | PUT  | Executes a java script statement | The java script statement | A json string returning **result: ok**, or an error message |
| restart       | PUT  | Restarts the service | No indata | A json string returning **result: ok**, or an error message |
| fail          | PUT  | Fails the execution | The reason for the failure as a string |A json string returning **result: ok**, or an error message |
| getStatistics | GET  | Retreives statistics of the execution| No indata |A json string with current execution statistics , or an error message |


## load

|||
|:------------ |:-----|
|**Name**|load|
|**Type**|POST|
|**Description**|Will upload model(s) to the service. It will replace anything previously loaded in the service.|
|**Input**|A json string, using the GW3 notation.|
|**Input example**|__curl -i -H "Content-Type: text/plain;charset=UTF-8" -X POST -d @petClinic.gw3 http://localhost:8887/graphwalker/__|
|**Output**|A json string with **result: ok**, or an error mesage|
|**Output example**|If the file is loaded ok: __{"result":"ok"}__|
|**Output example**|If the file failed: __{"result":"nok","error":"java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 4 path $"}__|

## hasNext

|||
|:------------ |:-----|
|**Name**|hasNext|
|**Type**|GET|
|**Description**|Query the service if the execution of the model(s) is done.|
|**Input**|No indata|
|**Input example**|Ask the servcie if we have more steps to get: __curl -i  http://localhost:8887/graphwalker/hasNext__|
|**Output**|A json string with **result: ok** and the value of the attribte, or an error mesage|
|**Output example**|__{"result":"ok","hasNext":"true"}__|
|**Output example**|If the attribute is not defined in the context: __{"result":"ok"}__|

## getNext

|||
|:------------ |:-----|
|**Name**|getNext|
|**Type**|GET|
|**Description**|Retrieve the next element to be executed.|
|**Input**|No indata|
|**Input example**|Get the next step: __curl -i  http://localhost:8887/graphwalker/getNext__|
|**Output**|A json string with the next element to execute, or an error message|
|**Output example**|__{"result":"ok","CurrentElementName":"e_StartBrowser"}__|

## getData

|||
|:------------ |:-----|
|**Name**|getData|
|**Type**|GET|
|**Description**|Gets the value of a specific attribute|
|**Input**|The name of the attribute (key), placed on the end of the URL|
|**Input example**|Retrieve value for atrribute x: __curl -i  http://localhost:8887/graphwalker/getData/x__|
|**Output**|A json string with **result: ok** and hasNext set to either true or false. Or an error message|
|**Output example**|__{"result":"ok","hasNext":"true"}__|
|**Output example**|If the attribute is not defined in the context: __{"result":"ok"}__|

## setData

|||
|:------------ |:-----|
|**Name**|setData|
|**Type**|PUT|
|**Description**|Executes a java script statement|
|**Input**|A java script statement|
|**Input example**|Set the value for atrribute x: __curl -i -X PUT http://localhost:8887/graphwalker/setData/x=123;__|
|**Output**|A json string with **result: ok**, or an error mesage|
|**Output example**|__{"result":"ok"}__|

## restart

|||
|:------------ |:-----|
|**Name**|restart|
|**Type**|PUT|
|**Description**|Restarts the service|
|**Input**|No indata|
|**Input example**|Restarts the machine: __curl -i -X PUT http://localhost:8887/graphwalker/restart__|
|**Output**|A json string with **result: ok**, or an error mesage|
|**Output example**|__{"result":"ok"}__|

## fail

|||
|:------------ |:-----|
|**Name**|fail|
|**Type**|PUT|
|**Description**|Fails the execution|
|**Input**|The reason for the failure as a string|
|**Input example**|The the service to fail the execution: __curl -i -X PUT http://localhost:8887/graphwalker/fail/asserTionFailed__|
|**Output**|A json string with **result: ok**, or an error mesage|
|**Output example**|__{"result":"ok"}__|
