import {configureStore} from "@reduxjs/toolkit";
import {useDispatch, useSelector} from "react-redux";


import rootReducer, {AppState, RootState} from './rootReducer'
import {useEffect} from "react";

export const store = configureStore({
    reducer: rootReducer
})

if (process.env.NODE_ENV === 'development' && module.hot) {
    module.hot.accept('./rootReducer', () => {
        const newRootReducer = require('./rootReducer').default
        store.replaceReducer(newRootReducer)
    })
}

export type AppDispatch = typeof store.dispatch

export const useAppState = () => useSelector((s: RootState) => {
    return s.app;
});

export function useApp(): [AppState, AppDispatch] {
    const appState = useAppState();
    const dispatch = useDispatch<AppDispatch>()
    useEffect(() => {
        return () => {}
    })
    return [appState, dispatch];
}
