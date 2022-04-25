rootProject.name = "connector"
includeBuild("trustee-platform/services/edc") //TODO
includeBuild("trustee-platform/services/broker")
include("api")
include("dataland-connector")
include("dataland-edc-dummyserver")
include("dataland-edc-client")
include("consumer")
include("provider")
include("transfer-file")
