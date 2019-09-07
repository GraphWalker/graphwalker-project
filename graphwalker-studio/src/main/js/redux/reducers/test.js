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
  ELEMENT_UPDATE
} from "../actionTypes";

const initialState = {
  models: [],
  selectedModelIndex: 0,
  selectedElementId: null,
};

export default function(state = initialState, action) {
  switch (action.type) {
    case TEST_LOAD: {
      const file = JSON.parse(action.payload.content);

      // convert gw model to
      file.models.forEach(model => {
        const elements = [];
        const { startElementId } = model;

        model.vertices.map(({id, name, sharedState, actions, requirements, properties: {x = 0, y = 0}}) => elements.push({
          group: 'nodes',
          data: {
            id,
            name,
            color: id === startElementId ? 'LightGreen' : 'LightSteelBlue',
            sharedState,
            actions,
            requirements
          },
          position: {x, y}
        }));

        model.edges.filter(({sourceVertexId: source}) => source == null).map(() => elements.push({
          group: 'nodes',
          data: {id: 'Start', name: 'Start', color: 'LightGreen'},
          position: {x: 0, y: 0}
        }));

        model.edges.map(({id, name, guard, actions, sourceVertexId: source = 'Start', targetVertexId: target}) => elements.push({
          group: 'edges',
          data: {
            id,
            name,
            source: source == null ? 'Start' : source,
            target,
            color: source == null ? 'LightGreen' : 'LightSteelBlue',
            guard,
            actions
          }
        }));

        model.editor = { elements };
      })

      return {
        ...state,
        ...file,
        selectedModelIndex: 0,
        selectedElementId: null
      }
    }
    case TEST_NEW: {
      return {
        ...initialState,
        models: [action.payload],
        selectedModelIndex: 0,
        selectedElementId: null
      }
    }
    case MODEL_ADD: {
      return {
        ...state,
        models: [action.payload, ...state.models],
        selectedModelIndex: 0,
        selectedElementId: null
      }
    }
    case MODEL_CLOSE: {
      const {models, selectedModelIndex} = state;
      const {index} = action.payload;

      return {
        ...state,
        models: models.filter((value, n) => n !== index),
        selectedModelIndex: selectedModelIndex === index ? Math.max(selectedModelIndex - 1, 0): selectedModelIndex
      }
    }
    case MODEL_CLOSE_ALL: {
      return {
        ...initialState
      }
    }
    case MODEL_SELECT: {
      return {
        ...state,
        selectedModelIndex: action.payload.index,
        selectedElementId: null
      }
    }
    case MODEL_UPDATE: {
      const {field, event: {currentTarget: {value}}} = action.payload;
      const {models, selectedModelIndex} = state;

      const updatedModels = models.map(model => {
        return Object.assign({}, model);
      });

      updatedModels[selectedModelIndex][field] = value;
      return {
        ...state,
        models: updatedModels
      };
    }
    case ELEMENT_CREATE: {
      const { data: { id, name, source, target, position }} = action.payload;
      const { models, selectedModelIndex } = state;

      const updatedModels = models.map(model => {
        return Object.assign({}, model);
      });

      if (position != null) {
        const { x, y } = position;
        updatedModels[selectedModelIndex].editor.elements.push({
          group: 'nodes',
          data: {id, name, color: 'LightSteelBlue'},
          position: {x, y}
        });
      } else {
        updatedModels[selectedModelIndex].editor.elements.push({
          group: 'edges',
          data: {
            id,
            name,
            source,
            target,
            color: 'LightSteelBlue'
          }
        });
      }
      return {
        ...state,
        models: updatedModels
      }
    }
    case ELEMENT_DELETE: {
      const { id } = action.payload;
      const { models, selectedModelIndex } = state;

      const updatedModels = models.map(model => {
        return Object.assign({}, model);
      });

      updatedModels[selectedModelIndex].editor.elements = updatedModels[selectedModelIndex].editor.elements.filter(element => {
        if (Array.isArray(id)) {
          return !id.includes(element.data.id)
        } else {
          return element.data.id != id;
        }
      })

      return {
        ...state,
        models: updatedModels
      }
    }
    case ELEMENT_SELECT: {
      return {
        ...state,
        selectedElementId: action.payload.id
      }
    }
    case ELEMENT_START: {
      const {event: {currentTarget: {checked}}} = action.payload;
      const {models, selectedElementId} = state;

      const updatedModels = models.map(model => {
        return Object.assign({}, model);
      });

      return {
        ...state,
        models: updatedModels.map(model => {
          const startElementId = checked ? selectedElementId : "";
          return Object.assign({}, model, { startElementId });
        })
      }
    }
    case ELEMENT_UPDATE: {
      const {field, event: {currentTarget: {value}}} = action.payload;
      const {models, selectedModelIndex, selectedElementId} = state;

      const updatedModels = models.map(model => {
        return Object.assign({}, model);
      });

      updatedModels[selectedModelIndex].editor.elements.forEach(element => {
        if (element.data.id === selectedElementId) {
          element.data[field] = value;
        }
      })

      updatedModels[selectedModelIndex].vertices = models[selectedModelIndex].vertices.map(vertex => {
        if (vertex.id === selectedElementId) {
          vertex[field] = value;
        }
        return vertex;
      })
      updatedModels[selectedModelIndex].edges = models[selectedModelIndex].edges.map(edge => {
        if (edge.id === selectedElementId) {
          edge[field] = value;
        }
        return edge;
      })

      return {
        ...state,
        models: updatedModels
      }
    }
    default: {
      return state;
    }
  }
}
