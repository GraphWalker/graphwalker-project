import { createStore, combineReducers, applyMiddleware, compose } from 'redux';
import logger from 'redux-logger'
import themeReducer from './reducers/themeReducer';

function create(initialState) {
  return createStore(
    combineReducers({
      theme: themeReducer,
    }),
    initialState,
    compose(applyMiddleware(logger)),
  );
}

export default function initStore(initialState) {
  return create(initialState);
}
