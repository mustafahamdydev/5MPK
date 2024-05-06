![image](https://github.com/mustafahamdydev/5MPK/assets/80783716/2959fabb-05c8-40be-a09d-992e94d26551)


# 5MPK!

## Project Description
5MPK is a comprehensive Android application, developed in Kotlin, that facilitates multi-modal public transport route 
planning within the Greater Cairo Region. The application assists users in identifying the most efficient route from
one location to another using Cairo’s public transport system. It provides valuable information such as the estimated
total travel cost, travel time, and a visual representation of the route on Google Maps, complete with all stops, 
transfer points, and walking distances.

## How it works
The application employs a customized A* path-finding algorithm, developed in Python, which selects the optimal path
based on factors such as distance (impacting travel time) and the number of transfers (affecting cost). The algorithm’s
backend runs on-device using Chaquopy. The application integrates Google Maps APIs to display the route and calculate
the estimated travel time. For monetization, the app incorporates Google AdMob to display ads. Additionally, it uses
Firebase for handling user authentication and account management.

### The Backend Python Algorithm
The centerpiece of the project is a bespoke path-finding algorithm implemented using the python programming language, 
meticulously designed for the specific task of mapping public transportation routes. This algorithm is an exemplification
of the harmonious integration of various computational components, each contributing to the overall functionality of the 
system. The first component is the Haversine Heuristic Function, a pivotal element in the algorithmic design. This function
leverages the Haversine formula, a well-established method in spherical trigonometry, to compute the great-circle distance,
or the shortest distance over the earth’s surface, giving an “as-the-crow-flies” distance between two points. By taking the
latitude and longitude of two nodes as input, it returns the distance in meters. This heuristic is instrumental in the A* 
algorithm, providing an estimate of the distance between a given node and the goal, thereby guiding the path search towards 
the most promising directions. The second component is the adaptation of the A* Search Algorithm. A* is a widely recognized 
path-finding algorithm in the field of Artificial Intelligence, known for its performance and accuracy. the implementation 
incorporates a unique feature to handle bus transfers. The algorithm maintains a priority queue of nodes to explore, with 
the priority of each node determined by a cost function. This cost function is the sum of the cost to reach the node from 
the start node and the heuristic distance from the node to the goal. The algorithm also keeps track of the number of 
transfers at each node, adding a penalty to the cost if a transfer occurs. This penalty is a crucial aspect of the design, 
as it reflects the real-world inconvenience and time cost associated with bus transfers. The final component is the Find 
Routes Function. This function takes the output path from the A* algorithm and identifies the buses that service these points.
It iterates over the path, and for each stop, it finds the bus trip that covers the most subsequent stops in the path. These 
stops, along with the route ID and short name of the bus trip, are then added to an enhanced path. This process continues 
until all stops in the path have been covered.

### The Application
The application is built using Android Kotlin, which provides a rich set of pre-built UI components and tools for mobile 
application development. Kotlin, which provides a reactive programming paradigm, runs natively on Android devices, offering
unmatched speed, reliability, and features compared to other technologies such as Flutter or React Native. In addition to the
basic Android Kotlin development library, the application uses the Chaquopy Python library to run backend Python algorithms 
natively on users’ Android phones and interact with the Kotlin base code and classes of the application. In the backend, Python
is used for its extensive built-in libraries, ease of use, and speed in algorithm development and testing. Python libraries are
utilized such as pandas, networkx, geopy, heapq, pickle, and shapely. The application also uses a suite of Google Cloud APIs, like
Google Maps SDK, Google Places API, Google Geocoding API, Google Geolocation API, and Google Directions API, which are used extensively
throughout the application. For authentication, Google Cloud Firestore API and Firebase are used, which, depending on the user’s 
login/signup preference, will be used to save users' profile data. 

## Development Challanges
Our journey in developing this application was not without its challenges, the most significant of which was dealing
with Google’s frequent policy changes and updates. Despite being a global tech giant with a talented workforce,
Google’s multiple policy shifts during our three-month development period posed considerable hurdles. These changes
affected our use of their APIs, introduced bugs through five updates to Android Studio, and led to the deprecation of
numerous functions and classes. Google’s Android development environment can be complex and bloated, with many functions
performing similar tasks and libraries often conflicting with each other. We found Gradle to be particularly cumbersome,
and Jetpack Compose is a complex way for UI creation that should be removed. Despite these challenges, we chose to build
the app using Kotlin, as it is the most efficient language for Android app development in 2024, with Java being outdated
and Flutter Dart proving to be disorganized and cumbersome.

In addition to the challenges posed by Google’s erratic policy changes, we were severely hindered by the absolute inadequacy
of the Egyptian Government. The struggle to find usable public transport datasets in Egypt was a nightmare. Without the 
General Transit Feed Specification (GTFS), our application, which is designed to alleviate the daily commuting chaos for 
Egyptians, would have been a pipe dream. The current regime in Egypt has shown a staggering lack of progress in these areas,
and it often feels like they are actively stifling opportunities for individuals or groups to contribute to such improvements.
This has exacerbated the difficulties faced by Egyptians in their daily commute, turning it into a veritable hell. It’s a disgrace
that in this day and age, citizens are left to fend for themselves due to the government’s abject failure to provide basic services.

## Screenshots from the app

![App Screens 1](https://github.com/mustafahamdydev/5MPK/assets/80783716/7bea6f9c-4bda-4bc3-80dc-7b3cb41e57cc)


![App Screens 2](https://github.com/mustafahamdydev/5MPK/assets/80783716/f8888b87-25c0-4c7d-b919-2098cf7e0937)
