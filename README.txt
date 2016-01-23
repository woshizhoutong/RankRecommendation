Intruction:

This application has one Interface - RankRecService, which is implemented by RankRecServiceImpl. The class RankRecServiceImpl is the main class in this application, and getRankedRecommendations() is the major method and the only public method except for main(). This method sends out three different types of request, including searching, getting recommendation and getting reviews. The return type of getRankedRecommendations() is List<ProductItemModel>.

The multi-threading and thread safety are the most challenging parts. If we don’t use multi-threading to request reviews, it will cost a lot of time. 
For a single call, searching must be finished before getting recommendation, and getting recommendation must be done before getting reviews. Thus, the multi-thread happens in the getting review requests. We can send 5 requests per second according to Walmart API requirement. I use ExecutorService to send the requests batch by batch (5). 

When considering multi requests from the client side. I used the ring buffer idea to pause the request if they come in too fast. Basically, I have an array of size 5 to record most recent request. If the time difference between the oldest request and the newest request is shorter than 1 second, I pause the application until 1 second, and then allow additional request to be sent.

In order to present the output, a model class called ProductItemModel is defined. It shows only itemId, name, and review rating score of this item. The review rating score is calculation as rating/range, in case ranges are not the same for all items. The Comparable interface is implemented by this model class. The natural order is descending since we want the items with higher scores appear at the top.

For the test, I used Junit test. I tested several exceptions, and two special cases. I also tested the multi-thread requests.

The following libraries are used:

commons-io-2.4.jar
hamcrest-core-1.3.jar
json-simple-1.1.1.jar

junit-4.11.jar


Further consideration:
Memory control should be considered if time allows. For example, 100 clients sends requests in a short period of time. Even though we can pause the application and wait all the requests get responded one after one, the memory can kill the application.



How to use this app: (Windows)

1. Put the application at C:/

2. Go the the directory of the application:

cd c:\RankRec

3. Compile the java files with jars:

javac -cp lib/json-simple-1.1.1.jar;lib/commons-io-2.4.jar;lib/junit-4.11.jar src/com/tong/rankrec/*.java


4. Go to directory of "src": 

cd src

5. Run the main class RankRecServiceImpl. Here I use the argument "ipod" as an example: 

java -cp .;../lib/json-simple-1.1.1.jar;../lib/commons-io-2.4.jar;../lib/junit-4.11.jar com.tong.rankrec.RankRecServiceImpl ipod


6. Run the Test TestRunner:

java -cp .;../lib/json-simple-1.1.1.jar;../lib/commons-io-2.4.jar;../lib/junit-4.11.jar;../lib/hamcrest-core-1.3.jar com.tong.rankrec.TestRunner











