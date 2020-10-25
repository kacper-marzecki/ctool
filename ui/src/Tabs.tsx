import React, { useState } from 'react';

export interface TabDefinition {
  id: string;
  name: string;
  isActive: boolean;
  content: React.ReactNode
}

export function Tabs(props: { tabs: TabDefinition[], onTabClick: (id: string) => void}) {
  // const [tabs, setTabs] = useState(props.tabs);
  const tabNodes = props.tabs.map((tab) => {
    const selectedClass = tab.isActive ? 'text-blue-700 font-semibold' : 'text-grey-400';
    return (
      <li className="-mb-px mr-1">
        <button
          onClick ={() => props.onTabClick(tab.id)} 
          className={
            'bg-white inline-block border-l border-t border-r rounded-t py-2 px-4 ' + selectedClass
          }
        >
          {tab.name}
        </button>
      </li>
    );
  });
  const activeContent = props.tabs.find(it => it.isActive)?.content
  return (
    <div>
      <ul className="flex border-b">
        {tabNodes}
      </ul>
      {activeContent}
    </div>
  );
}
