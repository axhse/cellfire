import Feature from "ol/Feature";
import Polygon from "ol/geom/Polygon";
import { fromLonLat } from "ol/proj";

export class Grid {
  constructor(scale, startCoordinates) {
    this.scale = scale;
    this.startCoordinates = startCoordinates;
  }

  createStartRectangle() {
    return this.createRectangle(this.startCoordinates);
  }

  createRectangle(coordinates) {
    const leftLon = coordinates.x / this.scale;
    const rightLon = (coordinates.x + 1) / this.scale;
    const bottomLat = coordinates.y / this.scale;
    const topLat = (coordinates.y + 1) / this.scale;

    const geometry = new Polygon([
      [
        fromLonLat([leftLon, topLat]),
        fromLonLat([rightLon, topLat]),
        fromLonLat([rightLon, bottomLat]),
        fromLonLat([leftLon, bottomLat]),
        fromLonLat([leftLon, topLat]),
      ],
    ]);
    return new Feature({ geometry });
  }
}
