import React, { ReactElement, useState } from 'react';
import './App.css';
import { faTerminal } from '@fortawesome/free-solid-svg-icons';
import { AppViewPort } from './AppViewPort';
import { CommandPage } from './CommandPage';

type Page = 'command';

interface AppState {
  //TODO
  commandState: {};
  selectedPage: Page;
}

function App() {
  const [ state, setState ] = useState<AppState>({ commandState: {}, selectedPage: 'command' });
  const setPage = (page: Page) => () => {
    setState((s) => ({ ...s, selectedPage: page }));
  };
  const elements = [
    {
      icon: faTerminal,
      onClick: setPage('command')
    }
  ];
  const selectedPage: ReactElement | undefined = (() => {
    switch (state.selectedPage) {
      case 'command':
        return <CommandPage commandState={state.commandState} />;
    }
  })();
  return <AppViewPort elements={elements}>{selectedPage}</AppViewPort>;
}

export default App;
