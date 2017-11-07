import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import initRedux from './redux/initRedux';
import Studio from './studio';
import 'typeface-roboto';
import './styles.less';

const redux = initRedux({});

function Application() {
  return (
    <Provider store={redux}>
        <Studio />
    </Provider>
  );
}

render(<Application />, document.getElementById('studio'));
