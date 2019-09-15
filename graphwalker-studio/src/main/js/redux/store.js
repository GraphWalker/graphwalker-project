import { createStore, applyMiddleware, compose } from "redux";
import thunk from 'redux-thunk';
import rootReducer from "./reducers";
import WebSocketClient from "../client";

const client = new WebSocketClient('ws://localhost:9999');

const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;

export default createStore(
  rootReducer,
  composeEnhancers(applyMiddleware(thunk.withExtraArgument(client)))
);
