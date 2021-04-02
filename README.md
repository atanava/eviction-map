Eviction Map
-----------------------
#### Coding assigment:
Implement generic structure  EvictionMap<K, V> that acts as key-value map with following time-based eviction
policy:  expire entries after the specified duration has passed since the entry was created,
or the most recent replacement of the value. Specify duration as EvictionMap constructor parameter.

    EvictionMap methods:                           
    a) Creates value with specified key in this map. If the map previously contained a
    value associated with key, the old value is replaced by value:
                   public void put(K key, V value);
    b) Returns the value associated with key in this map or null if no mapping exists
                   public V get(K key)

#### Example test case:
    1) Initiate map with duration of 10 seconds
    2) Use "put" to insert one key-value item into the map
    3) Use "get" to retrieve it back immediately - value should be still there
    4) wait 10 seconds
    5) Use "get" to retrieve same value again -  it should return null as entry has been evicted internally by map
      (cause eviction duration has already passed for the entry)

#### Things to consider when implementing:
    - thread safety (depends on if/how you rely on threading)
    - avoiding resource leaks
    - the map could be used by long-running application with various usage criterias 
      (frequent put and get, less frequent put and mostly get, frequent put and infrequent get), 
      it should work well under all usage scenarios

#### Additionally:
    - deliver solution as standard Maven or Gradle project, written in Java. 
      It should be easy to just import into any Java IDE and run tests 
    - please don't add any library files or IDE specific files to the project
    - make sure to provide unit tests that use described api and showcase it's functionality
    - add readme file to the project explaining your solution design and major decisions 
    - pay attention to coding style, formatting and general design
    - be ready to discuss your design/implementation choices afterwards

## Solution

### Technologies

- Java 8
- Google Guava
- JUnit 5
- Logback
- SLF4J

### Implementations

* Created interface `EvictionMap` for polymorphism that allows us to use different implementations
* Created implementation based on Google Guava Cache for productivity comparsions
* Implemented business logic in `AbstractEvictionMap` abstract class.  
  This class contains:   
  2 nested classes named as `CompositeKey` and `CompositeValue` which in turn contain `K` key and `V` value with own timestamps,  
  2 collections which present via interfaces : first one is a Map of `K` keys and `CompositeValue`, second is a Deque of `CompositeKey`  
  Deque helps us to get first-added (oldest) keys very fast without searching in all Map for it.
  Method `evictCache` matches timestamps of `CompositeKey`'s and value of `entryLifeTime` variable 
  and does evictions from Map and Deque. 
  If variable `useGC` is "true", then also invokes `runtime.gc()` which offer Garbage Collector to start cleaning and release the memory from evicted objects.
  Variable `batchSize` defines the size of map from which to start eviction.
* Created `TheadUnsafeEvictionMap` implementation which uses thread unsafe collections aka `HashMap` and `LinkedList`.  
  This implementation does evictions in its own thread and consumes fewer size of memory.
* Created `TheadSafeEvictionMap` implementation which uses thread safe collections aka `ConcurrentHashMap` and `ConcurrentLinkedDeque`.  
  This implementation creates and starts a separate daemon thread which does evictions.
* Created factory class `EvictionMapFactory` which helps us to choose and create instances of necessary implementation easier.

### Tests
If you want to get more accurate measurement values of memory consumption, start the test classes separately from Maven,   
because it seems that the garbage collector, which could run before each test, in this case works better (tested with openjdk-15.0.2_linux-x64).  
And also you will receive pretty prints from the logger.  
Cast your EvictionMap instance to AbstractEvictionMap or its inheritor if you want to use specific methods such as `setUseGC` and `setBatchSize`
  