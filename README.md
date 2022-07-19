<h1 align="center">Disaster Ninja BE</h1>

The back end of https://disaster.ninja/ website.

Full details of the API endpoints is available at: https://disaster.ninja/active/api/doc

## About the project

Disaster Ninja is a tool specifically designed for humanitarian mappers. It is complete critical event management solution that visualizes mapping gaps, provides information about recent natural disasters, and helps connect with local mappers for ground truth verification.

Improving and updating OpenStreetMap data has proven to be an effective way to help international response efforts when disasters strike. An active contributor to the OpenStreetMap community, Kontur partnered with [the Humanitarian OpenStreetMap Team (HOT)](https://www.hotosm.org/) to create a tool to support the rapid deployment of emergency mapping campaigns.

The initial purpose of Disaster Ninja was to gather all necessary data to automate the Disaster Size-Up preparation by HOT, provide instant mapping insights, and free up time to maximize the efficiency of volunteer mappers’ work.

The tool’s functionality is much broader nowadays: it can be used whenever insights from correlating OpenStreetMap data and population density might be helpful. Users can also get analytics for areas affected by disasters, freehand polygons, administrative boundaries, add their own layers, and much more.

[More info ==>](https://www.kontur.io/portfolio/disaster-ninja/)


## Usage

To run Disaster Ninja BE you will need to have Java 17+ installed.

Then you will need to build the service from source code
- `gradlew check` - runs tests
- `gradlew build` - builds application jar file

For Windows users:
- `.\gradlew.bat check` - runs tests
- `.\gradlew.bat build` - builds application jar file

Then run `java -jar build/libs/disaster-ninja-be-{version}.jar` to start the service.
The service will connect to Kontur's services. Open http://localhost:8627/active/api/doc to view Swagger page in the browser.

One would need to register Kontur's account to be able to search for events. <a href="https://www.kontur.io/#footer">Ask Kontur team</a> to do that.
Account credentials can be set in application.yaml file, supply in System variables as `java -Dkontur.platform.keycloak.username={username} -Dkontur.platform.keycloak.password={password} -jar build/libs/disaster-ninja-be-{version}.jar`, or in environment variables `kontur.platform.keycloak.username={username}` and `kontur.platform.keycloak.password={password}`.

## Running in Docker

Docker image is available in <a href="https://github.com/konturio/disaster-ninja-be/pkgs/container/disaster-ninja-be">GitHub Packages</a> 

To start Disaster Ninja BE in your Docker environment run `docker run -d -p 8627:8627 -e "kontur.platform.keycloak.username={username}" -e "kontur.platform.keycloak.password={password}" ghcr.io/konturio/disaster-ninja-be:latest` <!--TODO create latest image-->
Change `{username}` and `{password}` to credentials provided to you by Kontur team.

Once it has started, open http://localhost:8627/active/api/doc to view Swagger page in the browser.
