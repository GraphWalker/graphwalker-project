import { createStore, combineReducers, applyMiddleware, compose } from 'redux';
import logger from 'redux-logger';
import project from './reducers/project';
import theme from './reducers/theme';

function create(initialState) {
  return createStore(
    combineReducers({
      project,
      theme,
    }),
    initialState,
    compose(applyMiddleware(logger)),
  );
}

export default function initStore(initialState) {
  return create(initialState);
}
