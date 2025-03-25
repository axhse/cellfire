import React from "react";
import ReactDOM from "react-dom/client";

import "katex/dist/katex.min.css";
import "ol/ol.css";
import "./index.css";

import App from "./app/App";

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
