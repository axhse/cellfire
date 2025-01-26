let scenarioService;

if (process.env.REACT_APP_BUILD_NAME === 'standalone') {
  scenarioService = new (require('./standalone/scenario').ScenarioService)();
} else {
  scenarioService = new (require('./integrated/scenario').ScenarioService)();
}

export { scenarioService };
