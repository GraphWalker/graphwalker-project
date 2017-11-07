import { createStore, combineReducers, applyMiddleware, compose } from 'redux';
import themeReducer from './reducers/themeReducer';

function create(initialState) {
  let middlewares = [];
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
