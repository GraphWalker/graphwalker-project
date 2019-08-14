import {ADD_MODEL, LOAD_TEST, NEW_TEST, SELECT_MODEL} from "../actionTypes";

const initialState = {
  models: [],
  selectedModelIndex: null
};

export default function(state = initialState, action) {
  switch (action.type) {
    case ADD_MODEL: {
      return {
        ...state,
        models: [action.payload, ...state.models],
        selectedModelIndex: 0
      }
    }
    case LOAD_TEST: {
      return {
        ...state,
        ...JSON.parse(action.payload.content),
        selectedModelIndex: 0
      }
    }
    case NEW_TEST: {
      return {
        ...initialState,
        models: [action.payload],
        selectedModelIndex: 0
      }
    }
    case SELECT_MODEL: {
      return {
        ...state,
        selectedModelIndex: action.payload.index
      }
    }
    default:
      return state;
  }
}
