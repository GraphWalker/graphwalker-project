import {ADD_MODEL, LOAD_TEST, NEW_TEST, SELECT_MODEL, SELECT_ELEMENT} from "../actionTypes";

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
    default:
      return state;
  }
}
