# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased](https://github.com/internetofwater/nldi-crawler/compare/1.1.1...master)
### Fixed
- Subsequent executions after a failed run now properly re-initialize the temp table

## [1.1.1](https://github.com/internetofwater/nldi-crawler/compare/nldi-crawler-1.1.0...1.1.1)
### Added
- Support for generic GeoJSON geometry types within a feature

### Fixed
- Malformed GeoJSON files no longer cause an infinite loop
- Test setup now works with latest NLDI CI database

## [1.1.0](https://github.com/internetofwater/nldi-crawler/compare/nldi-crawler-1.0.0...nldi-crawler-1.1.0)
### Changed
- Updated Ingestor to *additionally* check for id at top level of feature

## [1.0.0](https://github.com/internetofwater/nldi-crawler/compare/nldi-crawler-0.3.1...nldi-crawler-1.0.0)
### Changed
- Using Spring Boot
- Docker configuration added
- Jobs invoked via command-line rather than via ActiveMQ
- Jenkinsfile build job added
- Updated to shared pipeline library 0.6.1 and JDK 11
- Upgrade various dependencies
- Updated to latest build/deploy method
- Added code coverage badge
