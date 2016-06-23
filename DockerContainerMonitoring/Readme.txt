This utility gets the docker container metrics (CPU Util, Memory Util in percentage along with Network and IO usage) and takes container name as parameter. The metrics is fetched by querying Docker API and the same will loaded to MySQL DB. 
Call this utility with frequent defined interval (ex: using ScheduleAtFixedRate from ExecutorService) and this will start monitoring your container of interest.

Dependent libraries:
a) MySQL JDBC connector
b) JsonPath (Rest Assured)