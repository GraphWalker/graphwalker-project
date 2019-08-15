import {EXECUTION_MESSAGE, EXECUTION_PAUSE, EXECUTION_RUN, EXECUTION_STEP, EXECUTION_STOP} from "../actionTypes";

const initialState = {
  running: false,
  paused: false
}

export default function(state = initialState, action) {
  switch (action.type) {
    case EXECUTION_MESSAGE: {
      return {
        ...state
      }
    }
    case EXECUTION_RUN: {
      return {
        ...state,
        running: true,
        paused: false
      }
    }
    case EXECUTION_PAUSE: {
      return {
        ...state,
        running: false,
        paused: true
      }
    }
    case EXECUTION_STEP: {
      return {
        ...state
      }
    }
    case EXECUTION_STOP: {
      return {
        ...state,
        running: false,
        paused: false
      }
    }
    default: {
      return state;
    }
  }
}
