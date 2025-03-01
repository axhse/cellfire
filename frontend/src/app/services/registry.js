let simulator;

if (process.env.REACT_APP_BUILD_NAME === 'standalone') {
  simulator = new (require('./standalone/Simulator').Simulator)();
} else {
  simulator = new (require('./integrated/Simulator').Simulator)();
}

export { simulator };
