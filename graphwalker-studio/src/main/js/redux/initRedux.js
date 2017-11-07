import { createStore, combineReducers, applyMiddleware, compose } from 'redux';
import logger from 'redux-logger'
import themeReducer from './reducers/themeReducer';

function create(initialState) {
  let middlewares = [logger];
  return createStore(
    combineReducers({
      theme: themeReducer,
    }),
    initialState,
    compose(applyMiddleware(...middlewares)),
  );
}

export default function initRedux(initialState) {
  return create(initialState);
}
