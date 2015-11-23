# The RESTFUL module 

The module provides a restful api, tha facilitates the usage of GraphWalker for any other
tool or programming language that kan do HTTP.

| Name          | Type | Description |Input | Output |
|:------------- |:-----|:------------|:-----|:----|
| load          | PUT  | Upload model(s) to the service |The format is json, using the GW3 notation |A json string with **result: ok**, or an error mesage|
| hasNext       | GET  | Query the service if the execution of the model(s) is done. | No indata | A json string with hasNext resturning true or false, or an error message |
| getNext       | GET  | Retrieve the next element to be executed. | No indata | A json string with the next element to execute, or an error message |
| getData       | GET  | Get the value of a specific data | The name of the attribute (key) | A json string with the value of the attribute |
| setData       | PUT  | Executes a java script statement | The javas script statement | A json string returning **result: ok**, or an error message |
| restart       | PUT  | Restarts the service | No indata | A json string returning **result: ok**, or an error message |
| fail          | PUT  | Fails the execution | The reason for the failure as a string |A json string returning **result: ok**, or an error message |
| getStatistics | GET  | Retreives statistics of the execution| No indata |A json string with current execution statistics , or an error message |


## load

|||
|:------------ |:-----|
|**Name**|load|
|**Type**|PUT|
|**Description**|Will upload model(s) to the service. It will replace anything previously loaded in the service.|
|**Input**|A json string, using the GW3 notation.|
|**Input example**||
```json
{
  "name":"Name of the model/models scenario",
  "models":[
    {
      "name": "A Model Name",
      "id": "5ddf6a4d-fcca-4d22-a4ce-556fe63038d7",
      "generator":"random(edge_coverage(100))",
      "startElementId": "e0",
      "vertices": [
        {
          "id": "v1",
          "name": "v_BrowserStarted"
        }
      ],
      "edges": [
        {
          "id": "e0",
          "name": "e_init",
          "actions": [
            " num_of_books \u003d 0;",
            " MAX_BOOKS \u003d 5;"
          ],
          "targetVertexId": "v1"
        }
      ]
    }
  ]
}
```
|||
|:------------ |:-----|
|**Output**|A json string with **result: ok**, or an error mesage|
|**Output example**||
```json
{  
  "result":"ok"
}
```
