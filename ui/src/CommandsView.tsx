import { useApp } from "./store";
import { Button, Tabs } from "antd";
import React, { useState } from "react";
import { add } from "./utils";
import { match } from "ts-pattern";
import { NewCommandForm } from "./NewCommandForm";

export function CommandsView() {
    const NEW_COMMAND_KEY = "NEW_COMMAND_KEY"
    const [store, dispatch] = useApp();
    const [state, setState] = useState<{ activeCommand?: string, adding: boolean }>({ activeCommand: undefined, adding: false });

    const addCommandTab = state.adding ?
        <Tabs.TabPane tab={"new command"} key={NEW_COMMAND_KEY} closable={false}>
            <NewCommandForm />
        </Tabs.TabPane>
        : undefined

    return (
        <div>
            <Tabs type="editable-card" activeKey={state.activeCommand}
                onEdit={(key, action) => {
                    match(action)
                        .with("add", () => setState(s => ({ ...s, activeCommand: NEW_COMMAND_KEY, adding: true })))
                        .with("remove", () => /* TODO */ console.error("NOT_IMPLEMENTED"))
                        .run()
                }}
                onChange={(key) => {
                    setState(s => ({ ...s, activeCommand: key, adding: key === NEW_COMMAND_KEY }))
                }}>
                {Array.from(Array(0).keys()).map(it => {
                    return <Tabs.TabPane tab={`tab ${it}`} key={it} closable={true}>
                        tab {it}
                    </Tabs.TabPane>
                })}
                {addCommandTab}
            </Tabs>
        </div>
    );
}