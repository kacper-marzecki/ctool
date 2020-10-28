import { Tag, Button, Input, Table } from "antd";
import { ColumnsType } from "antd/lib/table";
import React, { ChangeEvent, useEffect, useState } from "react";
import { StoredCommand } from "./model";
import { stateUpdateFunctions, apiGet, notifyErrorAnd } from "./utils";

interface State {
    commands: StoredCommand[],
    loading: boolean,
    searchBox: string
}
export function RecentCommandList(props: { selectCommand: (command: StoredCommand) => void, executeCommand: (command: StoredCommand) => void }) {
    const [state, setState] = useState<State>({ commands: [], loading: true, searchBox: "" })
    const [updateStateAt, lazyUpdateStateAt] = stateUpdateFunctions(setState)

    useEffect(() => {
        apiGet<StoredCommand[]>("command/recent")
            .then(updateStateAt("commands"))
            .then(lazyUpdateStateAt("loading")(false))
            .catch(notifyErrorAnd(lazyUpdateStateAt("loading")(false)))
    }, [])

    const onSearchBoxInput = (e: ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        updateStateAt("searchBox")(value)
    }

    let columns: ColumnsType<StoredCommand> = [
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
        return it.commandString.includes(state.searchBox)
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