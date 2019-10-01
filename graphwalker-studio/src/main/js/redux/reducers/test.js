import produce from "immer";
import {
  TEST_LOAD,
  TEST_NEW,
  MODEL_ADD,
  MODEL_CLOSE,
  MODEL_CLOSE_ALL,
  MODEL_SELECT,
  MODEL_UPDATE,
  ELEMENT_CREATE,
  ELEMENT_DELETE,
  ELEMENT_SELECT,
  ELEMENT_START,
  ELEMENT_UPDATE,
  ELEMENT_UPDATE_POSITION, EDITOR_SAVE_STATE
} from "../actionTypes";
import uuid from "uuid/v1";

const initialState = {
  models: [],
  selectedModelIndex: 0,
  selectedElementId: null,
};

export default function(state = initialState, action) {
  switch (action.type) {
    case TEST_LOAD: {
      return produce(state, draft => {
        Object.assign(draft, {
          ...JSON.parse(action.payload.content),
          selectedModelIndex: 0,
          selectedElementId: null
        });
        draft.models.forEach(model => {
          if (!model.id) {
            model.id = uuid()
          }
          model.vertices.forEach(vertex => {
            vertex.properties = Object.assign({x: 0, y: 0}, vertex.properties);
          });
        })
      });
    }
    case TEST_NEW: {
      return {
        ...initialState,
        models: [action.payload],
        selectedModelIndex: 0,
        selectedElementId: null
      }
    }
    case EDITOR_SAVE_STATE: {
      return produce(state, draft => {
        if (draft.models[action.payload.index]) {
          draft.models[action.payload.index].editor = action.payload.editor;
        }
      });
    }
    case MODEL_ADD: {
      return produce(state, draft => {
        Object.assign(draft, {
          models: [action.payload, ...state.models],
          selectedModelIndex: 0,
          selectedElementId: null
        });
      });
    }
    case MODEL_CLOSE: {
      return produce(state, draft => {
        const { models, selectedModelIndex } = state;
        Object.assign(draft, {
          models: models.filter((value, index) => index !== action.payload.index),
          selectedModelIndex: selectedModelIndex === action.payload.index ? Math.max(selectedModelIndex - 1, 0): selectedModelIndex
        });
      });
    }
    case MODEL_CLOSE_ALL: {
      return {
        ...initialState
      }
    }
    case MODEL_SELECT: {
      return produce(state, draft => {
        Object.assign(draft, {
          selectedModelIndex: action.payload.index,
          selectedElementId: null
        });
      });
    }
    case MODEL_UPDATE: {
      const { field, value } = action.payload;
      return produce(state, draft => {
        draft.models[state.selectedModelIndex][field] = value;
      });
    }
    case ELEMENT_CREATE: {
      const { id, name, sourceVertexId, targetVertexId, properties } = action.payload;
      return produce(state, draft => {
        if (properties != null) {
          draft.models[state.selectedModelIndex].vertices.push({
            id, name, properties
          });
        } else {
          draft.models[state.selectedModelIndex].edges.push({
            id, name, sourceVertexId, targetVertexId
          });
        }
      });
    }
    case ELEMENT_DELETE: {
      return produce(state, draft => {
        if (Array.isArray(action.payload.id)) {
          draft.models[state.selectedModelIndex].vertices = draft.models[state.selectedModelIndex].vertices.filter(vertex => !action.payload.id.includes(vertex.id))
          draft.models[state.selectedModelIndex].edges = draft.models[state.selectedModelIndex].edges.filter(edge => !action.payload.id.includes(edge.id))
        } else {
          draft.models[state.selectedModelIndex].vertices = draft.models[state.selectedModelIndex].vertices.filter(vertex => vertex.id !== action.payload.id)
          draft.models[state.selectedModelIndex].edges = draft.models[state.selectedModelIndex].edges.filter(edge => edge.id !== action.payload.id)
        }
      });
    }
    case ELEMENT_SELECT: {
      return {
        ...state,
        selectedElementId: action.payload.id
      }
    }
    case ELEMENT_START: {
      const { id } = action.payload;
      return produce(state, draft => {
        draft.models.forEach(model => {
          model.startElementId = model.startElementId != id ? id : "";
        });
      });
    }
    case ELEMENT_UPDATE: {
      const { field, value } = action.payload;
      return produce(state, draft => {
        const models = draft.models[state.selectedModelIndex];
        models.vertices.forEach(vertex => {
          if (vertex.id === state.selectedElementId) {
            vertex[field] = value;
          }
        });
        models.edges.forEach(edge => {
          if (edge.id === state.selectedElementId) {
            edge[field] = value;
          }
        });
      });
    }
    case ELEMENT_UPDATE_POSITION: {
      const { id, position } = action.payload;
      return produce(state, draft => {
        draft.models[state.selectedModelIndex].vertices.forEach(vertex => {
          if (vertex.id === id) {
            vertex.properties.x = position.x;
            vertex.properties.y = position.y;
          }
        })
      });
    }
    default: {
      return state;
    }
  }
}
