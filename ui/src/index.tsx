import React from "react";
import ReactDOM from "react-dom";
// import './index.css';
import App from "./App";
import * as serviceWorker from "./serviceWorker";
import "./index.css";
import { ConfigProvider } from "antd";
import plPL from "antd/es/locale/pl_PL";
import { Provider } from "react-redux";
import { store } from "./store";
import "antd/dist/antd.css";
import { WSAEACCES } from "constants";
import { notifyError } from "./utils";

const render = () => {
  const App = require("./App").default;
  const source = new EventSource("http://localhost:8080/api/sse")
  source.onopen = () => notifyError("connected");
  source.onmessage = (message) => notifyError(`message received: ${message.data}`);

  ReactDOM.render(
    <React.StrictMode>
      <Provider store={store}>
        <ConfigProvider locale={plPL}>
          <App />
        </ConfigProvider>
      </Provider>
    </React.StrictMode>,
    document.getElementById("root"),
  );
};

render();

if (process.env.NODE_ENV === "development" && module.hot) {
  module.hot.accept("./App", render);
}

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
