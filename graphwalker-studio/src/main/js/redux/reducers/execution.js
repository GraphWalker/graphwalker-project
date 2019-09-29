import produce from "immer";
import {
  EXECUTION_BREAKPOINT_TOGGLE,
  EXECUTION_CONNECT,
  EXECUTION_DELAY,
  EXECUTION_FAILED,
  EXECUTION_FULFILLED,
  EXECUTION_LOAD,
  EXECUTION_PAUSE,
  EXECUTION_RUN,
  EXECUTION_STEP,
  EXECUTION_STOP
} from "../actionTypes";

const initialState = {
  running: false,
  paused: false,
  delay: 250,
  fulfillment: {},
  totalCount: 0,
  visited: {},
  breakpoints: {},
  issues: []
}

export default function(state = initialState, action) {
  switch (action.type) {
    case EXECUTION_BREAKPOINT_TOGGLE: {
      const { modelId, elementId } = action.payload;
      const key = `${modelId},${elementId}`;
      return produce(state , draft => {
        if (key in draft.breakpoints) {
          delete draft.breakpoints[key];
        } else {
          draft.breakpoints[key] = true;
        }
      });
    }
    case EXECUTION_CONNECT: {
      console.log('EXECUTION_CONNECT', action.payload);
      return {
        ...state,
        running: false,
        paused: false
      }
    }
    case EXECUTION_DELAY: {
      const { value } = action.payload;
      return {
        ...state,
        delay: value
      }
    }
    case EXECUTION_FAILED: {
      console.log('EXECUTION_FAILED', action.payload);
      return {
        ...state,
        running: false,
        paused: false,
        issues: action.payload
      }
    }
    case EXECUTION_FULFILLED: {
      console.log('EXECUTION_FULFILLED', action.payload);
      return {
        ...state,
        running: false,
        paused: false
      }
    }
    case EXECUTION_LOAD: {
      console.log('EXECUTION_LOAD', action.payload);
      return {
        ...state,
        fulfillment: [],
        totalCount: 0,
        visited: {}
      }
    }
    case EXECUTION_PAUSE: {
      console.log('EXECUTION_PAUSE', action.payload);
      return {
        ...state,
        running: false,
        paused: true
      }
    }
    case EXECUTION_RUN: {
      console.log('EXECUTION_RUN', action.payload);
      return {
        ...state,
        running: true,
        paused: false,
        issues: []
      }
    }
    case EXECUTION_STEP: {
      console.log('EXECUTION_STEP', action.payload);
      const { command, modelId, elementId, stopConditionFulfillment, visitedCount, totalCount } = action.payload;
      if (command === 'visitedElement') {
        return produce(state , draft => {
          draft.fulfillment = Object.assign({}, draft.fulfillment, { [modelId] : stopConditionFulfillment });
          draft.totalCount = totalCount;
          draft.visited[modelId] = Object.assign({}, draft.visited[modelId]);
          draft.visited[modelId][elementId] = visitedCount;
        });
      } else {
        return state
      }
    }
    case EXECUTION_STOP: {
      console.log('EXECUTION_STOP', action.payload);
      return {
        ...state,
        running: false,
        paused: false,
        fulfillment: [],
        totalCount: 0,
        visited: {},
        issues: []
      }
    }
    default: {
      return state;
    }
  }
}
