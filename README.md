# Lab: Distributed Sorting Algorithm Implementation

### Report filename: *Team2_LabAssignment_DistributedSortingApplication.pdf*

A distributed sorting application that processes records from the sortbenchmark.org. Our application is designed for the Indy sort benchmark category which means it sorts 100 byte records with 10 byte keys. The system is consists of 3 parts: processing
initial data, sending and receiving data between processing nodes, and sorting and merging.


 Usage:
 ```
 %app% own.ip.address ownport master nodes_count
 
 or
 
 %app% own.ip.address ownport master.ip.address masterport input/file/path
```
