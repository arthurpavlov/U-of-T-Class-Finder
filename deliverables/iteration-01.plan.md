# Product Name: UofT course route planner / Team Name: CSC301 Team 03

## Iteration 01

 * Start date: February 4th
 * End date: February 8th

## Process

_This entire section is optional. Note that you will have to fill it out and more for the next 3 deliverables so it's good to start soon and get feedback._ 

#### Roles & responsibilities

Describe the different roles on the team and the responsibilities associated with each role.

 * We know that we want two concrete roles: meeting recorder and SCRUM master. But as for development roles, we will need to split up responsibilities based on preference and past experience.

#### Events

Describe meetings (and other events) you are planning to have:

 * We've decided to meet online (Discord) on Thursday nights and in-person (naturally) at our tutorials, Monday nights. 
 * Each meeting will be a review of our accomplishments since the last meeting and discussion on where to go next. Closer to iteration due dates, the discussions will revolve around what we have left to do and what is viable to complete.
 * We have also talked about working with a SCRUM structure which would require more frequent meetings, but as we haven't decided on a strict structure yet, we haven't chosen a time for these meetings.

#### Artifacts

 * N/A
 
## Product

_This entire section is mandatory._

#### Goals and tasks
1. Solidify/deciding on the idea – we have two potential ideas that we were considering and we need to decide and explore only one: an AR outlet finder and a automated route planner from class to class. The former looks to be a little unrealistic in implementing it but the latter seems to be too little work.
2. Organize our schedule and meeting structure – deciding on time/places to meet and discuss changes and responsibilities is extremely important with some of our tight weekly schedules.
3. Decide on general implementation details – that is, platforms, logical structure, & development roles. We want to get this organization done early so that our development moves along smoothly.
4. Create an initial code base to start testing/development – we’ll want to have something to work with asap so that the actual start of the project is easier when we eventually transition into heavy development.

* We don’t expect to complete all these goals by the end of the first iteration but items 1 & 2 are the most important for us to sort out before we can move forward.


#### Artifacts
1. Build a relational database to hold information about the user and location.
     - MongoDB
2. Come up with an algorithm for finding the minimal path between two mapped locations.
     - Depth First Search
3. Implement and deploy server to communicate between application and database for efficient and safe data transfer.
     - NodeJS deployed on cloud-based hosting service (DigitalOcean)
4. Implement mapping functionality, so that user can see the generated path.
     - Google Maps API
5. Implement GPS functionality, so that the path can be generated based on the user's location.
     - Google Maps Geolocation API


 