import { PROJECT_SET_ACTIVE_MODEL } from '../actions/project';

const initialState = {
  activeModelId: 0,
  models: [{
    id: '0',
    name: 'A',
    properties: {
      key: 'value A'
    },
    graph: {
      nodes: [
        { data: { id: "n1", name: "one", weight: 0.25 }, classes: "odd one" },
        { data: { id: "n2", name: "two", weight: 0.5 }, classes: "even two" },
        { data: { id: "n3", name: "three", weight: 0.75 }, classes: "odd three" },
      ],
      edges: [
        { data: { id: "n1n2", source: "n1", target: "n2", weight: 0.33 }, classes: "uh" },
        { data: { id: "n2n3", source: "n2", target: "n3", weight: 0.66 }, classes: "huh" },
        { data: { id: "n1n1", source: "n1", target: "n1" } }
      ]
    }
  }, {
    id: '1',
    name: 'B',
    properties: {
      key: 'value B'
    },
    graph: {
      nodes: [
        { data: { id: "n4", parent: "n5", name: "bar" } },
        { data: { id: "n5" } }
      ],
      edges: [
      ]
    }
  }],
};

const mapping = {
  [PROJECT_SET_ACTIVE_MODEL]: (state, action) => ({
    ...state,
    activeModelId: action.payload.id,
  }),
};

function project(state = initialState, action) {
  let newState = state;
  if (mapping[action.type]) {
    newState = mapping[action.type](state, action);
  }
  return newState;
}

export default project;
