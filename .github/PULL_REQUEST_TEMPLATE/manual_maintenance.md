# Manual Maintenance Sprint [NR]
Note: To create a PR using this template add the query parameter `template=manual_maintenance.md` to the merge request creation URL (or simply copy this md file into the description)

# Maintenance tasks (to be completed by the assignee)
## EDC
### Skipped updates
The following known issues need to be reviewed in case a compatible version is available. Add new known issues as they appear.
- [ ] ktlint 0.45.2 (higher version is not compatible with jlleitschuh plugin)
- [ ] sonarqube 3.4.0.2513 not update to 3.5.X, due to issues in file resolving mechanism

### Gradle update
- [ ] Execute `gradlew dependencyUpdates` to get a report on Dependencies with updates
- [ ] Update `settings.gradle.kts` (for libraries), `build.gradle.kts` (for plugins) and `gradle.properties` (for jacoco and ktlint)
  Note: fasterXML is managed by spring, thus NO manual version update should be conducted
- [ ] update the gradle wrapper: execute `gradle wrapper --gradle-version X.Y.Z`

### OpenAPI update
- [ ] If you updated the OpenAPI-Generator, also update the `dataland-edc-client/openApiTemplate` folder as described in the README.md

### Dockerfile updates
Update versions in the following dockerfiles
- [ ] `./dataland-edc-dummyserver/Dockerfile`
- [ ] `./dataland-edc-server/Dockerfile`

## Conclusion
- [ ] After updating all components check if everything still works
- [ ] This template has been updated to reflect the latest state of tasks required and known issues with upgrades

# Review (to be completed by the reviewer)
- [x] The Github Actions (including Sonarqube Gateway and Lint Checks) are green. This is enforced by Github.
- [ ] A peer-review has been executed
    - [ ] The code has been manually inspected by someone who did not implement the feature
    - [ ] It was observed "in running software" that everything still works fine
- [ ] The PR actually implements what is described above
- [ ] Documentation is updated as required
- [ ] The automated deployment is updated if required
- [ ] The test-script `dataland-edc-server/test/devtest.sh` still works. Run it locally!
- [ ] If the behaviour of the EDC has changed from an external perspective, the dummyEDC has also been adopted to those changes
