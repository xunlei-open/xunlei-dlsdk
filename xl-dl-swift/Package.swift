// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "XlDlSwift",
    platforms: [
        .iOS(.v13),
        .macOS(.v11)
    ],
    products: [
        .library(name: "XlDlSwift", targets: ["XlDlSwift"]),
        .executable(name: "xl-dl-swift-macos-example", targets: ["XlDlSwiftMacOSExample"]),
        .executable(name: "xl-dl-swift-integration-test", targets: ["XlDlSwiftIntegrationTest"])
    ],
    targets: [
        .target(
            name: "CXlDl",
            path: "Sources/CXlDl",
            publicHeadersPath: "include"
        ),
        .target(
            name: "XlDlSwift",
            dependencies: ["CXlDl"]
        ),
        .executableTarget(
            name: "XlDlSwiftMacOSExample",
            dependencies: ["XlDlSwift"],
            path: "Examples/macOS",
            exclude: ["README.md"],
            linkerSettings: [
                .unsafeFlags(["-L", "Binaries/macos"]),
                .linkedLibrary("dk")
            ]
        ),
        .executableTarget(
            name: "XlDlSwiftIntegrationTest",
            dependencies: ["XlDlSwift"],
            path: "Tests/Integration",
            linkerSettings: [
                .unsafeFlags(["-L", "Binaries/macos"]),
                .linkedLibrary("dk")
            ]
        )
    ]
)
