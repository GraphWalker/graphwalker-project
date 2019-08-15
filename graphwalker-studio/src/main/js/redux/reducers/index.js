import { combineReducers } from "redux";
import execution from "./execution";
import test from "./test"

export default combineReducers({
  execution,
  test
});
