import { combineReducers, createAction, createReducer } from '@reduxjs/toolkit'
import { act } from "react-dom/test-utils";
import {CommandExecutionMessage} from "./model";
import {add, notifyInfo} from "./utils";

export type AppPage = "command" | "calendar";

export interface CommandState {
    executionId: number,
    name: string,
    lines: string[]
}

export interface AppState {
    selectedPage: AppPage,
    commands: CommandState[]
}

export const increment = createAction('counter/increment')
export const decrement = createAction('counter/decrement')
export const changePage = createAction<AppPage>('app/changePage')
export const incrementByAmount = createAction<number>('counter/incrementByAmount')
export const sseEvent = createAction<CommandExecutionMessage>('sseevent')

export const initialState: AppState = { selectedPage: "command", commands: [] }

const appReducer = createReducer(initialState, (builder) => {
    builder
        .addCase(changePage, (state, action) => {
            notifyInfo("dupa")
            state.selectedPage = action.payload
        })
        .addCase(sseEvent, (state, action) => {
            const m = action.payload
            switch (m.t) {
                case "CommandExecutionStarted":
                    state.commands = [...state.commands, {name: m.commandName, executionId: m.executionId, lines: []}]
                    break;
                case "CommandLine":
                    state.commands = state.commands.map(it => (it.executionId === m.commandId)
                        ?  {...it, lines: add(it.lines, m.line)}
                        : it
                    )
                    break;
            }
            if(m.t === "CommandExecutionStarted"){
                console.error("IN HERE")

            } else if(m.t === "CommandLine") {

            } else {
                console.error("IN HERE")
            }
        })
})


const rootReducer = combineReducers({ app: appReducer })

export type RootState = ReturnType<typeof rootReducer>

export default rootReducer