import { combineReducers } from "redux";
import execution from "./execution";
import test from "./test"
import editor from "./editor";

export default combineReducers({
  execution,
  test,
  editor
});
