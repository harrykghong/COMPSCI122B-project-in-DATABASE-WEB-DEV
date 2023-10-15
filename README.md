- # General
    - #### Team#: 99

    - #### Names: Zhihao Hong，Zhihao Cheng

    - #### Project 5 Video Demo Link:
    https://youtu.be/d_CNJHbTk2g
    - #### Instruction of deployment:

    - #### Collaborations and Work Distribution:
    Task1:Zhihao Cheng
    Task2:Zhihao Hong
    Task3:Zhihao Hong
    Task4: Together

- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      - #### AddmovieServlet
      - #### AddstarServlet
      - #### CheckoutServlet
      - #### SingleMovieServlet
      - #### StarsServlet
      - #### AutoCompleteServlet
      - #### EmployeeLoginServlet
      - #### IndexServlet
      - #### LoginServlet
      - #### SingleStarServlet
      - #### TopServlet

    - #### Explain how Connection Pooling is utilized in the Fabflix code.
    In contex.xml we configured the connection pooling of our two backend database as follows:
    <Resource name="jdbc/moviedb2"
              auth="Container"
              driverClassName="com.mysql.cj.jdbc.Driver"
              factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
              maxTotal="100" maxIdle="30" maxWaitMillis="10000"
              type="javax.sql.DataSource"
              username="mytestuser"
              password="My6$Password"
              url="jdbc:mysql://172.31.2.76:3306/moviedb?autoReconnect=true&amp;allowPublicKeyRetrieval=true&amp;useSSL=false&amp;cachePrepStmts=true"/>

    There three lines configured Connection Pooling: "factory="org.apache.tomcat.jdbc.pool.DataSourceFactory", 
                                                     "maxTotal="100" maxIdle="30" maxWaitMillis="10000"". and
                                                     "url="jdbc:mysql://172.31.2.76:3306/moviedb?autoReconnect=true&amp;allowPublicKeyRetrieval=true&amp;useSSL=false&amp;cachePrepStmts=true"/>"

    - #### Explain how Connection Pooling works with two backend SQL.
    Initialization: When our application starts, it creates a pool of connections for each database. 
    Request Handling: When a user request comes in that needs to communicate with Database A, instead of establishing a new connection, one of the existing connections from the pool of connections for Database A is used. Same for Database B.
    Connection Release: After the database operation is finished, the connection is not closed but is returned back to the pool.

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
      - #### Master：AddmovieServlet, AddstarServlet, CheckoutServlet, SingleMovieServlet, StarsServlet
      - #### Slave: AutoCompleteServlet, EmployeeLoginServlet, IndexServlet, LoginServlet, SingleStarServlet, TopServlet
    
- #### How read/write requests were routed to Master/Slave SQL?
    - #### We changed some datasource in servlet to visit the slave, and we keep all the servlets that have write requests in the master.

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
        - #### When using terminal use py log_processing.py loginfo.txt (loginfo2.txt)

- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | !["\img\singlehttp1thread.png"](path to image in img/)   | 49                         | 26.04                               | 25.91                     | One threads is much faster           |
| Case 2: HTTP/10 threads                        | !["\img\singlehttp10threads.png"](path to image in img/)   | 147                        | 125.05                              | 124.95                    |Ten threads is 5 times longer than one thread            |
| Case 3: HTTPS/10 threads                       | !["\img\singlehttps10threads.png"](path to image in img/)   | 145                        | 123.33                              | 123.21                    |Time Close to HTTP           |
| Case 4: HTTP/10 threads/No connection pooling  | !["\img\singlehttp10threadsHTTPNoConnectionPooling.png"](path to image in img/)   | 142                        | 122.96                              | 122.83                    |Connection Pooling has few effect on time efficiency           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | !["\img\scaled1thread.png"](path to image in img/)   | 50                         | 26.41                               | 26.25                     | One threads is much faster           |
| Case 2: HTTP/10 threads                        | !["\img\scaled10threads.png"](path to image in img/)   | 132                        | 109.42                              | 109.29                    | Ten threads is 4 times longer than one thread           |
| Case 3: HTTP/10 threads/No connection pooling  | !["\img\scaled10threadsNoConnectionPooling.png"](path to image in img/)   | 132                        | 108.17                              | 108.04                    | Connection Pooling has few effect on time efficiency           |