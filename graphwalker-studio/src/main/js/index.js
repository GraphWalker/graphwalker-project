import React from 'react';
import { render } from "react-dom";
import { Provider } from 'react-redux'
import store from './redux/store'
import Application from './Application';

import './style.css';

render(<Provider store={store}><Application /></Provider>, document.getElementById('root'));
