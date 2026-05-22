export interface Vertex {
  id: string;
  name: string;
  sharedState?: string;
  actions?: string[];
  requirements?: string[];
  properties?: { x: number; y: number };
}

export interface Edge {
  id: string;
  name: string;
  sourceVertexId?: string;
  targetVertexId: string;
  guard?: string;
  weight?: number;
  actions?: string[];
  requirements?: string[];
}

export interface GWModel {
  id: string;
  name: string;
  generator: string;
  startElementId: string;
  actions?: string[];
  vertices: Vertex[];
  edges: Edge[];
}

export interface TestFile {
  name?: string;
  seed?: number;
  models: GWModel[];
}
