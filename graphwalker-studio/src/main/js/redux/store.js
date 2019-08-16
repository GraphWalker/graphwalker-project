import { createStore, applyMiddleware } from "redux";
import thunk from 'redux-thunk';
import rootReducer from "./reducers";
import WebSocketClient from "../client";

const client = new WebSocketClient('ws://localhost:9999');

export default createStore(
  rootReducer,
  applyMiddleware(thunk.withExtraArgument(client))
);
