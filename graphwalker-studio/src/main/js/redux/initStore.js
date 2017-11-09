import { createStore, combineReducers, applyMiddleware, compose } from 'redux';
import logger from 'redux-logger'
import theme from './reducers/theme';

function create(initialState) {
  return createStore(
    combineReducers({
      theme,
    }),
    initialState,
    compose(applyMiddleware(logger)),
  );
}

export default function initStore(initialState) {
  return create(initialState);
}
