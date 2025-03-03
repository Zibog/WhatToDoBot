package base

interface ResourceTestBase {
    val resources: String
        get() = "src/test/resources"
}