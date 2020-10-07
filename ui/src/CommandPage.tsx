import React from 'react';

interface Props {
  commandState: {};
}

export function CommandPage(props: Props) {
  return (
    <ul className="flex border-b">
      // TODO
      <li className="-mb-px mr-1">
        <a
          className="bg-white inline-block border-l border-t border-r rounded-t py-2 px-4 text-blue-700 font-semibold"
          href="#"
        >
          Tab
        </a>
      </li>
      <li className="mr-1">
        <a className="bg-white inline-block py-2 px-4 text-blue-500 hover:text-blue-800 font-semibold" href="#">
          Tab
        </a>
      </li>
      <li className="mr-1">
        <a className="bg-white inline-block py-2 px-4 text-blue-500 hover:text-blue-800 font-semibold" href="#">
          Tab
        </a>
      </li>
      <li className="mr-1">
        <a className="bg-white inline-block py-2 px-4 text-gray-400 font-semibold" href="#">
          Tab
        </a>
      </li>
    </ul>
  );
}
