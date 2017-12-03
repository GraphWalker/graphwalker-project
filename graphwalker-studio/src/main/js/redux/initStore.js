import { createStore, combineReducers, applyMiddleware, compose } from 'redux';
import logger from 'redux-logger';
import * as reducers from './reducers';

function create(initialState) {
  return createStore(
    combineReducers(reducers),
    initialState,
    compose(applyMiddleware(logger)),
  );
}

export default function initStore(initialState) {
  return create(initialState);
}
