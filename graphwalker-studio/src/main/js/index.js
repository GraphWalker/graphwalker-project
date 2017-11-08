import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import initStore from './redux/initStore';
import Studio from './studio';
import 'typeface-roboto';
import './styles.less';

const store = initStore({});

function Application() {
  return (
    <Provider store={store}>
      <Studio />
    </Provider>
  );
}

render(<Application />, document.getElementById('studio'));
