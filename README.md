# Collection Utils
[![Build Status](https://api.travis-ci.org/baba-shinde/collection-utils.svg?branch=master)](https://travis-ci.org/baba-shinde/collection-utils)

## BlockingArrayBlockingQueue
This is an extension of [ArrayBlokingQueue](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ArrayBlockingQueue.html). Specially to be used along with [Executor Service](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html) implementation.

### Introduction
During daily purpose we do use Executor Service's implementations i.e. ThreadPoolExecutor to process tasks using pooled threads. One of the argument for ThreadPoolExecutor is BlockingQueue [This is to park tasks until pooled threads picks up]. This implementation i.e. BlockingArrayBlockingQueue focuses on using this Blocking Queue's implementation for ThreadPoolExecutor. 

### What is it
ThreadPoolExecutor's `execute(Runnable/Callable arg)` method internally pushes task on BlockingQueue by invoking `offer()`. Offer call is not blocking in nature. `Offer()` call puts task at the tail of the Queue only if space available and return true. It returns false if task is not added to Queue. ThreadPoolExecutor throws [RejectionExecutionException](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/RejectedExecutionException.html) when it is not able to add element to underlying Queue. 
This can be big issue where producer is faster than Consumer. Here producer is caller thread and Consumer is pooled thread. 
To solve this I have used Delegation Pattern, a wrapper class to ArrayBlockingQueue which calls `put()` [blocking call] on invocation of `offer()`.

### Where/when to use
This BlockingArrayBlockingQueue to be used when you are not really sure about behavior of producers, that includes;
* No of Producers
* Rate at which tasks/messages are going to come
* Average size of tasks

### Why not to use Rejection Execution Handler
[RejectionExecutionException](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/RejectedExecutionException.html) is one way of handling such tasks which do not find place to reside in Queue. But Question remains open are we going to put message back to executor? or run it in independent thread? Look at section below;
* Submit again to Executor
Bad Idea !! This will lead you to [StackOverFlowError](http://docs.oracle.com/javase/8/docs/api/index.html?java/lang/StackOverflowError.html) [as it will continue submission of such tasks to executor and soon stack will overflow] so not recommended. 
```java
RejectedExecutionHandler handler = (final Runnable r, final ThreadPoolExecutor executor) -> {
	LOGGER.warn("Inside Exception Handler !!, submitting again !!");
	//lets try
	executor.submit(r);
};
```
Note: One might want to wait and then try to submit request to executor, still this will not ensure guarantee to you. Here we are also blocking the current thread who is invoking submit request to executor.
* Running with an independent thread [outside pooled threads]
Not very good idea !! This will defeat the purpose of using Executors ad we are spawning separate thread to process such tasks. This can be harmful as we loose control over no of threads in JVM.
```java
RejectedExecutionHandler handler = (final Runnable r, final ThreadPoolExecutor executor) -> {
	LOGGER.info("Inside Exception Handler !!, submitting again !!");
	Thread t = new Thread(r);
	t.start();
};
``` 

### Why/when not to use unbounded LinkedBlockingQueue
Unbounded [LinkedBlockingQueue](http://docs.oracle.com/javase/8/docs/api/index.html?java/util/concurrent/LinkedBlockingQueue.html) can also be used to hold tasks for Executor. but this will cost you JMV's heap. Even this will start behaving the same as of ArrayBlockingQueue on reaching its max limit i.e. `Integer.MAX`

### Benefits of BlockingArrayBlockingQueue
* Solution for fast producer and slow consumer problem. 
* Application does not have any heap memory issue (because of Executor)
* Its better to halt producer when your Queue's limit is reached [however Queue size can be mentioned carefully] rather than arranging inviable options to process.

**`Note: Look at BlockingArrayBlockingQueueTest where all behaviors mentioned above are demonstrated`**