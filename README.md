# Lab: Distributed Sorting Algorithm Implementation

### Report filename: *Team2_LabAssignment_DistributedSortingApplication.pdf*

A distributed sorting application that processes records from the sortbenchmark.org. Our application is designed for the Indy sort benchmark category which means it sorts 100 byte records with 10 byte keys. The system is consists of 3 parts: processing
initial data, sending and receiving data between processing nodes, and sorting and merging.


 Usage:
 
 Start master node with a command
 ```
 %app% own.ip.address ownport master nodes_count
 ```
 for example
 ```
 java Main 1.2.3.4 8080 master 2
 ```
 
 A master will start, ready to save worker addresses. Next start worker nodes with
 ```
 %app% own.ip.address ownport ownindex master.ip.address masterport input/file/path output/file/path
```
for example
```
java Main 11.12.13.14 8181 0 1.2.3.4 8080 /data/chunk.0.data ./sorted.0.data

java Main 21.22.23.24 8282 1 1.2.3.4 8080 /data/chunk.1.data ./sorted.1.data
```

After the specified number of nodes connect to the master, sort will be performed, and resulting data will reside in specified files.

See the report for the details.
