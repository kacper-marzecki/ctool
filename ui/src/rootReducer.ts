import {combineReducers, createAction, createReducer} from '@reduxjs/toolkit'
import {act} from "react-dom/test-utils";

export type AppPage= "command" | "calendar";

export interface AppState {
    selectedPage: AppPage
}

export const increment = createAction('counter/increment')
export const decrement = createAction('counter/decrement')
export const changePage = createAction<AppPage>('app/changePage')
export const incrementByAmount = createAction<number>('counter/incrementByAmount')

export const initialState: AppState = {selectedPage: "command"}



const appReducer = createReducer(initialState, (builder) => {
    builder
        .addCase(changePage, (state, action) => {
            state.selectedPage = action.payload
        })
})


const rootReducer = combineReducers({app: appReducer})

export type RootState = ReturnType<typeof rootReducer>

export default rootReducer