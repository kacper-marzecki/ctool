import React, {useState} from 'react';
import { TabDefinition, Tabs } from './Tabs';
import { add } from './utils';

interface Props {
  commandState: {
    commands: string[];
  };
}

export function CommandPage(props: Props) {
  const commandTabs: TabDefinition[] = props.commandState.commands.map((it) => ({
    name: it,
    id: it,
    content: <div>kekk</div>,
    isActive: false
  }));
  const addTab = {
    name: 'NEW',
    id: 'NEW',
    content: <div> added tab</div>,
    isActive: true
  };
  const [tabs, setTabs] = useState(add(commandTabs, addTab))
  const focusTab = (id: string) => setTabs(s => s.map(it => ({...it, isActive: it.id === id})))
  return <Tabs tabs={tabs} onTabClick ={focusTab}  />;
}
