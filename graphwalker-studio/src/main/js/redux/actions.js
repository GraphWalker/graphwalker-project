import {ADD_MODEL, LOAD_TEST, NEW_TEST} from "./actionTypes";

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
