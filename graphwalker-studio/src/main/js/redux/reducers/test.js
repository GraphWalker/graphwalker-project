import {
  ADD_MODEL,
  LOAD_TEST,
  NEW_TEST,
  SELECT_MODEL,
  SELECT_ELEMENT,
  UPDATE_MODEL,
  UPDATE_ELEMENT, SET_START_ELEMENT, UPDATE_EXECUTION, CLOSE_MODEL
} from "../actionTypes";

const initialState = {
  models: [],
  selectedModelIndex: null,
  selectedElementId: null,
  execution: {
    delay: 0
  }
};

export default function(state = initialState, action) {
  switch (action.type) {
    case ADD_MODEL: {
      return {
        ...state,
        models: [action.payload, ...state.models],
        selectedModelIndex: 0,
        selectedElementId: null
      }
    }
    case LOAD_TEST: {
      return {
        ...state,
        ...JSON.parse(action.payload.content),
        selectedModelIndex: 0,
        selectedElementId: null
      }
    }
    case NEW_TEST: {
      return {
        ...initialState,
        models: [action.payload],
        selectedModelIndex: 0,
        selectedElementId: null
      }
    }
    case SELECT_MODEL: {
      return {
        ...state,
        selectedModelIndex: action.payload.index,
        selectedElementId: null
      }
    }
    case SELECT_ELEMENT: {
      return {
        ...state,
        selectedElementId: action.payload.id
      }
    }
    case UPDATE_MODEL: {
      const {field, event: {currentTarget: {value}}} = action.payload;
      const {models, selectedModelIndex} = state;
      models[selectedModelIndex][field] = value;
      return {
        ...state,
        models
      };
    }
    case UPDATE_ELEMENT: {
      const {field, event: {currentTarget: {value}}} = action.payload;
      const {models, selectedModelIndex, selectedElementId} = state;
      models[selectedModelIndex].vertices = models[selectedModelIndex].vertices.map(vertex => {
        if (vertex.id === selectedElementId) {
          vertex[field] = value;
        }
        return vertex;
      })
      models[selectedModelIndex].edges = models[selectedModelIndex].edges.map(edge => {
        if (edge.id === selectedElementId) {
          edge[field] = value;
        }
        return edge;
      })
      return {
        ...state,
        models
      }
    }
    case SET_START_ELEMENT: {
      const {event: {currentTarget: {checked}}} = action.payload;
      const {models, selectedElementId} = state;
      return {
        ...state,
        models: models.map(model => {
          model.startElementId = checked ? selectedElementId : "";
          return model;
        })
      }
    }
    case UPDATE_EXECUTION: {
      const {value} = action.payload;
      return {
        ...state,
        execution: {
          delay: value
        }
      }
    }
    case CLOSE_MODEL: {
      const {models, selectedModelIndex} = state;
      const {index} = action.payload;
      return {
        ...state,
        models: models.filter((value, n) => n !== index),
        selectedModelIndex: Math.max(selectedModelIndex - 1, 0)
      }
    }
    default: {
      return state;
    }
  }
}
