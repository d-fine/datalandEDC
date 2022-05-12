# Downloading the Repository

Clone the repository by running `git clone --recurse-submodules git@github.com:d-fine/datalandEDC.git`. Alternatively
you could run `git submodule update --init --recursive` to initialize EDC after cloning datalandEDC.

# License
This project is free and open-source software licensed under the [GNU Affero General Public License v3](LICENSE) (AGPL-3.0). Commercial use of this software is allowed. If derivative works are distributed, you need to be published the derivative work under the same license. Here, derivative works includes web publications. That means, if you build a web service using this software, you need to publish your source code under the same license (AGPL-3.0)

In case this does not work for you, please contact dataland@d-fine.de for individual license agreements.

# Contributions
Contributions are highly welcome. Please refer to our [contribution guideline](contribution/contribution.md).
To allow for individual licenses and eventual future license changes, we require a contributor license agreement from any contributor that allows us to re-license the software including the contribution.

# Publishing a release
To publish a (non-SNAPSHOT)-Release:
* checkout & update the current main branch (`git checkout main && git pull`)
* tag the current commit using the tag name `RELEASE-<version>` (`git tag RELEASE-<version>`)
* push the tag (`git push origin RELEASE-<version>`)

# Dependency on OpenApi Generator
We need to modify the OpenApi generated Code to increase HTTP Timeouts. To achieve this, we adapted a moustache Template. 
In case the OpenApi Generator version is updated, most likely this template needs an update as well. To 
extract the Template corresponding to the current OpenApiversion, run:
`docker run --rm -v <yourTargetDirectoryHere>:/local openapitools/openapi-generator-cli:<openApiGeneratorPluginVersion> author template -g kotlin -o /local/out/`
where e.G. `yourTargetDirectoryHere`=`C:\datalandEDC`, and `openApiGeneratorPluginVersion`=`v5.4.0`.
Find the relevant template in: `/out/libraries/jvm-okhttp/infrastructure/ApiClient.kt.mustache`.
Copy it to `openApiTemplate/libraries/jvm-okhttp/infrastructure/ApiClient.kt.mustache`. 
Make sure the applied changes (import of `TimeUnit` as well as setting the timeouts during the `OkHttpClient.Builder` phase) get re-applied.