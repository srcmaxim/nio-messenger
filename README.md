#NIO-CHAT

A chat server and client implementation using the Java NIO package. 
Implements the event-based selector [Reactor Pattern](http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf) 
which is a neat way to avoid creating a new thread for every client connection. 

As a result this will scale much better than the traditional thread-per-connection approach.

##1 Reactor pattern and selector

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
- [java nio](ttp://gee.cs.oswego.edu/dl/cpjslides/nio.pdf)

##2 Requirements

###2.1 Server

###2.2 Client

##3 Protocols

###3.1 Server protocol

Создается серверный сокет на `HOST`, `PORT` указанный в Properties.
Конфигурируется для не блокируюдей работы. В случае неисправности будет выполнен выход из сервера.
Cелектрор регистрирует серверный сокет на прием новых соединений `OP_ACCEPT`.

``` java
while (running) {
    selector.select();
    for (Iterator i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
        SelectionKey key = (SelectionKey) i.next();
        if (key.isAcceptable()) {
            accept(key);
        }
        if (key.isReadable()) {
            read(key);
        }
    }
}
```

Далее пока программа не остановленна, будет выполнятся следующая итерация:

1. Выбор селектора:  `selector.select()`, если события не происходят -- поток блокируется
2. Итерация по всем selectedKeys (и последующее их удаление). Для каждого key происходит проверка на состояние:
    1. Если состояние `acceptable`, -- сервер принимает соединение на конкретный сокет и регистрирует его в selector, 
а также в списке `clientChannels` (необходимо для последующей рассылки сообщений).
Сокет конфигурируется для не блокируюдей работы.
Cелектор регистрирует серверный сокет на прием новых соединений `OP_READ`.
    2. Если состояние `readable`, -- сервер читает сообщение присланное сокетом.
        ``` java 
        buffer.clear();
        while (cc.read(buffer) > 0) {
            buffer.flip();
            request.append(new String(buffer.array(), buffer.position(),
                    buffer.limit(), Properties.CHARSET));
            buffer.clear();
        }
        ``` 
        Если сообщение не читается -- сокет не исправен и удаляется из списка пользователей `clientChannels.remove(cc)`.
Сообщение делится на команду (доступные команды выбираются из перечисления) и текст.
На основе этой команды происходит выбор действия [Client actions][1].
    3. Для каждого сокета из списка serverSocket происходит запись информации.
Если сообщение не читается -- сокет не исправен и удаляется из списка пользователей `i.remove()`.

###3.2 Client protocol

##4 Client actions:
1. `LOGIN` сообщение регистрации (1-е сообщение пользователя)
2. `LOGOUT` сообщение дерегистрации (последнее сообщение пользователя)
3. `SEND` отправка сообщения

##5 Example of communication between two users (Server log)

__SERVER__
>SERVER LOG: Server started and ready for handling requests </br>
SERVER LOG: Maxim:  LOGGED </br>
SERVER LOG: SEND Maxim: Where is Dasha? </br>
SERVER LOG: Dasha:  LOGGED </br>
SERVER LOG: SEND Maxim: Dasha, hi! </br>
SERVER LOG: SEND Dasha: Hi Maxim! How are you? </br>
SERVER LOG: SEND Maxim: I'm fine, buy </br>
SERVER LOG: Maxim:  UNLOGGED </br>
SERVER LOG: Dasha:  UNLOGGED </br>
 
__CLIENT 1__
>CLIENT LOG: Connecting to localhost on port 6001 </br>
CLIENT LOG: Connected! </br>
CLIENT LOG: Sending message: LOGIN Dasha: </br> 
CLIENT LOG: Server response -> Dasha: LOGGED </br>
CLIENT LOG: Server response -> Maxim: Dasha, hi! </br>
CLIENT LOG: Sending message: SEND Dasha: Hi Maxim! How are you? </br>
CLIENT LOG: Server response -> Dasha: Hi Maxim! How are you? </br>
CLIENT LOG: Server response -> Maxim: I'm fine, buy </br>
CLIENT LOG: Server response -> Maxim: UNLOGGED </br>
CLIENT LOG: Sending message: LOGOUT Dasha: </br>
CLIENT LOG: Server logout! </br>

__CLIENT 2__
>CLIENT LOG: Connecting to localhost on port 6001 </br>
CLIENT LOG: Connected! </br>
CLIENT LOG: Sending message: LOGIN Maxim: </br> 
CLIENT LOG: Server response -> Maxim: LOGGED </br>
CLIENT LOG: Sending message: SEND Maxim: Where is Dasha? </br> 
CLIENT LOG: Server response -> Maxim: Where is Dasha?  </br>
CLIENT LOG: Server response -> Dasha: LOGGED </br>
CLIENT LOG: Sending message: SEND Maxim: Dasha, hi! </br>
CLIENT LOG: Server logout!  </br>