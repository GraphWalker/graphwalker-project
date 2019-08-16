import {
  ADD_MODEL,
  LOAD_TEST,
  NEW_TEST,
  SELECT_MODEL,
  SELECT_ELEMENT,
  UPDATE_MODEL,
  UPDATE_ELEMENT,
  SET_START_ELEMENT,
  CLOSE_MODEL,

  EXECUTION_CONNECT,
  EXECUTION_DELAY,
  EXECUTION_FAILED,
  EXECUTION_FULFILLED,
  EXECUTION_LOAD,
  EXECUTION_PAUSE,
  EXECUTION_RUN,
  EXECUTION_STEP,
  EXECUTION_STOP
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

export const loadTest = content => {
  return async (dispatch, getState, client) => {
    dispatch({type: EXECUTION_STOP, payload: {response: await client.close()}});
    dispatch({type: LOAD_TEST, payload: {content}});
  }
}

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

export const closeModel = index => ({
  type: CLOSE_MODEL,
  payload: {
    index
  }
});

export const setExecutionDelay = (value) => ({
  type: EXECUTION_DELAY,
  payload: {
    value
  }
});

const action = (type, response = {}) => {
  return {
    type,
    payload: {
      response
    }
  }
}

export const runTest = () => {
  return async (dispatch, getState, client) => {
    const {test: {models}, execution: {paused, delay}} = getState();
    try {
      if (!paused) {
        dispatch(action(EXECUTION_CONNECT, await client.connect()));
        dispatch(action(EXECUTION_LOAD, await client.send({command: 'start', gw: {name: 'TEST', models}})));
      }
      dispatch(action(EXECUTION_RUN));
      const callback = async () => {
        const { execution: { running, delay }} = getState();
        if (running) {
          await dispatch(stepTest());
          setTimeout(callback, delay);
        }
      }
      setTimeout(callback, delay);
    } catch (error) {
      dispatch(action(EXECUTION_STOP, error));
    }
  }
}

export const stepTest = () => {
  return async (dispatch, getState, client) => {
    try {
      const response = await client.send({command: 'hasNext'});
      if (response.hasNext) {
        dispatch(action(EXECUTION_STEP, await client.send({command: 'getNext'})));
      } else {
        dispatch(action(EXECUTION_FULFILLED, response));
      }
    } catch (error) {
      dispatch(action(EXECUTION_STOP, error));
    }
  }
}

export const pauseTest = () => {
  return async (dispatch, getState, client) => {
    dispatch(action(EXECUTION_PAUSE));
  }
}

export const stopTest = () => {
  return async (dispatch, getState, client) => {
    dispatch(action(EXECUTION_STOP, await client.close()));
  }
}
