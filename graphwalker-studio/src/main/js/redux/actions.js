import {
  ADD_MODEL,
  LOAD_TEST,
  NEW_TEST,
  SELECT_MODEL,
  SELECT_ELEMENT,
  UPDATE_MODEL,
  UPDATE_ELEMENT,
  SET_START_ELEMENT,
  UPDATE_EXECUTION,
  CLOSE_MODEL,
  EXECUTION_RUN,
  EXECUTION_PAUSE,
  EXECUTION_STOP,
  EXECUTION_STEP
} from "./actionTypes";

let modelCount = 0;
export const addModel = () => ({
  type: ADD_MODEL,
  payload: {
    edges: [],
    generator: "random(edge_coverage(100))",
    id: "e918f0f7-dc56-4aa3-97fb-3660654309f6",
    name: `Untitled-${++modelCount}`,
    startElementId: "",
    vertices: []
  }
});

export const loadTest = content => ({
  type: LOAD_TEST,
  payload: {
    content
  }
});

export const newTest = () => {
  modelCount = 0;
  return {
    type: NEW_TEST,
    payload: {
      edges: [],
      generator: "random(edge_coverage(100))",
      id: "e918f0f7-dc56-4aa3-97fb-3660654309f6",
      name: `Untitled-${++modelCount}`,
      startElementId: "",
      vertices: []
    }
  }
};

export const selectModel = index => ({
  type: SELECT_MODEL,
  payload: {
    index
  }
});

export const selectElement = id => ({
  type: SELECT_ELEMENT,
  payload: {
    id
  }
});

export const updateModel = (field, event) => ({
  type: UPDATE_MODEL,
  payload: {
    field,
    event
  }
});

export const updateElement = (field, event) => ({
  type: UPDATE_ELEMENT,
  payload: {
    field,
    event
  }
});

export const setStartElement = (event) => ({
  type: SET_START_ELEMENT,
  payload: {
    event
  }
});

export const updateExecution = (value) => ({
  type: UPDATE_EXECUTION,
  payload: {
    value
  }
});

export const closeModel = index => ({
  type: CLOSE_MODEL,
  payload: {
    index
  }
});

export const runTest = () => ({
  type: EXECUTION_RUN
})

export const stepTest = () => ({
  type: EXECUTION_STEP
})

export const pauseTest = () => ({
  type: EXECUTION_PAUSE
})

export const stopTest = () => ({
  type: EXECUTION_STOP
})
