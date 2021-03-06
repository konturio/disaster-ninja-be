# Release notes

## UNRELEASED

#### Added

#### Changed

#### Removed

#### Installation sequence


## Release 0.12.0 - 2022-07-18

#### Added

#### Changed
- #11041 Add endpoint for RUM metrics
- #11053 Add bbox filter to event search endpoint

#### Removed

#### Installation sequence



## Release 0.11.0 - 2022-06-08

#### Added
- #10022 For tiles add version, tile type, interface for insights api client for open source purposes, 
- #10285 For tiles add parameter with the class of requested indicator

#### Changed
- #10022 Change tile url for bivariate presets
- #10479 Updates configs for k8s deployment
- #10329 Update LICENSE copyrights

#### Removed

#### Installation sequence



## Release 0.10.0 - 2022-06-01

#### Added
- #10323 Add Dockerfile
- #10342 Configure github ci

#### Changed
- #10323 Update the service to java 17
- #10339 Removed call to insights-api /population/humanitarian_impact. Started to use graphql instead
- #10328 Disable insights api integration on missing config

#### Removed

#### Installation sequence



## Release 0.9.0 - 2022-05-26

#### Added

#### Changed
- #10432 Fixed error propagation to client while searching layers

#### Removed

#### Installation sequence



## Release 0.8.0 - 2022-05-12

#### Added

#### Changed
- #10207 Refactor Layers providers parallel invocation

#### Removed

#### Installation sequence



## Release 0.7.0 - 2022-05-05

#### Added
- #10006 Add fallback for /apps/default_id. Returns preconfigured id when User Profile Api responses with an error.

#### Changed
- #10095 Optimize EventShapeLayerProvider
- #10164 Invoke layers providers in parallel
- #9900 Format analytical panel numbers.

#### Removed

#### Installation sequence



## Release 0.6.0 - 2022-04-29

#### Added

#### Changed
- #10039 Update Legend Step structure. Add stepIconFill and stepIconStroke
- #10012 Use new /layers/collections/{id}/itemsByGeometry endpoint to get list of layers from kcapi 

#### Removed

#### Installation sequence
- Install new kcapi version with new endpoint beforehand.


## Release 0.5.0 - 2022-04-21

#### Added
- #9951 Total event loss 

#### Changed
- #10008 Fix layer config for old events
- #9945 Change text in the panel with Analytics



## Release 0.4.1 - 2022-04-13

#### Added

#### Changed
- #9580 Increase page limit for kcapi and layers api requests from 100 to 1000
- #9491 Send null statistic to FE for events without statistic 

#### Removed

#### Installation sequence



## Release 0.4.0 - 2022-04-12

#### Added

#### Changed
- #9725 Pass application id into Layers API feature search request
- #9463 Add tooltip into layers legend
- #9818 Remove steps duplication in legend. Update volcanoes legend config 
- #9819 Use displayRule from Layers API to set up boundaryRequiredForRetrieval and eventIdRequiredForRetrieval

#### Removed

#### Installation sequence
- Don't forget to install Layers API with appId in feature search endpoint 



## Release 0.3.0 - 2022-04-04

#### Added
- Slack notifications



## Release 0.2.0 - 2022-04-01

#### Added
- appId parameter is added to the next list of endpoints
  - POST /layers/search
  - POST /layers/details
  - POST /layers
  - PUT /layers/{id}

#### Changed

#### Removed

#### Installation sequence
- Install new version of Layers API where applications are introduced