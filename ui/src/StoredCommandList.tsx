import { Button, Input, Tag } from "antd";
import Table, { ColumnsType } from "antd/lib/table";
import React, { ChangeEvent, useEffect, useState } from "react";
import { StoredCommand } from "./model";
import { apiGet, notifyError, stateUpdateFunctions } from "./utils";

interface State {
    commands: StoredCommand[],
    loading: boolean,
    searchBox: string
}

export function StoredCommandList(props: { selectCommand: (command: StoredCommand) => void, executeCommand: (command: StoredCommand) => void }) {

    const [state, setState] = useState<State>({ commands: [], loading: true, searchBox: "" })
    const [updateStateAt, lazyUpdateStateAt] = stateUpdateFunctions(setState)

    useEffect(() => {
        apiGet<StoredCommand[]>("command/stored")
            .then(updateStateAt("commands"))
            .catch(notifyError)
            .finally(lazyUpdateStateAt("loading")(false))
    }, [])

    const onSearchBoxInput = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        updateStateAt("searchBox")(value)
    }

    let columns: ColumnsType<StoredCommand> = [
        {
            title: 'name',
            dataIndex: 'name',
            key: 'name',
        },
        {
            title: 'command',
            dataIndex: 'commandString',
            key: 'commandString',
        },
        {
            title: 'args',
            dataIndex: 'args',
            key: 'args',
            render: (_, command) => {
                return command.args.map(it => <Tag>{it}</Tag>)
            }
        },
        {
            title: 'directory',
            dataIndex: 'dir',
            key: 'dir',
        },
        {
            title: "",
            key: "action",
            render: (_, command) => {
                return <div>
                    <Button onClick={() => props.selectCommand(command)}>Select</Button>
                    <Button onClick={() => props.executeCommand(command)}>Execute</Button>
                </div >
            }
        }]

    const visibleCommands = state.commands.filter(it => {
        return it.name.includes(state.searchBox)
            || it.commandString.includes(state.searchBox)
            || it.dir.includes(state.searchBox);
    })
    let tableData = Array.from(Array(20).keys()).map(it => (
        {
            name: `kek ${it}`,
            commandString: "commandString",
            args: ["args"],
            dir: "dir",
            uses: 1
        }))
    return <>
        <div style={{ position: "relative" }}>
            <div style={{ position: "absolute", zIndex: 999, top: 0, transform: "translate(0px, 15px)" }}>
                <Input value={state.searchBox} onChange={onSearchBoxInput} placeholder="Filter commands" />
            </div>
            <Table pagination={{ position: ["topRight"] }} loading={state.loading} columns={columns} dataSource={visibleCommands} sticky />
        </div>
    </>

}