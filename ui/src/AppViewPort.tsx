import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPowerOff} from "@fortawesome/free-solid-svg-icons/faPowerOff";
import React, {FunctionComponent} from "react";
import {IconDefinition} from "@fortawesome/fontawesome-common-types";

export interface NavbarElement {
  icon: IconDefinition,
  onClick: () => void
}

interface Props {
  elements: NavbarElement[]
}

export const AppViewPort: FunctionComponent<Props> = ({elements, children}) => {

  return (
    <div className="flex flex-row h-full">
      <nav className="bg-gray-900 w-20  justify-between flex flex-col ">
        <div className="mt-10 mb-10">
          <a href="#">
            <p className="font-mono text-lg text-white text-center">
              ctool
            </p>
          </a>
          <div className="mt-10">
            <ul>
              {elements.map((it, idx) => (
                <li key={idx} className="mb-6 mx-auto fill-current h-5 w-5 mx-auto text-gray-300 hover:text-green-500">
                  <a href="#">
              <span>
                <FontAwesomeIcon
                  onClick={it.onClick}
                  icon={it.icon}/>
              </span>
                  </a>
                </li>
              ))}

            </ul>
          </div>
        </div>
        <div className="mb-4 mx-auto hover:color-green-500">
          <a href="#">
          <span>
            <FontAwesomeIcon className="fa-grey "
                             icon={faPowerOff}/>
          </span>
          </a>
        </div>
      </nav>
      <div className="px-16 py-4 text-gray-700 bg-gray-200 h-screen w-screen">
        {children}
      </div>
    </div>
  );
}