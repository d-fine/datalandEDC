rootProject.name = "connector"
includeBuild("trustee-platform/services/edc") // TODO
includeBuild("trustee-platform/services/broker")
include("api")
include("dataland-edc-client")
include("dataland-edc-dummyserver")
include("dataland-edc-server")
