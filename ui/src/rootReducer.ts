import { combineReducers, createAction, createReducer } from '@reduxjs/toolkit'
import { act } from "react-dom/test-utils";

export type AppPage = "command" | "calendar";

export interface AppState {
    selectedPage: AppPage,
    commands: string[]
}

export const increment = createAction('counter/increment')
export const decrement = createAction('counter/decrement')
export const changePage = createAction<AppPage>('app/changePage')
export const incrementByAmount = createAction<number>('counter/incrementByAmount')

export const initialState: AppState = { selectedPage: "command", commands: ["command1"] }



const appReducer = createReducer(initialState, (builder) => {
    builder
        .addCase(changePage, (state, action) => {
            state.selectedPage = action.payload
        })
})


const rootReducer = combineReducers({ app: appReducer })

export type RootState = ReturnType<typeof rootReducer>

export default rootReducer