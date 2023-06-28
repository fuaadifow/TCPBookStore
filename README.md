# develop a 3-tier TCP-based networking
multi-threaded client-server application to consult a database about books.
The application features a client that offers a JavaFX based graphical user
interface to request the service and communicate with a intermediate server
providing a query specific service. The server, located in the local host, will
be composed of two parts or classes; one being the main server which attend
request as they arrive on an infinite loop, and the server’s service provider
that are created to attend each service. It is the server’s service provider
which does connects to the database using JDBC, retrieves the outcome of
the query and sends back the outcome to the client.
