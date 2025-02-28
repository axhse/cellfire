let simulator;

if (process.env.REACT_APP_BUILD_NAME === 'standalone') {
  simulator = new (require('./standalone/simulator').Simulator)();
} else {
  simulator = new (require('./integrated/simulator').Simulator)();
}

export { simulator };
