import React from 'react';
import './App.css';
import {faTerminal} from '@fortawesome/free-solid-svg-icons'
import {AppViewPort} from "./AppViewPort";


function App() {
  const elements = [
    {
      icon: faTerminal,
      onClick: () => {
      }
    }
  ];
  return (
    <AppViewPort elements={elements}>
    </AppViewPort>
  );
}

export default App;
