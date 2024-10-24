# Lab Notebook
### This documentation serves as a comprehensive record of the progress made and lessons learned throughout the semester within the Procrastaint project at RCOS. Designed to aid both current contributors and future members of the RCOS community, this document captures the journey of developing and refining Procrastaint, an open-source project aimed at having users click less to navigate. Users' time should be spent doing tasks, not making them. Creating a new task is as easy as typing. Users write a sentence and the NLP will take the time they want and assigns the task to that time. This handles various formats of times including range based tasks and repeating tasks. From tackling challenging issues to discovering new techniques, each entry in this documentation reflects my commitment to advancing the project and fostering a collaborative environment where knowledge is shared and ideas flourish.

## The issues I have been working on
Future goals for our project:
* Connect to Google/Apple Calendar
* Make a calendar to see all tasks
* Add widgets to quickly add tasks
* Connect with Google Assistant/Siri

Goals I'm contributing to:
* Android user interface (UI) design
* Connect to Google/Apple Calendar
* Ducumentations

## Update 09/27/24
I'm relatively new to working on RCOS projects with a team, and this is also one of my first experiences using GitHub for collaboration. This week, I contributed by helping my group write the proposal for our project, which gave me some insight into the overall direction we’re heading. After that, I started familiarizing myself with the existing codebase, trying to get a better understanding of what's already been developed. Our project is primarily written in Kotlin, which is the language we're using both for building the Android app and handling some of the backend components. Since Kotlin is new to me, I’ve been dedicating some time to learning the language and its features, which has been a challenging but rewarding process. I’ve also set up Android Studio on my laptop, preparing myself to dive into development work later on as I continue getting comfortable with the tools and code structure.


## Update 10/04/24
I’ve been going through the existing code for the Android app in our project, trying to get a clear understanding of what’s already been implemented and how the different components interact. This has been a great opportunity to familiarize myself with the project’s structure and see how various parts of the app work together. Along with that, I’ve been brainstorming ideas for new features I could potentially add to the app, thinking about how I can make meaningful contributions to the project moving forward. 

Additionally, I spent time learning about the Model–View–ViewModel (MVVM) architecture, which is a widely-used design pattern in Android app development. MVVM plays an important role in separating the backend logic from the front-end design, ensuring that the app is modular and easier to maintain. I’m beginning to see how this architecture is applied in our project’s codebase, and it’s helping me understand how to approach future development. 

## Update 10/11/24
This week, I had a productive conversation with my team lead about potential new features we could add to the Android app. During our discussion, he brought up the idea of integrating the app’s calendar with all the tasks that users need to complete. This feature would allow users to have a clear and organized view of their upcoming tasks, giving them the ability to see what they need to focus on in the coming weeks and months. I thought this was a great idea, as it would really enhance the user experience by making task management more intuitive. After our talk, I began exploring how to implement this feature by creating a new model for the database, which would store the task and calendar-related data. This required me to dive into learning how to structure the database in a way that can efficiently handle and display these values. To help with this, I turned to YouTube, where I found some helpful tutorial videos that walked through the process of setting up database models and connecting them to the user interface.

## Update 10/18/24
Since integrating the calendar with tasks involves working closely with the database, I’m focusing on learning more about database management and how to implement changes effectively. To do this, I relied on online resources, like tutorials and documentation, as well as guidance from my team lead, who has been a helpful resource for explaining the project’s existing database structure and best practices. As I gain more confidence, I’ll start making some changes to the code to experiment with the new model I’m creating for the task-calendar integration.

Also, I’ve identified another important issue that I can contribute to: Procrastaint  is aiming to connect with external calendars, such as Google Calendar and Apple’s iOS calendar. This feature would allow users to sync their tasks and events from the app with their personal calendars, offering a more seamless and convenient way to manage their schedules. This integration would be a valuable addition, but it requires an understanding of APIs (Application Programming Interfaces). I’ve started researching and learning more about APIs, watching tutorials to get a solid foundation in what APIs are, how they work, and how we can use them to achieve this integration. It’s an area I’m still exploring, but I’m excited about the potential this feature could bring to our app, and I’m eager to develop the knowledge needed to implement it.


#### Here are some useful resources I used during the first mid semester:
* https://www.w3schools.com/KOTLIN/index.php
 This is a link to Kotlin tutorial.
* https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax
This is a link for writing documents tutorials using markdown. It's helpful for future documenting our project.
* https://www.youtube.com/watch?v=fo6rvTP9kkc
  This video provide a quick introduction to MVVM (Model-View-ViewModel) Pattern.
* https://www.youtube.com/watch?v=bxuYDT-BWaI
  This video provides a very clear introduction to API(Application Programming Interfaces).
* https://www.youtube.com/watch?v=eVqObct7AFQ
  This is a tutorial to connecting with Google Calendar API.

