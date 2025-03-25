let SIMULATOR;

if (process.env.REACT_APP_BUILD_NAME === "standalone") {
  SIMULATOR = new (require("./standalone/Simulator").Simulator)();
} else {
  SIMULATOR = new (require("./integrated/Simulator").Simulator)();
}

export { SIMULATOR };
