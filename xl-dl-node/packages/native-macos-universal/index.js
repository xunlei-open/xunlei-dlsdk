const path = require("node:path");

exports.getLibraryPath = function getLibraryPath() {
  return path.join(__dirname, "lib", "libdk.dylib");
};
