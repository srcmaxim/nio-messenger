# NIO-CHAT

A chat server and client implementation using the Java NIO package. 
Implements the event-based selector [Reactor Pattern](http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf) 
which is a neat way to avoid creating a new thread for every client connection. 

As a result this will scale much better than the traditional thread-per-connection approach.

## Reactor pattern and selector

A few words about selector, channels, and non-blocking operations:

>_Java NIO: Channels and Buffers_
In the standard IO API you work with byte streams and character streams. 
In NIO you work with channels and buffers. Data is always read from a channel 
into a buffer, or written from a buffer to a channel.

>_Java NIO: Non-blocking IO_
Java NIO enables you to do non-blocking IO. For instance, a thread can ask a channel to 
read data into a buffer. While the channel reads data into the buffer, the thread can do 
something else. Once data is read into the buffer, the thread can then continue processing it. 
The same is true for writing data to channels.

>_Java NIO: Selectors_
Java NIO contains the concept of "selectors". A selector is an object that can monitor 
multiple channels for events (like: connection opened, data arrived etc.). Thus, a 
single thread can monitor multiple channels for data.

Implementation of Reactor (Selector) pattern.
The reactor design pattern was introduced as a general architecture to implement 
event-driven systems. In order to solve our original problem of implementing a server 
application that can handle thousands of simultaneous client connections, 
Reactor pattern provides a way in which we can listen to the events (incoming 
connections/requests) with a synchronous demultiplexing strategy, so that when an 
incoming event occurs, it is dispatched to a service provider (handler) that can handle this event.
Handle identifies resources that are managed by the operating system, such as network connections, 
open files, etc. Handles are used by demultiplexer to wait on the events to occur on handles.

Demultiplexer works in synchronous mode to waiting on the events to occurs on the handlers. 
This is a synchronous blocking behavior, but this only blocks when we do not have events 
queued up at the handles. In all other cases, when there is an event for a given handle, 
demultiplexer notifies the initiation dispatcher to call-back the appropriate event handler.
A very common realization of a demultiplexer in Unix is the ‘select(.)’ system call, 
which is used to examine the status of file descriptors in Unix.

Links:
- [reactor pattern](http://kasunpanorama.blogspot.com/2015/04/understanding-reactor-pattern-with-java.html)
- [java nio](http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf)

