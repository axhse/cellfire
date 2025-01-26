import { FORECAST_STEP } from '../../domain/definitions';
import { roundPoint } from '../../domain/logic';

export class ScenarioService {
  async createScenario(startPoint, startDate) {
    startPoint = roundPoint(startPoint);

    return {
      id: 'demo id',
      startPoint,
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
      demoCells.push(produceDemoCell(x, y));
      if (x > 0) {
        demoCells.push(produceDemoCell(-x, y));
        if (y > 0) {
          demoCells.push(produceDemoCell(-x, -y));
        }
      }
      if (y > 0) {
        demoCells.push(produceDemoCell(x, -y));
      }
    }
  }
  return { cells: demoCells };
}

function produceDemoCell(x, y) {
  return {
    x,
    y,
    fire: {
      heat: 100 + (((x + 3) * 5) % 77) + (((y + 7) * 9) % 100),
      resource: 0 + (((x + 4) * 5) % 77) / 77 + (((y + 3) * 9) % 100) / 44,
    },
  };
}
