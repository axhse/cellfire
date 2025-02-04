import { FORECAST_STEP } from '../../domain/definitions';

export class ScenarioService {
  async createScenario(startCoordinates, startDate) {
    return {
      id: 'DEMO-ID',
      startCoordinates,
      startDate,
      actualDate: startDate,
    };
  }

  async removeScenario() {
    return;
  }

  async forecastScenario(scenario) {
    return produceDemoForecast(scenario);
  }
}

function produceDemoForecast(scenario) {
  const demoCells = [];
  for (
    let x = 0;
    x <= (scenario.actualDate - scenario.startDate) / FORECAST_STEP;
    x++
  ) {
    for (
      let y = 0;
      x + y <= (scenario.actualDate - scenario.startDate) / FORECAST_STEP;
      y++
    ) {
      demoCells.push(produceDemoCell(scenario.startCoordinates, x, y));
      if (x > 0) {
        demoCells.push(produceDemoCell(scenario.startCoordinates, -x, y));
        if (y > 0) {
          demoCells.push(produceDemoCell(scenario.startCoordinates, -x, -y));
        }
      }
      if (y > 0) {
        demoCells.push(produceDemoCell(scenario.startCoordinates, x, -y));
      }
    }
  }
  return { cells: demoCells };
}

function produceDemoCell(startCoordinates, offsetX, offsetY) {
  const heat =
    100 + (((offsetX + 3) * 5) % 77) * 10 + (((offsetY + 7) * 9) % 100) * 4;
  const fuel =
    0 + (((offsetX + 4) * 5) % 77) / 77 + (((offsetY + 3) * 9) % 100) / 44;
  const isDamaged = (offsetX * 2 + offsetY * 5 + 7) % 20 < 10;

  return {
    coordinates: {
      x: startCoordinates.x + offsetX,
      y: startCoordinates.y + offsetY,
    },
    fire: { heat, fuel, isDamaged },
    factors: { ignitionTemperature: 200 },
  };
}
