# Product Name: UofT course route planner / Team Name: CSC301 Team 03

## Iteration 02

 * Start date: February 19th
 * End date: March 11th

## Process

#### Roles & responsibilities

1. Developer Roles
* Since this is the first stage where we will start development, we decided to assign general developer responsibilities based on their strengths and preferences.
* Course/User DB creation & data setup: Arthur
* Front end design: Pelin, Raymond, Yue
* Google API integration/feature design: Alex, Wontae, Wendy

2. Other Roles
* Git overseerer (management of merge request, etc): Arthur
* Meeting overseerer (transcription of meeting minutes, directing flow of meeting, etc): Alex

#### Events

General Meetings:
We decided to have general meetings on Discord every week. These would be "weekly reviews" where we would go over our individual progress on the goals that we set for the week, what we accomplished and the obstacles we came across. 
Then we would discuss goals for the following week and finally end off with odds and ends/ideas for the later development of the project.

Other than these meetings, in-person meetings at tutorial will be secondarily used to talk about ideas and issues that we've had. These meetings are mostly for the sake of themselves as meeting in-person would help with accountability and general team cohesion.
We haven't discussed coding sessions or other types of meetings quite yet since we haven't moved too far in development but in the near future we will be discussing the need for these kinds of meetings.

#### Artifacts
   
1. To-do lists:
* After every meeting, we will post a number of to-do lists for the following week.
* The items on these lists of tasks will be assigned to either an individual or a team of up to three programmers (see the developer roles above).
* Each lists will be marked with a priority with the first being the items required (ideally completed) by the end of the week

2. Schedule:
* We'll be keeping track of general milestones on a calendar as well as the deliverable deadline
* This will give us a bigger picture of where we are at and how much time we have until the next goal
   
Week 1

#### Git / GitHub workflow

* We want to aim to have master be as stable as possible so beginning after our initial commits and code base initalization, we will stop all commits to master except for deliverable item updates (plan.md/review.md)
* Individuals or teams will work on feature branches and then create a pull request to merge their branch to master
* Arthur will be overseeing the review and merges to master and resolving conflicts
* The rule preventing to code commits to master may be relaxed closer to the deadlines in the case of necessity

## Product

#### Goals and tasks
 
 Goals:
 1. Create a basic application that will take in a schedule with 5 locations and display a route to and from each location in sequence with estimated times for each route
    * The idea for this first deliverable is to implement our main idea - the automatic direction for a student given a schedule
    * This would be the absolute minimum that we would want to present for this first deliverable
    * Associated with this first goal is a number of backend work such as DB population, request handling, file handling, etc.
 2. Add a user interface for the application for selecting the schedule file and viewing the route
 3. Dynamic user location tracking to display a user's estimated distance/time away from their next class and the best route to that class

#### Artifacts

* Week to week, we will try and create a stable build to deploy to our devices to show off our idea
* In this first week, we plan to simply create a proof of concept by accessing our backend DB and using the Google API to display a route on a map
* Close to the end of this first deliverable we will create a video (as required) to present our idea