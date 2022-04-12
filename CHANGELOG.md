# Release notes

## UNRELEASED

#### Added

#### Changed

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