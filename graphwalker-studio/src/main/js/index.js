import "core-js/stable";
import "regenerator-runtime/runtime";
import React from 'react';
import { render } from "react-dom";
import { Provider } from 'react-redux'
import store from './redux/store'
import Application from './Application';

render(<Provider store={store}><Application /></Provider>, document.getElementById('root'));
